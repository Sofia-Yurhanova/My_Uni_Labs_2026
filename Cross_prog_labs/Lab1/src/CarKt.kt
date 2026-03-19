import java.util.Scanner
import java.util.Locale
import kotlin.math.abs

fun main() {
    val sc = Scanner(System.`in`).useLocale(Locale.US)

    fun readValidFloat(label: String): Float {
        var value: Float
        while (true) {
            print(label)
            value = sc.nextFloat()
            if (value >= 0) {
                break
            }
            println("Помилка: Значення не може бути від'ємним! Спробуйте ще раз.")
        }
        return value
    }

    val v1 = readValidFloat("Введіть V1 (км/год): ")
    val v2 = readValidFloat("Введіть V2 (км/год): ")
    val s = readValidFloat("Введіть початкову відстань S (км): ")
    val t = readValidFloat("Введіть час T (год): ")

    println("\n--- Результат ---")

    if (v1 != v2) {
        val diff = abs(v1 - v2) * t

        val dist1 = s + diff
        val dist2 = abs(s - diff)

        println("Оскільки швидкості різні, є два варіанти:")
        println("1. Якщо 1ий має меншу швидкість: %.2f км".format(dist1))
        println("2. Якщо 2ий має меншу швидкість: %.2f км".format(dist2))
    } else {
        println("Оскільки V1 = V2, відстань між ними не зміниться: %.2f км".format(s))
    }
}