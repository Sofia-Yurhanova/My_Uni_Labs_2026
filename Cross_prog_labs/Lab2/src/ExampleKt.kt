import java.util.Locale
import java.util.Scanner

fun formatNum(num: Double): String = when (num) {
    Double.NEGATIVE_INFINITY -> "-∞"
    Double.POSITIVE_INFINITY -> "+∞"
    else -> String.format(Locale.US, "%.2f", num)
}

fun intersect(A: Double, L: Double, R: Double): String {
    if (A >= R) return ""
    val start = if (A > L) A else L
    val bracket = if (A > L) "[" else "("
    return "$bracket${formatNum(start)}; ${formatNum(R)})"
}

fun main() {
    val sc = Scanner(System.`in`).useLocale(Locale.US)
    print("Введіть коефіцієнти a, b, c: ")
    val a = sc.nextDouble()
    val b = sc.nextDouble()
    val c = sc.nextDouble()

    val A = -a

    val result = when {
        b == 0.0 -> when {
            c > 0 -> intersect(A, 0.0, Double.POSITIVE_INFINITY)
            c < 0 -> intersect(A, Double.NEGATIVE_INFINITY, 0.0)
            else -> ""
        }
        b > 0.0 -> {
            val r = c / b
            when {
                r > 0 -> intersect(A, 0.0, r)
                r < 0 -> intersect(A, r, 0.0)
                else -> ""
            }
        }
        else -> { // b < 0
            val r = c / b
            val p1 = if (r >= 0) intersect(A, Double.NEGATIVE_INFINITY, 0.0)
            else intersect(A, Double.NEGATIVE_INFINITY, r)
            val p2 = if (r >= 0) intersect(A, r, Double.POSITIVE_INFINITY)
            else intersect(A, 0.0, Double.POSITIVE_INFINITY)

            // Об'єднуємо непорожні проміжки через знак ∪
            listOf(p1, p2).filter { it.isNotEmpty() }.joinToString(" ∪ ")
        }
    }

    println("Розв'язок системи:")
    if (result.isEmpty()) println("x ∈ ∅ (Розв'язків немає)")
    else println("x ∈ $result")
}