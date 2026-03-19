import numpy as np
import matplotlib.pyplot as plt
import random
from scipy.integrate import quad

class MonteCarloIntegration:
    def __init__(self, num_points=10000):
        self.num_points = num_points

    def evaluate_function(self, x, mode):
        if mode == 1:
            # Тестова функція: f(x) = x^2 на [0, 1]
            return x**2
        elif mode == 2:
            # Основна функція: f(x) = e^(-x^2) на [0, 2]
            return np.exp(-x**2)
        else:
            raise ValueError(" ")

    def exact_test_integral(self):
        return 1.0 / 3.0

    def generate_random_point(self, a, b, y_max):
        x = random.uniform(a, b)
        y = random.uniform(0, y_max)
        return x, y

    def monte_carlo_integrate(self, a, b, y_max, mode, title):
        points_under = 0
        x_under, y_under = [], []
        x_over, y_over = [], []

        # Генерування рандомних точок
        for _ in range(self.num_points):
            x, y = self.generate_random_point(a, b, y_max)
            f_x = self.evaluate_function(x, mode)

            if y <= f_x:
                points_under += 1
                x_under.append(x)
                y_under.append(y)
            else:
                x_over.append(x)
                y_over.append(y)

        # Розрахунок площі
        rect_area = (b - a) * y_max
        integral_approx = rect_area * (points_under / self.num_points)

        # Графік точок
        plt.figure(figsize=(8, 6))
        plt.scatter(x_under, y_under, color='blue', s=1, label='Точки під графіком')
        plt.scatter(x_over, y_over, color='pink', s=1, label='Точки над графіком')
        
        # Лінія функції
        x_line = np.linspace(a, b, 100)
        y_line = [self.evaluate_function(val, mode) for val in x_line]
        plt.plot(x_line, y_line, color='black', linewidth=2, label='f(x)')
        
        plt.title(title)
        plt.legend()
        plt.show()

        return integral_approx

# --- ЕТАП 8: Проведення обчислень ---
if __name__ == "__main__":
    mc = MonteCarloIntegration(num_points=50000)

    print("=== ТЕСТОВА ЗАДАЧА (Режим 1)")
    print("Функція: f(x) = x^2, Інтервал: [0, 1]")
    a1, b1, y_max1 = 0.0, 1.0, 1.0 
    
    approx_val1 = mc.monte_carlo_integrate(a1, b1, y_max1, mode=1, title="Монте-Карло: Тестова функція f(x)=x^2")
    exact_val1 = mc.exact_test_integral()
    
    abs_error1 = abs(exact_val1 - approx_val1)
    rel_error1 = (abs_error1 / exact_val1) * 100

    print(f"Точне значення: {exact_val1:.6f}")
    print(f"Обчислене методом Монте-Карло: {approx_val1:.6f}")
    print(f"Абсолютна похибка: {abs_error1:.6f}")
    print(f"Відносна похибка: {rel_error1:.2f}%\n")


    print("=== ОСНОВНА ЗАДАЧА (Режим 2)")
    print("Функція: f(x) = e^(-x^2), Інтервал: [0, 2]")
    
    a2, b2 = 0.0, 2.0
    y_max2 = 1.0  # при x=0
    
    approx_val2 = mc.monte_carlo_integrate(a2, b2, y_max2, mode=2, title="Монте-Карло: Основна функція f(x)=e^(-x^2)")
    
    # Використовуємо чисельний метод з scipy для оцінки похибки
    exact_val2_scipy, _ = quad(lambda x: np.exp(-x**2), a2, b2)
    
    abs_error2 = abs(exact_val2_scipy - approx_val2)
    rel_error2 = (abs_error2 / exact_val2_scipy) * 100

    print(f"Точне значення (через scipy.quad): {exact_val2_scipy:.6f}")
    print(f"Обчислене методом Монте-Карло: {approx_val2:.6f}")
    print(f"Оцінка абсолютної похибки: {abs_error2:.6f}")
    print(f"Оцінка відносної похибки: {rel_error2:.2f}%")