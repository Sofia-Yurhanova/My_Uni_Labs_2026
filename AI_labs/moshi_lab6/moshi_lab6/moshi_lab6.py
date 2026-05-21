import tkinter as tk
import random
from tkinter import messagebox

class TicTacToeTreeGame:
    def __init__(self, root): # Виправлено: додано __
        self.root = root
        self.root.title("Хрестики-нулики (Minimax AI)")
        self.board = [' ' for _ in range(9)] 
        self.current_player = 'X'
        self.buttons = []
        self.create_gui()

    # Етап 5: Реалізація візуалізації та взаємодії з мишею
    def create_gui(self):
        # Панель для додаткових кнопок (опціонально)
        top_frame = tk.Frame(self.root)
        top_frame.grid(row=0, column=0, columnspan=3, pady=5)
        
        btn_reset = tk.Button(top_frame, text="Почати спочатку", command=self.reset_game)
        btn_reset.pack(side=tk.LEFT, padx=5)
        
        btn_exit = tk.Button(top_frame, text="Вихід", command=self.root.quit)
        btn_exit.pack(side=tk.LEFT, padx=5)

        # Сітка ігрового поля
        for i in range(9):
            btn = tk.Button(self.root, text=' ', font=('Arial', 24, 'bold'), width=5, height=2,
                            command=lambda i=i: self.user_click(i))
            btn.grid(row=(i//3) + 1, column=i%3, padx=5, pady=5)
            self.buttons.append(btn)

    def user_click(self, index):
        if self.board[index] == ' ' and self.current_player == 'X':
            self.make_move(index, 'X')
            
            # Якщо після ходу гравця гра не закінчилася, ходить комп'ютер
            if not self.check_winner('X') and not self.is_draw():
                self.current_player = 'O'
                # Невелика затримка, щоб хід комп'ютера не був миттєвим
                self.root.after(500, self.computer_move)

    def make_move(self, index, player):
        self.board[index] = player
        color = "blue" if player == 'X' else "red"
        self.buttons[index].config(text=player, state=tk.DISABLED, disabledforeground=color)
        
        if self.check_winner(player):
            self.root.after(100, lambda: self.end_game(f"Переміг {player}!"))
        elif self.is_draw():
            self.root.after(100, lambda: self.end_game("Нічия!"))

    # Етап 6: Безпрограшна гра комп'ютера
    def computer_move(self):
        best_score = -float('inf')
        best_move = None
        
        for i in range(9):
            if self.board[i] == ' ':
                self.board[i] = 'O'
                score = self.minimax(self.board, 0, False)
                self.board[i] = ' '
                
                if score > best_score:
                    best_score = score
                    best_move = i
                        
        if best_move is not None:
            self.make_move(best_move, 'O')
            self.current_player = 'X'

    def minimax(self, board, depth, is_maximizing):
        if self.check_winner('O'):
            return 10 - depth 
        if self.check_winner('X'):
            return depth - 10 
        if self.is_draw():
            return 0 

        if is_maximizing:
            best_score = -float('inf')
            for i in range(9):
                if board[i] == ' ':
                    board[i] = 'O'
                    score = self.minimax(board, depth + 1, False)
                    board[i] = ' '
                    best_score = max(score, best_score)
            return best_score
        else:
            best_score = float('inf')
            for i in range(9):
                if board[i] == ' ':
                    board[i] = 'X'
                    score = self.minimax(board, depth + 1, True)
                    board[i] = ' '
                    best_score = min(score, best_score)
            return best_score

    def check_winner(self, player):
        win_conditions = [
            (0, 1, 2), (3, 4, 5), (6, 7, 8),
            (0, 3, 6), (1, 4, 7), (2, 5, 8), 
            (0, 4, 8), (2, 4, 6)            
        ]
        for condition in win_conditions:
            if self.board[condition[0]] == self.board[condition[1]] == self.board[condition[2]] == player:
                return True
        return False

    def is_draw(self):
        return ' ' not in self.board

    def end_game(self, message):
        messagebox.showinfo("Кінець гри", message)
        self.reset_game()

    def reset_game(self):
        self.board = [' ' for _ in range(9)]
        for btn in self.buttons:
            btn.config(text=' ', state=tk.NORMAL)
        self.current_player = 'X'

    def computer_move(self):
       
        difficulty = 1.0

        if random.random() > difficulty:
            available_moves = [i for i, x in enumerate(self.board) if x == ' ']
            if available_moves:
                best_move = random.choice(available_moves)
        else:
            best_score = -float('inf')
            best_move = None
            
            for i in range(9):
                if self.board[i] == ' ':
                    self.board[i] = 'O'
                    score = self.minimax(self.board, 0, False)
                    self.board[i] = ' '
                    
                    if score > best_score:
                        best_score = score
                        best_move = i
                        
        if best_move is not None:
            self.make_move(best_move, 'O')
            self.current_player = 'X'

if __name__ == "__main__":
    root = tk.Tk()
    app = TicTacToeTreeGame(root)
    root.mainloop()