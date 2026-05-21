package com.library;
import javax.swing.*;
import java.awt.*;

public class LibraryUI extends JFrame {
    private LibraryRepository repository = new LibraryRepository();
    private DefaultListModel<Book> listModel = new DefaultListModel<>();
    private JList<Book> bookList = new JList<>(listModel);

    public LibraryUI() {
        setTitle("ІС «Бібліотека»");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Завантаження даних при старті
        repository.loadFromFile();
        refreshList();

        // Налаштування списку
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(bookList);
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Додати");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Обробники подій ---

        // ДОДАВАННЯ
        addButton.addActionListener(e -> {
            showBookDialog(null);
        });

        // РЕДАГУВАННЯ
        editButton.addActionListener(e -> {
            int selectedIndex = bookList.getSelectedIndex();
            if (selectedIndex != -1) {
                Book selectedBook = repository.getBooks().get(selectedIndex);
                showBookDialog(selectedBook);
            } else {
                JOptionPane.showMessageDialog(this, "Оберіть книгу для редагування!", "Увага", JOptionPane.WARNING_MESSAGE);
            }
        });

        // ВИДАЛЕННЯ
        deleteButton.addActionListener(e -> {
            int selectedIndex = bookList.getSelectedIndex();
            if (selectedIndex != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Ви впевнені, що хочете видалити книгу?", "Підтвердження", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    repository.removeBook(selectedIndex);
                    repository.saveToFile();
                    refreshList();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Оберіть книгу для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Метод оновлення списку на екрані
    private void refreshList() {
        listModel.clear();
        for (Book b : repository.getBooks()) {
            listModel.addElement(b);
        }
    }

    // Універсальне вікно для Додавання та Редагування
    private void showBookDialog(Book bookToEdit) {
        JTextField titleField = new JTextField(bookToEdit != null ? bookToEdit.getTitle() : "");
        JTextField authorField = new JTextField(bookToEdit != null ? bookToEdit.getAuthor() : "");
        JTextField pagesField = new JTextField(bookToEdit != null ? String.valueOf(bookToEdit.getPages()) : "");
        JTextField yearField = new JTextField(bookToEdit != null ? String.valueOf(bookToEdit.getYear()) : "");

        // Видалили specificField, бо тепер у нас є вибір зі списку

        String[] types = {"Художня книга", "Підручник", "Наукова книга"};
        String[] genres = {"Детектив", "Фентезі", "Роман", "Пригоди", "Жахи"};
        String[] subjects = {"Українська мова", "Математика", "Історія", "Фізика", "Програмування"};
        String[] areas = {"Кібернетика", "Біологія", "Астрономія", "Економіка", "Юриспруденція"};

        JComboBox<String> typeBox = new JComboBox<>(types);
        DefaultComboBoxModel<String> specificModel = new DefaultComboBoxModel<>(genres);
        JComboBox<String> specificBox = new JComboBox<>(specificModel);
        JLabel specificLabel = new JLabel("Оберіть жанр:");

        // Зміна списку залежно від типу книги
        typeBox.addActionListener(e -> {
            int selectedIndex = typeBox.getSelectedIndex();
            switch (selectedIndex) {
                case 0 -> { // Художня
                    specificModel.removeAllElements();
                    for (String s : genres) specificModel.addElement(s);
                    specificLabel.setText("Оберіть жанр:");
                }
                case 1 -> { // Підручник
                    specificModel.removeAllElements();
                    for (String s : subjects) specificModel.addElement(s);
                    specificLabel.setText("Оберіть предмет:");
                }
                case 2 -> { // Наукова
                    specificModel.removeAllElements();
                    for (String s : areas) specificModel.addElement(s);
                    specificLabel.setText("Оберіть галузь:");
                }
            }
        });

        // Якщо це редагування, визначаємо тип і встановлюємо значення у specificBox
        if (bookToEdit != null) {
            typeBox.setEnabled(false); // Забороняємо змінювати тип при редагуванні
            if (bookToEdit instanceof FictionBook) {
                typeBox.setSelectedIndex(0);
                specificBox.setSelectedItem(((FictionBook) bookToEdit).getGenre());
            } else if (bookToEdit instanceof Textbook) {
                typeBox.setSelectedIndex(1);
                specificBox.setSelectedItem(((Textbook) bookToEdit).getSubject());
            } else if (bookToEdit instanceof ScienceBook) {
                typeBox.setSelectedIndex(2);
                specificBox.setSelectedItem(((ScienceBook) bookToEdit).getResearchArea());
            }
        }

        // ТУТ БУЛА ПОМИЛКА: додаємо specificLabel та specificBox замість specificField
        Object[] message = {
                "Тип книги:", typeBox,
                "Назва:", titleField,
                "Автор:", authorField,
                "Кількість сторінок:", pagesField,
                "Рік видання:", yearField,
                specificLabel, specificBox
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                bookToEdit == null ? "Додати нову книгу" : "Редагувати книгу",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                // Застосовуємо метод capitalize для першої великої літери
                String title = capitalize(titleField.getText());
                String author = capitalize(authorField.getText());

                // Отримуємо значення з випадаючого списку (вводити вручну більше не треба)
                String specific = (String) specificBox.getSelectedItem();

                int pages = Integer.parseInt(pagesField.getText());
                int year = Integer.parseInt(yearField.getText());

                if (year > 2026) {
                    JOptionPane.showMessageDialog(this, "Рік видання має бути до 2026!", "Помилка вводу", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (pages <= 0) {
                    JOptionPane.showMessageDialog(this, "Кількість сторінок має бути більшою за нуль!", "Помилка вводу", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (bookToEdit == null) {
                    // Створення нової книги
                    Book newBook = switch (typeBox.getSelectedIndex()) {
                        case 0 -> new FictionBook(title, author, pages, year, specific);
                        case 1 -> new Textbook(title, author, pages, year, specific);
                        case 2 -> new ScienceBook(title, author, pages, year, specific);
                        default -> new Book(title, author, pages, year);
                    };
                    repository.addBook(newBook);
                } else {
                    // Оновлення існуючої книги
                    bookToEdit.setTitle(title);
                    bookToEdit.setAuthor(author);
                    bookToEdit.setPages(pages);
                    bookToEdit.setYear(year);

                    if (bookToEdit instanceof FictionBook) ((FictionBook) bookToEdit).setGenre(specific);
                    else if (bookToEdit instanceof Textbook) ((Textbook) bookToEdit).setSubject(specific);
                    else if (bookToEdit instanceof ScienceBook) ((ScienceBook) bookToEdit).setResearchArea(specific);
                }

                repository.saveToFile();
                refreshList();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Помилка вводу! Сторінки та рік мають бути числами.", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String capitalize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        text = text.trim(); // Видаляємо зайві пробіли по краях
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    public static void main(String[] args) {
        // Запуск Swing у правильному потоці
        SwingUtilities.invokeLater(() -> new LibraryUI().setVisible(true));
    }
}