import json
import random
import copy
import os

# ==========================================
# Ініціалізація та збереження/читання даних
# ==========================================
DATA_FILE = 'school_data.json'

def generate_and_save_data():
    data = {
        "classes": ["1-А", "1-Б"],
        "subjects": {
            "Читання": {"count": 6, "special_room": None, "teacher_type": "homeroom"},
            "Математика": {"count": 5, "special_room": None, "teacher_type": "homeroom"},
            "ЯДС": {"count": 4, "special_room": None, "teacher_type": "homeroom"},
            "Мистецтво": {"count": 4, "special_room": None, "teacher_type": "homeroom"},
            "Фізкультура": {"count": 3, "special_room": "Спортзал", "teacher_type": "special_pe"},
            "Музика": {"count": 2, "special_room": "Клас музики", "teacher_type": "special_music"}
        }, 
        "teachers": {
            "1-А": {"homeroom": "Вчитель 1-А", "special_pe": "Вчитель Фіз-ри", "special_music": "Вчитель Музики"},
            "1-Б": {"homeroom": "Вчитель 1-Б", "special_pe": "Вчитель Фіз-ри", "special_music": "Вчитель Музики"}
        },
        "rooms": {
            "1-А": "Кабінет 1-А", 
            "1-Б": "Кабінет 1-Б", 
            "Спортзал": "Спортзал", 
            "Клас музики": "Клас музики"
        }
    }
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)
    return data

def load_data():
    if not os.path.exists(DATA_FILE):
        return generate_and_save_data()
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

SCHOOL_DATA = load_data()

BASE_LESSONS = []
for subj, info in SCHOOL_DATA["subjects"].items():
    BASE_LESSONS.extend([subj] * info["count"])

DAYS_SLICES = [
    slice(0, 5),   # Понеділок
    slice(5, 10),  # Вівторок
    slice(10, 15), # Середа
    slice(15, 20), # Четвер
    slice(20, 24)  # П'ятниця
]

# ==========================================
# Структура Хромосоми
# ==========================================
class Chromosome:
    def __init__(self, schedule=None):
        self.schedule = schedule if schedule else {}
        self.fitness = 0

    def calculate_fitness(self):
        penalty = 0
        num_slots = len(BASE_LESSONS)
        classes = SCHOOL_DATA["classes"]
        
        # 1. ЖОРСТКІ ОБМЕЖЕННЯ (Штраф 100)
        for slot in range(num_slots):
            teachers_in_slot = set()
            rooms_in_slot = set()
            
            for cls in classes:
                subject = self.schedule[cls][slot]
                subj_info = SCHOOL_DATA["subjects"][subject]
                
                teacher = SCHOOL_DATA["teachers"][cls][subj_info["teacher_type"]]
                room = subj_info["special_room"] if subj_info["special_room"] else SCHOOL_DATA["rooms"][cls]
                
                if teacher in teachers_in_slot:
                    penalty += 100 
                teachers_in_slot.add(teacher)
                
                if room in rooms_in_slot:
                    penalty += 100 
                rooms_in_slot.add(room)
                
        # 2. М'ЯКІ ОБМЕЖЕННЯ
        for cls in classes:
            class_schedule = self.schedule[cls]
            
            # Штраф 20: Кілька Фізкультур в один день
            for day_slice in DAYS_SLICES:
                lessons_today = class_schedule[day_slice]
                pe_count = lessons_today.count("Фізкультура")
                if pe_count > 1:
                    penalty += (pe_count - 1) * 20
                    
            # Штраф 10: Однакові предмети підряд
            for i in range(len(class_schedule) - 1):
                if class_schedule[i] == class_schedule[i+1]:
                    if (i + 1) % 5 != 0: 
                        penalty += 10
                
        self.fitness = penalty
        return self.fitness

# ==========================================
# Генетичні Оператори
# ==========================================
def init_population(pop_size):
    population = []
    for _ in range(pop_size):
        schedule = {}
        for cls in SCHOOL_DATA["classes"]:
            shuffled_lessons = BASE_LESSONS.copy()
            random.shuffle(shuffled_lessons)
            schedule[cls] = shuffled_lessons
        
        chrom = Chromosome(schedule)
        chrom.calculate_fitness()
        population.append(chrom)
    return population

def mutate(chromosome, mutation_rate=0.1):
    for cls in SCHOOL_DATA["classes"]:
        if random.random() < mutation_rate:
            idx1, idx2 = random.sample(range(len(BASE_LESSONS)), 2)
            chromosome.schedule[cls][idx1], chromosome.schedule[cls][idx2] = \
                chromosome.schedule[cls][idx2], chromosome.schedule[cls][idx1]

def crossover_one_point(parent1, parent2):
    child_schedule = {}
    classes = SCHOOL_DATA["classes"]
    split_point = random.randint(1, len(classes) - 1) if len(classes) > 1 else 1
    
    for i, cls in enumerate(classes):
        if i < split_point:
            child_schedule[cls] = copy.deepcopy(parent1.schedule[cls])
        else:
            child_schedule[cls] = copy.deepcopy(parent2.schedule[cls])
            
    return Chromosome(child_schedule)

def crossover_multi_point(parent1, parent2):
    child_schedule = {}
    for cls in SCHOOL_DATA["classes"]:
        p1_genes = parent1.schedule[cls]
        p2_genes = parent2.schedule[cls]
        
        start, end = sorted(random.sample(range(len(BASE_LESSONS)), 2))
        child_genes = [None] * len(BASE_LESSONS)
        child_genes[start:end] = p1_genes[start:end]
        
        p1_counts = {subj: child_genes.count(subj) for subj in set(BASE_LESSONS)}
        
        ptr = 0
        for gene in p2_genes:
            # ВИПРАВЛЕНА ПОМИЛКА: 
            # Спочатку безпечно просуваємо вказівник до найближчого вільного місця
            while ptr < len(BASE_LESSONS) and child_genes[ptr] is not None:
                ptr += 1
            
            # Якщо місця закінчилися - перериваємо цикл ДО звернення до масиву
            if ptr >= len(BASE_LESSONS):
                break
                
            if p1_counts.get(gene, 0) < SCHOOL_DATA["subjects"][gene]["count"]:
                child_genes[ptr] = gene
                p1_counts[gene] = p1_counts.get(gene, 0) + 1
                ptr += 1

        for i in range(len(child_genes)):
            if child_genes[i] is None:
                for subj, info in SCHOOL_DATA["subjects"].items():
                    if child_genes.count(subj) < info["count"]:
                        child_genes[i] = subj
                        break

        child_schedule[cls] = child_genes
    return Chromosome(child_schedule)

# ==========================================
# Основний цикл ГА з виводом динаміки
# ==========================================
def run_genetic_algorithm(pop_size=30, max_generations=500, mutation_rate=0.2):
    print(" Ініціалізація популяції...")
    population = init_population(pop_size)
    
    best_penalty_history = []
    
    for generation in range(max_generations):
        population.sort(key=lambda x: x.fitness)
        
        best_chromosome = population[0]
        current_best_fitness = best_chromosome.fitness
        
        if generation == 0 or current_best_fitness < best_penalty_history[-1]:
            print(f"Покоління {generation:3} | Знайдено кращий розклад! Штраф: {current_best_fitness}")
            
        best_penalty_history.append(current_best_fitness)
        
        if current_best_fitness == 0:
            print(f" Ідеальний розклад знайдено на поколінні {generation}!")
            return best_chromosome
            
        new_population = []
        elite_count = max(1, int(pop_size * 0.1))
        new_population.extend(copy.deepcopy(population[:elite_count]))
        
        while len(new_population) < pop_size:
            parents = random.sample(population[:pop_size//2], 2)
            p1, p2 = parents[0], parents[1]
            
            if random.random() < 0.5:
                child = crossover_one_point(p1, p2)
            else:
                child = crossover_multi_point(p1, p2)
                
            mutate(child, mutation_rate)
            child.calculate_fitness()
            new_population.append(child)
            
        population = new_population
        
    print(f"\n Досягнуто ліміту поколінь ({max_generations}).")
    population.sort(key=lambda x: x.fitness)
    return population[0]

# ==========================================
# Запуск програми та вивід результатів
# ==========================================
if __name__ == "__main__":
    # pop_size=20 гарантує, що алгоритм еволюціонуватиме і покаже процес роботи
    best_schedule = run_genetic_algorithm(pop_size=20, max_generations=300, mutation_rate=0.15)
    
    print("\n" + "="*80)
    print(" НАЙКРАЩИЙ ЗНАЙДЕНИЙ РОЗКЛАД (Штраф: {})".format(best_schedule.fitness))
    print("="*80)
    
    days = ["Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця"]
    
    for cls in SCHOOL_DATA["classes"]:
        print(f"\n КЛАС: {cls}")
        schedule = best_schedule.schedule[cls]
        
        day_idx = 0
        lesson_idx = 1
        
        print(f"--- {days[day_idx]} ---")
        for i, subject in enumerate(schedule):
            subj_info = SCHOOL_DATA["subjects"][subject]
            teacher = SCHOOL_DATA["teachers"][cls][subj_info["teacher_type"]]
            room = subj_info["special_room"] if subj_info["special_room"] else SCHOOL_DATA["rooms"][cls]
            
            # Вивід з вирівнюванням
            print(f"  Урок {lesson_idx}: {subject:<12} | {teacher:<16} | {room}")
            
            lesson_idx += 1
            lessons_today = 5 if day_idx < 4 else 4
            
            if lesson_idx > lessons_today and i < 23:
                day_idx += 1
                lesson_idx = 1
                print(f"--- {days[day_idx]} ---")