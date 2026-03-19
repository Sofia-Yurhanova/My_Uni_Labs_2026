import random
import json
import math

# --- 1. Робота з картою ---

def generate_map(filename="lab4.json"):
    # Випадкова кількість міст від 25 до 35
    N = random.randint(25, 35)
    
    # Генерація матриці відстаней (симетрична, діагональ = 0)
    distances = [[0 for _ in range(N)] for _ in range(N)]
    for i in range(N):
        for j in range(i + 1, N):
            dist = random.randint(10, 100)
            distances[i][j] = dist
            distances[j][i] = dist
            
    map_data = {"N": N, "distances": distances}
    
    # Збереження у файл
    with open(filename, 'w') as f:
        json.dump(map_data, f)
    print(f"Згенеровано карту з {N} містами і збережено у {filename}")
    return distances, N

def load_map(filename="map.json"):
    with open(filename, 'r') as f:
        map_data = json.load(f)
    print(f"Карту завантажено з {filename}")
    return map_data["distances"], map_data["N"]

# --- 2. Мурашиний алгоритм ---

class AntColonyOptimization:
    def __init__(self, distances, N, num_ants, iterations, alpha, beta, rho, Q=100):
        self.distances = distances
        self.N = N
        self.num_ants = num_ants
        self.iterations = iterations
        self.alpha = alpha
        self.beta = beta
        self.rho = rho
        self.Q = Q
        
        # Ініціалізація феромонів початковим малим значенням (напр., 0.1)
        self.pheromones = [[0.1 for _ in range(N)] for _ in range(N)]

    def solve(self):
        best_tour = None
        best_tour_length = float('inf')

        for _ in range(self.iterations):
            all_tours = self._construct_tours()
            self._update_pheromones(all_tours)
            
            # Пошук найкращого маршруту в поточній ітерації
            for tour, length in all_tours:
                if length < best_tour_length:
                    best_tour_length = length
                    best_tour = tour

        return best_tour, best_tour_length

    def _construct_tours(self):
        all_tours = []
        for _ in range(self.num_ants):
            start_city = random.randint(0, self.N - 1)
            tour = [start_city]
            unvisited = set(range(self.N))
            unvisited.remove(start_city)
            
            current_city = start_city
            tour_length = 0
            
            while unvisited:
                next_city = self._choose_next_city(current_city, unvisited)
                tour.append(next_city)
                tour_length += self.distances[current_city][next_city]
                unvisited.remove(next_city)
                current_city = next_city
                
            # Повернення в початкове місто
            tour.append(start_city)
            tour_length += self.distances[current_city][start_city]
            all_tours.append((tour, tour_length))
            
        return all_tours

    def _choose_next_city(self, current_city, unvisited):
        probabilities = []
        denominator = 0.0
        
        for city in unvisited:
            tau = self.pheromones[current_city][city] ** self.alpha
            eta = (1.0 / self.distances[current_city][city]) ** self.beta
            prob = tau * eta
            probabilities.append((city, prob))
            denominator += prob
            
        # Рулетка для вибору наступного міста
        random_val = random.uniform(0, denominator)
        cumulative = 0.0
        for city, prob in probabilities:
            cumulative += prob
            if cumulative >= random_val:
                return city
        return probabilities[-1][0] # Fallback

    def _update_pheromones(self, all_tours):
        # 1. Випаровування
        for i in range(self.N):
            for j in range(self.N):
                self.pheromones[i][j] *= (1.0 - self.rho)
                
        # 2. Відкладання нових феромонів
        for tour, length in all_tours:
            pheromone_to_add = self.Q / length
            for i in range(len(tour) - 1):
                city1, city2 = tour[i], tour[i+1]
                self.pheromones[city1][city2] += pheromone_to_add
                self.pheromones[city2][city1] += pheromone_to_add # Граф неорієнтований

# --- 3. Проведення симуляцій ---

if __name__ == "__main__":
    # Створюємо карту один раз
    generate_map("lab4_map.json")
    
    # Завантажуємо для використання в усіх симуляціях (гарантуємо однакову карту)
    distances, N = load_map("lab4_map.json")
    
    print(f"\n--- Запуск 10 симуляцій для карти з {N} міст ---")
    
    # Список конфігурацій для аналізу: (num_ants, alpha, beta, rho)
    # Змінюємо параметри, щоб дослідити їх вплив згідно із завданням 3
    simulations_params = [
        # Базові
        {"ants": 20, "alpha": 1.0, "beta": 2.0, "rho": 0.5},
        # Дослідження кількості мурах
        {"ants": 5, "alpha": 1.0, "beta": 2.0, "rho": 0.5},
        {"ants": 50, "alpha": 1.0, "beta": 2.0, "rho": 0.5},
        # Дослідження випаровування (rho)
        {"ants": 20, "alpha": 1.0, "beta": 2.0, "rho": 0.1}, # Повільне випаровування
        {"ants": 20, "alpha": 1.0, "beta": 2.0, "rho": 0.9}, # Швидке випаровування
        # Дослідження alpha (вплив феромонів)
        {"ants": 20, "alpha": 5.0, "beta": 1.0, "rho": 0.5}, # Сильний вплив стадного інстинкту
        {"ants": 20, "alpha": 0.1, "beta": 2.0, "rho": 0.5}, # Мурахи ігнорують феромони
        # Дослідження beta (вплив жадібності/видимості)
        {"ants": 20, "alpha": 1.0, "beta": 5.0, "rho": 0.5}, # Дуже жадібний алгоритм
        {"ants": 20, "alpha": 2.0, "beta": 0.1, "rho": 0.5}, # Сліпий пошук за феромонами
        # Збалансований
        {"ants": N, "alpha": 1.5, "beta": 3.0, "rho": 0.4}
    ]
    
    for idx, p in enumerate(simulations_params):
        aco = AntColonyOptimization(
            distances=distances, N=N, 
            num_ants=p["ants"], iterations=50, # 50 ітерацій достатньо для порівняння
            alpha=p["alpha"], beta=p["beta"], rho=p["rho"]
        )
        _, best_length = aco.solve()
        print(f"Симуляція {idx+1} | Мурах: {p['ants']:2d}, Alpha: {p['alpha']:3.1f}, Beta: {p['beta']:3.1f}, Rho: {p['rho']:3.1f} | Найкоротший шлях: {best_length:.2f}")
