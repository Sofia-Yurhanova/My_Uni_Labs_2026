import java.io.*

data class OrderKt(
    val productName: String,
    val price: Double,
    val quantity: Int,
    val customerName: String,
    val city: String,
    val deliveryMethod: String,
    val isPaid: Boolean
) : Serializable {
    override fun toString(): String {
        return "Товар: $productName | Ціна: $price | К-ть: $quantity | Замовник: $customerName | Місто: $city | Доставка: $deliveryMethod | Оплачено: ${if (isPaid) "Так" else "Ні"}"
    }
}

const val FILE_NAME = "orders_kt.dat"
var orders = mutableListOf<OrderKt>()

fun main() {
    loadFromFile() // Завантажуємо існуючі дані при старті

    while (true) {
        println("\n--- Меню Менеджера (Kotlin) ---")
        println("1. Додати замовлення")
        println("2. Вивести всі замовлення")
        println("3. Пошук: оплачені за містом")
        println("4. Пошук: неоплачені за доставкою")
        println("5. Видалити замовлення")
        println("0. Вийти")
        print("Оберіть дію: ")

        when (readlnOrNull()) {
            "1" -> addOrder()
            "2" -> displayAllOrders()
            "3" -> filterPaidByCity()
            "4" -> filterUnpaidByDelivery()
            "5" -> deleteOrder()
            "0" -> {
                println("Роботу завершено.")
                return
            }
            else -> println("Невірний вибір.")
        }
    }
}

fun getDeliveryMethodFromUser(): String {
    println("1 - Нова Пошта | 2 - Укрпошта | 3 - Кур'єр")
    print("Вибір: ")
    return when (readlnOrNull()) {
        "1" -> "Нова Пошта"
        "2" -> "Укрпошта"
        "3" -> "Кур'єр"
        else -> "Інший спосіб"
    }
}

fun readDouble(prompt: String): Double {
    while (true) {
        print(prompt)
        return readlnOrNull()?.toDoubleOrNull() ?: continue
    }
}

fun readInt(prompt: String): Int {
    while (true) {
        print(prompt)
        return readlnOrNull()?.toIntOrNull() ?: continue
    }
}

fun addOrder() {
    print("Товар: "); val prod = readlnOrNull() ?: ""
    val pr = readDouble("Ціна: ")
    val q = readInt("Кількість: ")
    print("Замовник: "); val name = readlnOrNull() ?: ""
    print("Місто: "); val city = readlnOrNull() ?: ""
    val del = getDeliveryMethodFromUser()
    print("Оплачено? (1-так, 0-ні): "); val paid = readlnOrNull() == "1"

    orders.add(OrderKt(prod, pr, q, name, city, del, paid))

    // АВТОЗБЕРЕЖЕННЯ
    saveToFile()
    println("Замовлення успішно додано та автоматично збережено у файл!")
}

fun displayAllOrders() {
    if (orders.isEmpty()) println("Порожньо.") else orders.forEach { println(it) }
}

fun filterPaidByCity() {
    print("Місто: "); val city = readlnOrNull() ?: ""
    val filtered = orders.filter { it.isPaid && it.city.equals(city, true) }
    if (filtered.isEmpty()) {
        println("Немає оплачених замовлень для міста $city.")
    } else {
        filtered.forEach { println(it) }
    }
}

fun filterUnpaidByDelivery() {
    val del = getDeliveryMethodFromUser()
    val filtered = orders.filter { !it.isPaid && it.deliveryMethod == del }
    if (filtered.isEmpty()) {
        println("Немає неоплачених замовлень для доставки: $del.")
    } else {
        filtered.forEach { println(it) }
    }
}

fun deleteOrder() {
    if (orders.isEmpty()) {
        println("Список порожній.")
        return
    }
    println("\nОберіть номер для видалення:")
    orders.forEachIndexed { index, order -> println("${index + 1}. $order") }
    while (true) {
        print("Введіть номер (0 - скасувати): ")
        val input = readlnOrNull()?.toIntOrNull()
        if (input == 0) return
        if (input != null && input in 1..orders.size) {
            orders.removeAt(input - 1)

            // АВТОЗБЕРЕЖЕННЯ
            saveToFile()
            println("Замовлення видалено та автоматично оновлено у файлі.")
            break
        } else { println("Невірний номер.") }
    }
}

fun saveToFile() {
    try {
        ObjectOutputStream(FileOutputStream(FILE_NAME)).use { it.writeObject(orders) }
    } catch (e: Exception) { println("Помилка збереження.") }
}

@Suppress("UNCHECKED_CAST")
fun loadFromFile() {
    val file = File(FILE_NAME)
    if (file.exists() && file.length() > 0) {
        try {
            ObjectInputStream(FileInputStream(file)).use { orders = it.readObject() as MutableList<OrderKt> }
        } catch (e: Exception) { }
    }
}