import java.util.Scanner
import kotlin.math.abs
import kotlin.math.cosh

fun main() {
    val sc = Scanner(System.`in`)
    print("Введіть значення x: ")
    val x = sc.nextDouble()
    val eps = 0.00001

    var sum = 0.0
    var a_k = 1.0
    var k = 0

    while (abs(a_k) >= eps) {
        sum += a_k
        k += 1
        a_k = a_k * (x * x) / ((2 * k - 1) * 2 * k)
    }

    val ch_x = cosh(x)

    System.out.printf("Обчислена сума (sum): %.6f\n", sum)
    println("Кількість доданків (k): $k")
    System.out.printf("Бібліотечна функція (ch_x): %.6f\n", ch_x)
}