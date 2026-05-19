import pandas as pd
import os
import numpy as np
from sklearn.neighbors import NearestNeighbors
import warnings
warnings.filterwarnings('ignore')

# --- 0. РОЗУМНЕ ЗЧИТУВАННЯ CSV ---
def smart_load_csv(filename, expected_cols):
    for sep in [';', ',', '\t']:
        df = pd.read_csv(filename, sep=sep, encoding='latin-1', on_bad_lines='skip', low_memory=False)
        if df.shape[1] >= expected_cols:
            return df
    return df

# --- 1. ЗАВАНТАЖЕННЯ ТА ПОПЕРЕДНЯ ОБРОБКА ДАНИХ ---
def load_and_preprocess():
    print("Завантаження даних...")
    
    # Знаходимо папку, де лежить цей скрипт
    current_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Вказуємо точні назви файлів з приставкою BX-
    books_file = os.path.join(current_dir, 'BX-Books.csv')
    users_file = os.path.join(current_dir, 'BX-Users.csv')
    ratings_file = os.path.join(current_dir, 'BX-Book-Ratings.csv')
    
    # Завантажуємо
    books = smart_load_csv(books_file, 5)
    users = smart_load_csv(users_file, 2) 
    ratings = smart_load_csv(ratings_file, 3)

    if books.shape[1] < 5:
        raise ValueError(f"Помилка! Файл Books пошкоджений або не знайдений.")
    if users.shape[1] < 2:
        raise ValueError(f"Помилка! Файл Users пошкоджений або не знайдений.")
    if ratings.shape[1] < 3:
        raise ValueError(f"Помилка! Файл Ratings пошкоджений або не знайдений.")

    # Обрізаємо та перейменовуємо колонки
    books = books.iloc[:, :5]
    books.columns = ['isbn', 'title', 'author', 'year', 'publisher']

    users = users.iloc[:, :2]
    users.columns = ['user_id', 'age']

    ratings = ratings.iloc[:, :3]
    ratings.columns = ['user_id', 'isbn', 'rating']
    
    return books, users, ratings

# --- 2. ФІЛЬТРАЦІЯ ТА ЗВЕДЕНА ТАБЛИЦЯ ---
def create_pivot_table(ratings, min_user_ratings=200, min_book_ratings=50):
    print(f"Фільтрація: користувачі >= {min_user_ratings} оцінок, книги >= {min_book_ratings} оцінок...")
    
    user_counts = ratings['user_id'].value_counts()
    active_users = user_counts[user_counts >= min_user_ratings].index
    filtered_ratings = ratings[ratings['user_id'].isin(active_users)]

    book_counts = filtered_ratings['isbn'].value_counts()
    popular_books = book_counts[book_counts >= min_book_ratings].index
    final_ratings = filtered_ratings[filtered_ratings['isbn'].isin(popular_books)]

    pivot = final_ratings.pivot(index='isbn', columns='user_id', values='rating').fillna(0)
    print(f"Розмір зведеної таблиці: {pivot.shape[0]} книг x {pivot.shape[1]} користувачів")
    return pivot, final_ratings

# --- 3. АЛГОРИТМ CENTNN ДЛЯ КНИГ ---
def centnn_fit(X, M=10, epsilon=0.01):
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
            if loser_count == 0 or epoch > 15: 
                break
        
        if k < M:
            errors = np.zeros(k)
            for n in range(N):
                j = assignments[n]
                errors[j] += np.linalg.norm(X[n] - W[j])**2
            worst_cluster = np.argmax(errors)
            W.append(W[worst_cluster] + epsilon)
            k += 1
        else:
            break
    return np.array(W), assignments

# --- 4. ФУНКЦІЇ РЕКОМЕНДАЦІЇ ---
def recommend_knn(book_isbn, pivot, model, books_df, n=10):
    try:
        book_idx = pivot.index.get_loc(book_isbn)
        distances, indices = model.kneighbors(pivot.iloc[book_idx, :].values.reshape(1, -1), n_neighbors=n+1)
        
        recs = []
        for i in range(1, len(distances.flatten())):
            similar_isbn = pivot.index[indices.flatten()[i]]
            title = books_df[books_df['isbn'] == similar_isbn]['title'].values
            title = title[0] if len(title) > 0 else "Невідома назва"
            recs.append(f"- {title} (ISBN: {similar_isbn}, Відстань: {distances.flatten()[i]:.2f})")
        return recs
    except KeyError:
        return ["Книгу не знайдено у відфільтрованій базі."]

def recommend_centnn(book_isbn, pivot, assignments, books_df, n=10):
    try:
        book_idx = pivot.index.get_loc(book_isbn)
        cluster_id = assignments[book_idx]
        
        cluster_indices = np.where(assignments == cluster_id)[0]
        
        recs = []
        count = 0
        for idx in cluster_indices:
            if idx != book_idx and count < n:
                similar_isbn = pivot.index[idx]
                title = books_df[books_df['isbn'] == similar_isbn]['title'].values
                title = title[0] if len(title) > 0 else "Невідома назва"
                recs.append(f"- {title} (ISBN: {similar_isbn}, Кластер: {cluster_id})")
                count += 1
        return recs if recs else ["Немає достатньо книг у кластері."]
    except KeyError:
        return ["Книгу не знайдено у відфільтрованій базі."]

# --- ОСНОВНИЙ БЛОК ВИКОНАННЯ ---
if __name__ == "__main__":
    books, users, ratings = load_and_preprocess()
    
    # Базові параметри з методички
    pivot, final_ratings = create_pivot_table(ratings, min_user_ratings=200, min_book_ratings=50)
    
    # Тренування KNN
    print("Тренування моделі KNN...")
    knn_model = NearestNeighbors(metric='euclidean', algorithm='brute')
    knn_model.fit(pivot.values)
    
    # Тренування CentNN
    print("Тренування моделі CentNN (це займе деякий час)...")
    centroids, book_clusters = centnn_fit(pivot.values, M=20)
    
    # Вибираємо 10 випадкових книг
    test_books = np.random.choice(pivot.index, 10, replace=False)
    
    print("\n" + "="*50)
    print(" РЕЗУЛЬТАТИ РЕКОМЕНДАЦІЙ ")
    print("="*50)
    
    for isbn in test_books:
        title_q = books[books['isbn'] == isbn]['title'].values
        target_title = title_q[0] if len(title_q) > 0 else "Невідома назва"
        
        print(f"\nЦільова книга: {target_title} (ISBN: {isbn})")
        
        print("  Рекомендації KNN:")
        for rec in recommend_knn(isbn, pivot, knn_model, books, n=5):
            print(f"    {rec}")
            
        print("  Рекомендації CentNN:")
        for rec in recommend_centnn(isbn, pivot, book_clusters, books, n=5):
            print(f"    {rec}")