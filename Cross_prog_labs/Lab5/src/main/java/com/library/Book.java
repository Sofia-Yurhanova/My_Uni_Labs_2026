package com.library;
import java.io.Serializable;

// Базовий клас (Serializable потрібен для збереження у файл)
public class Book implements Serializable {
    private String title;
    private String author;
    private int pages;
    private int year;

    public Book(String title, String author, int pages, int year) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.year = year;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    @Override
    public String toString() {
        return String.format("%s: %s (%d р.)", author, title, year);
    }
}

class FictionBook extends Book {
    private String genre;

    public FictionBook(String title, String author, int pages, int year, String genre) {
        super(title, author, pages, year);
        this.genre = genre;
    }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public String toString() {
        return String.format("[Художня] '%s', %s (%d р., %d стор.) - Жанр: %s",
                getTitle(), getAuthor(), getYear(), getPages(), genre);
    }
}

class Textbook extends Book {
    private String subject;

    public Textbook(String title, String author, int pages, int year, String subject) {
        super(title, author, pages, year);
        this.subject = subject;
    }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    @Override
    public String toString() {
        return String.format("[Підручник] '%s', %s (%d р., %d стор.) - Предмет: %s",
                getTitle(), getAuthor(), getYear(), getPages(), subject);
    }
}

class ScienceBook extends Book {
    private String researchArea;

    public ScienceBook(String title, String author, int pages, int year, String researchArea) {
        super(title, author, pages, year);
        this.researchArea = researchArea;
    }
    public String getResearchArea() { return researchArea; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

    @Override
    public String toString() {
        return String.format("[Наукова] '%s', %s (%d р., %d стор.) - Галузь: %s",
                getTitle(), getAuthor(), getYear(), getPages(), researchArea);
    }
}