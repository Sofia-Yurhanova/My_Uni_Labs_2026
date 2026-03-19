import java.util.Scanner
import kotlin.math.abs

fun main() {
    val sc = Scanner(System.`in`)
    var number: Int

    while (true) {
        print("Введіть двоцифрове число: ")
        number = sc.nextInt()

        if (abs(number) in 10..99) {
            break
        }
        println("Помилка! Введіть саме двоцифрове число.")
    }

    val n = abs(number)
    val d1 = n / 10
    val d2 = n % 10

    val sum = d1 + d2
    val product = d1 * d2

    println("\nРезультат для числа $number :")
    println("Сума цифр: $d1 + $d2 = $sum")
    println("Добуток цифр: $d1 * $d2 = $product")
}