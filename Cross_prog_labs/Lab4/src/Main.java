import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String productName;
    private final double price;
    private final int quantity;
    private final String customerName;
    private final String city;
    private final String deliveryMethod;
    private final boolean isPaid;

    public Order(String productName, double price, int quantity, String customerName,
                 String city, String deliveryMethod, boolean isPaid) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.customerName = customerName;
        this.city = city;
        this.deliveryMethod = deliveryMethod;
        this.isPaid = isPaid;
    }

    public String getCity() { return city; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public boolean isPaid() { return isPaid; }

    @Override
    public String toString() {
        return String.format("Товар: %s | Ціна: %.2f | К-ть: %d | Замовник: %s | Місто: %s | Доставка: %s | Оплачено: %s",
                productName, price, quantity, customerName, city, deliveryMethod, (isPaid ? "Так" : "Ні"));
    }
}

public class Main {
    private static final String FILE_NAME = "orders_java.dat";
    private static List<Order> orders = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    static void main() {
        loadFromFile(); // Завантажуємо існуючі дані при старті

        while (true) {
            System.out.println("\n--- Меню Менеджера ---");
            System.out.println("1. Додати замовлення");
            System.out.println("2. Вивести всі замовлення");
            System.out.println("3. Пошук: оплачені за містом");
            System.out.println("4. Пошук: неоплачені за доставкою");
            System.out.println("5. Видалити замовлення");
            System.out.println("0. Вийти");
            System.out.print("Оберіть дію: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addOrder();
                case "2" -> displayAllOrders();
                case "3" -> filterPaidByCity();
                case "4" -> filterUnpaidByDelivery();
                case "5" -> deleteOrder();
                case "0" -> {
                    System.out.println("Роботу завершено.");
                    return;
                }
                default -> System.out.println("Невірний вибір. Спробуйте ще раз.");
            }
        }
    }

    private static String getDeliveryMethodFromUser() {
        System.out.println("1 - Нова Пошта | 2 - Укрпошта | 3 - Кур'єр");
        System.out.print("Ваш вибір: ");
        String choice = scanner.nextLine();
        return switch (choice) {
            case "1" -> "Нова Пошта";
            case "2" -> "Укрпошта";
            case "3" -> "Кур'єр";
            default -> "Інший спосіб";
        };
    }

    private static void addOrder() {
        System.out.print("Товар: "); String product = scanner.nextLine();
        double price;
        while (true) {
            System.out.print("Ціна: ");
            try { price = Double.parseDouble(scanner.nextLine()); break; }
            catch (NumberFormatException e) { System.out.println("Введіть число."); }
        }
        int quantity;
        while (true) {
            System.out.print("Кількість: ");
            try { quantity = Integer.parseInt(scanner.nextLine()); break; }
            catch (NumberFormatException e) { System.out.println("Введіть ціле число."); }
        }
        System.out.print("Замовник: "); String name = scanner.nextLine();
        System.out.print("Місто: "); String city = scanner.nextLine();
        String delivery = getDeliveryMethodFromUser();
        System.out.print("Оплачено? (1-так, 0-ні): ");
        boolean isPaid = scanner.nextLine().equals("1");

        orders.add(new Order(product, price, quantity, name, city, delivery, isPaid));

        // АВТОЗБЕРЕЖЕННЯ
        saveToFile();
        System.out.println("Замовлення успішно додано та автоматично збережено у файл!");
    }

    private static void displayAllOrders() {
        if (orders.isEmpty()) {
            System.out.println("Порожньо.");
        } else {
            orders.forEach(System.out::println);
        }
    }

    private static void filterPaidByCity() {
        System.out.print("Місто: "); String target = scanner.nextLine();
        List<Order> filtered = orders.stream()
                .filter(o -> o.isPaid() && o.getCity().equalsIgnoreCase(target))
                .toList();

        if (filtered.isEmpty()) {
            System.out.println("Немає оплачених замовлень для міста " + target + ".");
        } else {
            filtered.forEach(System.out::println);
        }
    }

    private static void filterUnpaidByDelivery() {
        String target = getDeliveryMethodFromUser();
        List<Order> filtered = orders.stream()
                .filter(o -> !o.isPaid() && o.getDeliveryMethod().equalsIgnoreCase(target))
                .toList();

        if (filtered.isEmpty()) {
            System.out.println("Немає неоплачених замовлень для доставки: " + target + ".");
        } else {
            filtered.forEach(System.out::println);
        }
    }

    private static void deleteOrder() {
        if (orders.isEmpty()) {
            System.out.println("Список порожній, нічого видаляти.");
            return;
        }
        System.out.println("\nОберіть номер замовлення для видалення:");
        for (int i = 0; i < orders.size(); i++) {
            System.out.println((i + 1) + ". " + orders.get(i));
        }
        int index;
        while (true) {
            System.out.print("Введіть номер (або 0 для скасування): ");
            try {
                index = Integer.parseInt(scanner.nextLine());
                if (index == 0) return;
                if (index > 0 && index <= orders.size()) {
                    orders.remove(index - 1);

                    // АВТОЗБЕРЕЖЕННЯ
                    saveToFile();
                    System.out.println("Замовлення видалено та автоматично оновлено у файлі.");
                    break;
                } else { System.out.println("Невірний номер."); }
            } catch (NumberFormatException e) { System.out.println("Помилка! Введіть ціле число."); }
        }
    }

    // Зробили метод private, бо тепер він викликається лише зсередини (автоматично)
    private static void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(orders);
        } catch (IOException e) { System.out.println("Помилка збереження: " + e.getMessage()); }
    }

    @SuppressWarnings("unchecked")
    private static void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists() || file.length() == 0) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            orders = (List<Order>) ois.readObject();
        } catch (Exception e) { System.out.println("Помилка завантаження."); }
    }
}