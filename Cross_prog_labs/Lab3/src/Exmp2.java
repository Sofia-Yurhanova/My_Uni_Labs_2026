import java.util.Scanner;

public class Exmp2 {

    // Функція для розпізнавання "pi" у введеному тексті
    public static double parseValue(String str) {
        str = str.toLowerCase().replace(" ", "").replace("*", "");
        if (!str.contains("pi")) {
            return Double.parseDouble(str);
        }

        String[] parts = str.split("/");
        String numStr = parts[0].replace("pi", "");

        double num = 1.0;
        if (numStr.equals("-")) num = -1.0;
        else if (!numStr.isEmpty()) num = Double.parseDouble(numStr);

        double val = num * Math.PI;
        if (parts.length > 1) {
            val /= Double.parseDouble(parts[1]);
        }
        return val;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Введіть a та b: ");
        double a = parseValue(sc.next());
        double b = parseValue(sc.next());

        System.out.print("Введіть c та d: ");
        double c = parseValue(sc.next());
        double d = parseValue(sc.next());

        double hx = (b - a) / 7;
        double hy = (d - c) / 7;

        System.out.print("   y \\ x |");
        for (int i = 0; i <= 7; i++) {
            System.out.printf("%8.2f", a + i * hx);
        }
        System.out.println("\n" + "-".repeat(80));

        for (int j = 0; j <= 7; j++) {
            double y = c + j * hy;
            System.out.printf("%8.2f |", y);
            for (int i = 0; i <= 7; i++) {
                double x = a + i * hx;
                double u = 5 * Math.tan(x + y);
                if (Math.abs(u) > 10000) {
                    System.out.printf("%8s", "∞");
                } else {
                    System.out.printf("%8.3f", u);
                }
            }
            System.out.println();
        }
    }
}