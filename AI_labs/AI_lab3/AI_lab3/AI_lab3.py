import numpy as np
import matplotlib.pyplot as plt

# Етап 3: Допоміжна функція міри віддалі
def euclidean_distance(p1, p2):
    """Обчислює Евклідову відстань між двома точками."""
    return np.sqrt(np.sum((p1 - p2)**2))

# Етап 4: Алгоритм K-середніх (K-means)
def kmeans_clustering(data, k, max_iters=100):
    n_samples, n_features = data.shape
    random_indices = np.random.choice(n_samples, k, replace=False)
    centroids = data[random_indices]
    labels = np.zeros(n_samples, dtype=int)
    
    for _ in range(max_iters):
        for i, point in enumerate(data):
            distances = [euclidean_distance(point, centroid) for centroid in centroids]
            labels[i] = np.argmin(distances)
            
        new_centroids = np.zeros((k, n_features))
        for cluster_idx in range(k):
            cluster_points = data[labels == cluster_idx]
            if len(cluster_points) > 0:
                new_centroids[cluster_idx] = np.mean(cluster_points, axis=0)
            else:
                new_centroids[cluster_idx] = data[np.random.choice(n_samples)]
                
        if np.allclose(centroids, new_centroids):
            break
        centroids = new_centroids
        
    return labels, centroids

# Етап 5: Алгоритм C-середніх (Fuzzy C-means)
def c_means_clustering(data, c, m=2, max_iters=100, error=1e-5):
    """
    Реалізація алгоритму нечітких C-середніх (Fuzzy C-means).
    c - кількість кластерів
    m - експоненціальна вага (ступінь нечіткості), = 2
    """
    n_samples, n_features = data.shape
    
    # Ініціалізуємо матрицю належності випадковими значеннями та нормалізуємо
    U = np.random.rand(n_samples, c)
    U = U / np.sum(U, axis=1)[:, np.newaxis]
    
    centroids = np.zeros((c, n_features))
    
    for _ in range(max_iters):
        U_old = U.copy()
        
        # 1. Обчислюємо нові центроїди
        U_m = U ** m
        centroids = (U_m.T @ data) / np.sum(U_m, axis=0)[:, np.newaxis]
        
        # 2. Оновлюємо матрицю належності U
        dist = np.zeros((n_samples, c))
        for i in range(c):
            dist[:, i] = np.linalg.norm(data - centroids[i], axis=1)
            
        # Уникаємо ділення на нуль
        dist = np.fmax(dist, np.finfo(np.float64).eps)
        
        power = 2.0 / (m - 1)
        for i in range(c):
            U[:, i] = 1.0 / np.sum((dist[:, i:i+1] / dist) ** power, axis=1)
            
        # Умова зупинки
        if np.linalg.norm(U - U_old) < error:
            break
            
    # Перетворюємо "нечіткі" ймовірності у жорсткі мітки (беремо кластер з максимальною ймовірністю)
    labels = np.argmax(U, axis=1)
    return labels, centroids

# Етап 7: Функція оцінки якості кластеризації
def calculate_clustering_quality(data, labels, centroids):
    """
    Оцінює середньо-зважену відстань від точок до їхніх центроїдів.
    """
    total_distance = 0
    for i, point in enumerate(data):
        total_distance += euclidean_distance(point, centroids[labels[i]])
    return total_distance / len(data)

N = 5000
X = np.random.rand(N, 2)
K_CLUSTERS = 6

# Запуск алгоритмів
k_labels, k_centroids = kmeans_clustering(X, k=K_CLUSTERS)
c_labels, c_centroids = c_means_clustering(X, c=K_CLUSTERS)

# Підрахунок якості
k_quality = calculate_clustering_quality(X, k_labels, k_centroids)
c_quality = calculate_clustering_quality(X, c_labels, c_centroids)

print("--- Результати K-means ---")
print(f"Кількість кластерів: {K_CLUSTERS}")
print(f"Оцінка якості (середня відстань до центру): {k_quality:.4f}\n")

print("--- Результати Fuzzy C-means ---")
print(f"Кількість кластерів: {K_CLUSTERS}")
print(f"Оцінка якості (середня відстань до центру): {c_quality:.4f}\n")

# Візуалізація
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

# Графік для K-means
ax1.scatter(X[:, 0], X[:, 1], c=k_labels, cmap='viridis', s=15)
ax1.scatter(k_centroids[:, 0], k_centroids[:, 1], c='red', marker='X', s=120, linewidths=3, label='Центроїди')
ax1.set_title(f'K-means (K={K_CLUSTERS})')
ax1.legend()

# Графік для C-means
ax2.scatter(X[:, 0], X[:, 1], c=c_labels, cmap='plasma', s=15)
ax2.scatter(c_centroids[:, 0], c_centroids[:, 1], c='red', marker='X', s=120, linewidths=3, label='Центроїди')
ax2.set_title(f'Fuzzy C-means (C={K_CLUSTERS})')
ax2.legend()

plt.suptitle('Порівняння алгоритмів K-середніх та C-середніх на випадкових даних', fontsize=14)
plt.tight_layout()
plt.show()