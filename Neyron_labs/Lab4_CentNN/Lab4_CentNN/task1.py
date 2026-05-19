import numpy as np
import requests
import matplotlib.pyplot as plt

# --- 1. Словник посилань на датасети для ВАРІАНТУ 4 ---
datasets = {
    'unbalance2': {
        'url': 'https://web.archive.org/web/20230316224609/https://cs.joensuu.fi/sipu/datasets/unbalance2.txt',
        'M': 8 # Орієнтовна кількість кластерів
    },
    'D31': {
        'url': 'https://web.archive.org/web/20230316224609/https://cs.joensuu.fi/sipu/datasets/D31.txt',
        'M': 31 # 31 кластер
    },
    'dim128': {
        'url': 'https://web.archive.org/web/20230316224609/https://cs.joensuu.fi/sipu/datasets/dim128.txt',
        'M': 16 # 16 кластерів
    }
}

# --- 2. Функція завантаження даних ---
def load_data(url):
    response = requests.get(url)
    data = []
    for line in response.text.strip().split('\n'):
        parts = line.strip().split()
        if len(parts) >= 2:
            # Зчитуємо всі виміри, а не лише 2
            data.append([float(p) for p in parts])
    return np.array(data)

# --- 3. Алгоритм CentNN ---
def cent_nn_clustering(X, M, epsilon=0.01):
    N = X.shape[0] 
    c = np.mean(X, axis=0) 
    W = [c + epsilon, c - epsilon] 
    k = 2 
    assignments = np.zeros(N, dtype=int)
    
    while k <= M: 
        epoch = 0 
        while True:
            loser_count = 0 
            counts = np.zeros(k)
            
            for n in range(N): 
                x = X[n] 
                distances = [np.linalg.norm(x - w) for w in W]
                j = np.argmin(distances)
                i = assignments[n] 
                
                if epoch != 0 and i != j: 
                    loser_count += 1 
                    if counts[i] > 1:
                        W[i] = W[i] - (1.0 / (counts[i] - 1)) * (x - W[i])
                
                if epoch == 0 or i != j: 
                    counts[j] += 1
                    W[j] = W[j] + (1.0 / (counts[j] + 1)) * (x - W[j])
                
                assignments[n] = j
            
            epoch += 1 
            if loser_count == 0 or epoch > 30: 
                break
                
        if k < M: 
            errors = np.zeros(k)
            for n in range(N):
                j = assignments[n]
                errors[j] += np.linalg.norm(X[n] - W[j])**2
            
            worst_cluster = np.argmax(errors)
            W.append(W[worst_cluster] + epsilon)
            k += 1 
            print(f"  -> Знайдено {k} кластерів з {M}...")
        else:
            break
            
    return np.array(W), assignments

# --- 4. Виконання та візуалізація ---
fig, axes = plt.subplots(1, 3, figsize=(20, 6))

for idx, (name, params) in enumerate(datasets.items()):
    print(f"\n[{idx+1}/3] Обробка набору даних '{name}'...")
    X = load_data(params['url'])
    M = params['M']
    
    print(f"Завантажено точок: {len(X)}. Розмірність: {X.shape[1]}. Пошук {M} кластерів...")
    centroids, labels = cent_nn_clustering(X, M=M)
    
    # Малюємо (для dim128 беремо лише перші 2 координати для візуалізації)
    ax = axes[idx]
    ax.scatter(X[:, 0], X[:, 1], c=labels, cmap='tab20', s=10, alpha=0.6)
    ax.scatter(centroids[:, 0], centroids[:, 1], c='red', marker='X', s=80, edgecolors='black')
    ax.set_title(f"Набір: {name} | Кластерів: {M}")

plt.tight_layout()
plt.show()
print("\nВсі три набори успішно кластеризовані!")
