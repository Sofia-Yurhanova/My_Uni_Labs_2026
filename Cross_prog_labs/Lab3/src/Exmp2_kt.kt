import java.util.Scanner
import kotlin.math.PI
import kotlin.math.tan

fun parseValue(str: String): Double {
    val s = str.lowercase().replace(" ", "").replace("*", "")
    if (!s.contains("pi")) return s.toDouble()

    val parts = s.split("/")
    val numStr = parts[0].replace("pi", "")

    val num = when (numStr) {
        "" -> 1.0
        "-" -> -1.0
        else -> numStr.toDouble()
    }

    var valResult = num * PI
    if (parts.size > 1) {
        valResult /= parts[1].toDouble()
    }
    return valResult
}

fun main() {
    val sc = Scanner(System.`in`)

    print("Введіть a та b: ")
    val a = parseValue(sc.next())
    val b = parseValue(sc.next())

    print("Введіть c та d: ")
    val c = parseValue(sc.next())
    val d = parseValue(sc.next())

    val hx = (b - a) / 7
    val hy = (d - c) / 7

    print("   y \\ x |")
    for (i in 0..7) {
        System.out.printf("%8.2f", a + i * hx)
    }
    println("\n" + "-".repeat(80))

    for (j in 0..7) {
        val y = c + j * hy
        System.out.printf("%8.2f |", y)
        for (i in 0..7) {
            val x = a + i * hx
            val u = 5 * tan(x + y)
            if (Math.abs(u) > 10000) {
                System.out.printf("%8s", "∞")
            } else {
                System.out.printf("%8.3f", u)
            }
        }
        println()
    }
}