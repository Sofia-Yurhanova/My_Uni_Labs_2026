import random
import math

class NQueensAnnealing:
    def __init__(self, n):
        
        self.n = n
        self.initial_temp = 30.0
        self.final_temp = 0.5
        self.alpha = 0.98
        self.steps_per_change = 100
        
        # Ініціалізація стану (Етап 3)
        self.current_sol = [random.randint(0, self.n - 1) for _ in range(self.n)]
        self.current_energy = self.get_energy(self.current_sol)
        
        # Зберігання найкращого результату (Етап 5)
        self.best_sol = list(self.current_sol)
        self.best_energy = self.current_energy

    def get_energy(self, solution):
        """Етап 4: Функція оцінки (Енергія)"""
        conflicts = 0
        for i in range(self.n):
            for j in range(i + 1, self.n):
                # Перевірка горизонталі та діагоналей
                if solution[i] == solution[j] or abs(solution[i] - solution[j]) == abs(i - j):
                    conflicts += 1
        return conflicts

    def print_board(self, sol):
        """Етап 6: Вивід шахової дощки"""
        print("+" + "---" * self.n + "+")
        for row in range(self.n):
            line = "| "
            for col in range(self.n):
                line += "Q  " if sol[col] == row else ".  "
            print(line + "|")
        print("+" + "---" * self.n + "+")

    def run(self):
        """Етап 7: Реалізація алгоритму відпалу"""
        temp = self.initial_temp
        
        print(f"Початкова енергія: {self.current_energy}")

        while temp > self.final_temp and self.best_energy > 0:
            for _ in range(self.steps_per_change):
                neighbor = list(self.current_sol) # Етап 5: Копіювання
                col = random.randint(0, self.n - 1)
                neighbor[col] = random.randint(0, self.n - 1)
                
                new_energy = self.get_energy(neighbor)
                delta = new_energy - self.current_energy
                
                # Критерій Метрополіса
                if delta < 0 or random.random() < math.exp(-delta / temp):
                    self.current_sol = neighbor
                    self.current_energy = new_energy
                    
                    # Оновлюємо найкращий розв'язок
                    if self.current_energy < self.best_energy:
                        self.best_sol = list(self.current_sol)
                        self.best_energy = self.current_energy
                        if self.best_energy == 0: 
                            break
               
            if int(temp * 10) % 5 == 0: 
                print(f"Tемпература: {temp:.2f}, Конфліктів: {self.best_energy}")
            temp *= self.alpha # Геометричне охолодження
            
        return self.best_sol, self.best_energy

if __name__ == "__main__":
    N_SIZE = 10

    solver = NQueensAnnealing(n=N_SIZE)
    final_solution, final_conflicts = solver.run()

    print("\nКінцевий результат:")
    solver.print_board(final_solution)
    print(f"Конфліктів: {final_conflicts}")