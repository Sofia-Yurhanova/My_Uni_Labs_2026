import java.util.Scanner;

public class Number {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int number;

        while (true) {
            System.out.print("Введіть двоцифрове число: ");
            number = scanner.nextInt();

            int absNum = Math.abs(number);
            if (absNum >= 10 && absNum <= 99) {
                break;
            }
            System.out.println("Помилка! Число має бути двоцифровим (від 10 до 99).");
        }

        int n = Math.abs(number);

        int d1 = n / 10;
        int d2 = n % 10;

        int sum = d1 + d2;
        int product = d1 * d2;

        System.out.println("Цифри числа: " + d1 + " та " + d2);
        System.out.println("Сума цифр: " + sum);
        System.out.println("Добуток цифр: " + product);
    }
}
