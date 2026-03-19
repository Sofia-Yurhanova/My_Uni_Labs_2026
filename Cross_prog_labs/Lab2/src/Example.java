import java.util.Locale;
import java.util.Scanner;

public class Example {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useLocale(Locale.US);
        System.out.print("Введіть a, b, c через пробіл: ");
        double a = sc.nextDouble();
        double b = sc.nextDouble();
        double c = sc.nextDouble();

        double A = -a;
        String result = "";

        if (b == 0) {
            if (c > 0) result = intersect(A, 0, Double.POSITIVE_INFINITY);
            else if (c < 0) result = intersect(A, Double.NEGATIVE_INFINITY, 0);
        } else if (b > 0) {
            double r = c / b;
            if (r > 0) result = intersect(A, 0, r);
            else if (r < 0) result = intersect(A, r, 0);
        } else { // b < 0
            double r = c / b;
            if (r > 0) {
                result = combine(intersect(A, Double.NEGATIVE_INFINITY, 0),
                        intersect(A, r, Double.POSITIVE_INFINITY));
            } else if (r < 0) {
                result = combine(intersect(A, Double.NEGATIVE_INFINITY, r),
                        intersect(A, 0, Double.POSITIVE_INFINITY));
            } else { // root == 0
                result = combine(intersect(A, Double.NEGATIVE_INFINITY, 0),
                        intersect(A, 0, Double.POSITIVE_INFINITY));
            }
        }

        System.out.println("Розв'язок системи:");
        if (result.isEmpty()) {
            System.out.println("x ∈ ∅ (Розв'язків немає)");
        } else {
            System.out.println("x ∈ " + result);
        }
    }

    // Метод для знаходження перетину [A, +∞) та (L, R)
    private static String intersect(double A, double L, double R) {
        if (A >= R) return ""; // Не перетинаються

        double start = Math.max(A, L);
        String leftBracket = (A > L) ? "[" : "(";

        return leftBracket + formatNum(start) + "; " + formatNum(R) + ")";
    }

    // Метод для об'єднання двох проміжків
    private static String combine(String s1, String s2) {
        if (s1.isEmpty()) return s2;
        if (s2.isEmpty()) return s1;
        return s1 + " ∪ " + s2;
    }

    // Метод для красивого виводу нескінченності
    private static String formatNum(double num) {
        if (num == Double.NEGATIVE_INFINITY) return "-∞";
        if (num == Double.POSITIVE_INFINITY) return "+∞";
        return String.format(Locale.US, "%.2f", num);
    }
}
