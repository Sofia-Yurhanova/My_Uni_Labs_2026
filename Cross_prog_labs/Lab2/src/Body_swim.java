import java.util.Scanner;
import java.util.Locale;

public class Body_swim {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

        float m = readPositiveFloat(scanner, "Введіть масу тіла m (кг): ");
        float v = readPositiveFloat(scanner, "Введіть об'єм тіла V (м3): ");
        float pLiquid = readPositiveFloat(scanner, "Введіть густину рідини р (кг/м3): ");

        float pBody = m / v;
        System.out.printf("\nГустина тіла: %.2f кг/м3\n", pBody);

        if (pBody < pLiquid) {
            System.out.println("Результат: Тіло спливає.");
        } else if (Math.abs(pBody - pLiquid) < 0.0001) {
            System.out.println("Результат: Тіло плаває всередині рідини.");
        } else {
            System.out.println("Результат: Тіло тоне.");
        }
    }

    private static float readPositiveFloat(Scanner sc, String label) {
        float val;
        while (true) {
            System.out.print(label);
            if (sc.hasNextFloat()) {
                val = sc.nextFloat();
                if (val > 0) return val;
            } else {
                sc.next();
            }
            System.out.println("Помилка! Введіть додатне число.");
        }
    }
}
