import java.util.Scanner;
import java.util.Locale;

public class Car {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

        float v1 = readPositiveFloat(scanner, "Введіть V1 (км/год): ");
        float v2 = readPositiveFloat(scanner, "Введіть V2 (км/год): ");
        float s = readPositiveFloat(scanner, "Введіть початкову відстань S (км): ");
        float t = readPositiveFloat(scanner, "Введіть час T (год): ");

        System.out.println("\n--- Результат ---");

        if (v1 != v2) {
            float vR = Math.abs(v1 - v2);
            float distanceChange = vR * t;
            float distAway = s + distanceChange;
            float distTowards = Math.abs(s - distanceChange);

            System.out.println("Оскільки швидкості різні, є два варіанти:\n");
            System.out.printf("1. Якщо 1ий має меншу швидкість: %.2f км\n", distAway);
            System.out.printf("2. Якщо 2ий має меншу швидкість: %.2f км\n", distTowards);
        } else {
            System.out.printf("Оскільки V1 = V2, відстань між ними не зміниться: %.2f км\n", s);
        }
    }

    private static float readPositiveFloat(Scanner sc, String label) {
        float value;
        while (true) {
            System.out.print(label);
            if (sc.hasNextFloat()) {
                value = sc.nextFloat();
                if (value >= 0) {
                    return value;
                } else {
                    System.out.println("Помилка: Значення не може бути від'ємним!");
                }
            } else {
                System.out.println("Помилка: Введіть число!");
                sc.next();
            }
        }
    }
}