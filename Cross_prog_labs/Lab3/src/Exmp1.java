import java.util.Scanner;

public class Exmp1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Введіть значення x: ");
        double x = sc.nextDouble();
        double eps = 0.00001;

        double sum = 0;
        double a_k = 1;
        int k = 0;

        while (Math.abs(a_k) >= eps) {
            sum = sum + a_k;
            k = k + 1;
            a_k = a_k * (x * x) / ((2 * k - 1) * 2 * k);
        }

        double ch_x = Math.cosh(x);

        System.out.printf("Обчислена сума (sum): %.6f\n", sum);
        System.out.println("Кількість доданків (k): " + k);
        System.out.printf("Бібліотечна функція (ch_x): %.6f\n", ch_x);
    }
}