import java.util.Scanner
import java.util.Locale

fun main() {
    val sc = Scanner(System.`in`).useLocale(Locale.US)

    fun readFloat(label: String): Float {
        while (true) {
            print(label)
            val input = sc.nextFloat()
            if (input > 0) return input
            println("Число має бути більшим за 0!")
        }
    }

    val m = readFloat("Введіть масу m (кг): ")
    val v = readFloat("Введіть об'єм V (м3): ")
    val pLiquid = readFloat("Введіть густину рідини р (кг/м3): ")

    val rhoBody = m / v
    println("\nГустина тіла: $rhoBody кг/м3")

    when {
        rhoBody < pLiquid -> println("Тіло плаває на поверхні.")
        rhoBody == pLiquid -> println("Тіло плаває всередині рідини.")
        else -> println("Тіло тоне.")
    }
}