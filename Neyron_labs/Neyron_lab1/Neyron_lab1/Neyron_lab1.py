import numpy as np
import matplotlib.pyplot as plt

# 1. Параметри (2 входи, 2 нейрони)
W = np.array([[0.5, -0.8], 
              [0.9, 0.3]])
b = np.array([-0.2, 0.5])

print("=== Параметри нейронної мережі ===")
print(f"Матриця ваг W:\n{W}")
print(f"Вектор зміщення b: {b}\n")

def hardlim(n):
    # Функція активації (жорстке обмеження)
    return (n >= 0).astype(int)

test_inputs = [
    np.array([1, 2]),
    np.array([-2, 1]),
    np.array([0, 0]),
    np.array([2, -2]),
    np.array([-1, -1])
]

print("Результати тестування векторів:")
for P in test_inputs:
    # Обчислення: n = W*P + b
    n_raw = np.dot(W, P) + b
    # Активація: a = hardlim(n)
    a_raw = hardlim(n_raw)
    
    # Перетворюємо у звичайні списки для гарного виводу без np.float64
    P_list = P.tolist()
    n_list = [round(float(val), 2) for val in n_raw]
    a_list = a_raw.tolist()
    
    print(f"P = {P_list} -> n = {n_list} -> a = {a_list}")

plt.figure(figsize=(9, 9))
x_range = np.linspace(-3, 3, 100)

for i in range(len(W)):
    # Межа поділу: w1*p1 + w2*p2 + b = 0
    p2_vals = -(W[i, 0] * x_range + b[i]) / W[i, 1]
    line, = plt.plot(x_range, p2_vals, label=f'Межа нейрона {i+1}', linewidth=2)
    
    # Пунктирна лінія без зміщення (b=0)
    p2_no_bias = -(W[i, 0] * x_range) / W[i, 1]
    plt.plot(x_range, p2_no_bias, '--', color=line.get_color(), alpha=0.3)

# Відображення точок
for P in test_inputs:
    res = hardlim(np.dot(W, P) + b)
    plt.scatter(P[0], P[1], color='red', edgecolors='black', zorder=5)
    plt.text(P[0]+0.1, P[1]+0.1, f'a={res.tolist()}', fontsize=10, fontweight='bold')

    # 6. Перевстановлення параметрів (Функції ініціалізації)
print("\n=== ПУНКТ 6: ПЕРЕВСТАНОВЛЕННЯ ПАРАМЕТРІВ ===")

# Використовуємо функцію випадкової ініціалізації з діапазону [-1, 1]
W_new = np.random.uniform(-1, 1, (2, 2)) 
b_new = np.random.uniform(-1, 1, 2)

print(f"Нова випадкова матриця W:\n{W_new}")
print(f"Новий випадковий вектор b: {b_new}")

# Перевірка з новими параметрами для однієї точки
P_sample = np.array([1, 2])
n_new = np.dot(W_new, P_sample) + b_new
a_new = hardlim(n_new)

print(f"\nРезультат з новими вагами для P=[1, 2]:")
print(f"n = {n_new} -> a = {a_new}")


plt.xlim([-3, 3])
plt.ylim([-3, 3])
plt.axhline(0, color='black', lw=1)
plt.axvline(0, color='black', lw=1)
plt.grid(True, linestyle=':', alpha=0.6)
plt.title('Класифікація персептроном')
plt.xlabel('Вхід p1')
plt.ylabel('Вхід p2')
plt.legend(loc='upper right')

plt.tight_layout()
plt.show()
