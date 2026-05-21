package com.library;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryRepository {
    private List<Book> books = new ArrayList<>();
    private static final String FILE_NAME = "library.dat";

    public void addBook(Book book) { books.add(book); }
    public void removeBook(int index) {
        if (index >= 0 && index < books.size()) {
            books.remove(index);
        }
    }    public List<Book> getBooks() { return books; }

    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(books);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
                books = (List<Book>) ois.readObject();
            } catch (Exception e) {
                System.out.println("Помилка читання файлу або файл порожній. Створено нову базу.");
            }
        }
    }
}