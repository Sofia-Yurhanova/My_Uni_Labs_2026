import numpy as np
import matplotlib.pyplot as plt
import math

e1 = np.array([ 1,  1,  1,  1, 
               -1,  1, -1, -1, 
               -1,  1, -1, -1, 
               -1,  1, -1, -1]) 

e2 = np.array([ -1, 1, 1, -1, 
                1, -1, -1, 1, 
                1,  1,  1, 1, 
                1, -1, -1, 1])

patterns = [e1, e2]
pattern_names = ["Т", "A"]
M = len(patterns)   
N = len(patterns[0]) 

k = 1 / N 
T = N / 2
eps = 0.15 # 0 < eps < 1/M

plt.figure(figsize=(6, 3))
for i in range(M):
    plt.subplot(1, 2, i+1)
    plt.imshow(patterns[i].reshape(4, 4), cmap='gray_r')
    plt.title(f"Еталон {i+1}: {pattern_names[i]}")
    plt.axis('off')
plt.show()

first_layer_W = np.array(patterns) / 2
first_layer_b = N / 2

second_layer_W = np.zeros((M, M))
for i in range(M):
    for j in range(M):
        if i == j:
            second_layer_W[i, j] = 1
        else:
            second_layer_W[i, j] = -eps

def activation_func(s, T):
    if s <= 0: return 0
    elif s > T: return T
    else: return s

test_vector = np.copy(e1)
test_vector[0] = -1 
test_vector[15] = 1 

print("--ПЕРШИЙ ШАР (ХЕММІНГА)")
y1 = []
for i in range(M):
    # n = W*p + b
    s = np.dot(first_layer_W[i], test_vector) + first_layer_b
    out = activation_func(s, T)
    y1.append(out)
    print(f"Подібність до образу {pattern_names[i]}: {out}")

print("\n--ДРУГИЙ ШАР (MAXNET)")
last_iter = np.array(y1)
it = 0
E_max = 0.001

while True:
    it += 1
    next_iter = np.zeros(M)
    for i in range(M):
        temp = 0
        for j in range(M):
            temp += last_iter[j] * second_layer_W[i, j]
        next_iter[i] = activation_func(temp, T)
    
    print(f"Ітерація {it}: {np.round(next_iter, 4)}")
    
    norm = np.linalg.norm(next_iter - last_iter)
    if norm <= E_max or np.count_nonzero(next_iter) <= 1:
        last_iter = next_iter
        break
    last_iter = np.copy(next_iter)

winner = np.argmax(last_iter)
plt.imshow(test_vector.reshape(4, 4), cmap='gray_r')
plt.title(f"Розпізнано: {pattern_names[winner]}")
plt.axis('off')
plt.show()

print(f"\nРезультат: Мережа розпізнала образ '{pattern_names[winner]}'")