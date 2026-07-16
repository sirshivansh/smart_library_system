package com.shivansh.org.dto;

/**
 * Data Transfer Object representing a Book entity in the Smart Library System. Maps to the 'books'
 * table in the MySQL database.
 *
 * @author Shivansh
 * @version 2.0
 */
public class Book {
  private int bookId;
  private String title;
  private String author;
  private String isbn;
  private String genre;
  private int totalCopies;
  private int availableCopies;

  /** Default no-args constructor */
  public Book() {}

  /** Constructor without ID and ISBN (for quick creation) */
  public Book(String title, String author, String genre, int totalCopies, int availableCopies) {
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.totalCopies = totalCopies;
    this.availableCopies = availableCopies;
  }

  /** Full constructor without ISBN */
  public Book(
      int bookId, String title, String author, String genre, int totalCopies, int availableCopies) {
    this.bookId = bookId;
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.totalCopies = totalCopies;
    this.availableCopies = availableCopies;
  }

  /** Full constructor with ISBN */
  public Book(
      int bookId,
      String title,
      String author,
      String isbn,
      String genre,
      int totalCopies,
      int availableCopies) {
    this.bookId = bookId;
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.genre = genre;
    this.totalCopies = totalCopies;
    this.availableCopies = availableCopies;
  }

  // ── Getters and Setters ──────────────────────────────────────

  public int getBookId() {
    return bookId;
  }

  public void setBookId(int bookId) {
    this.bookId = bookId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public int getTotalCopies() {
    return totalCopies;
  }

  public void setTotalCopies(int totalCopies) {
    this.totalCopies = totalCopies;
  }

  public int getAvailableCopies() {
    return availableCopies;
  }

  public void setAvailableCopies(int availableCopies) {
    this.availableCopies = availableCopies;
  }

  @Override
  public String toString() {
    return "Book [ID="
        + bookId
        + ", Title="
        + title
        + ", Author="
        + author
        + (isbn != null ? ", ISBN=" + isbn : "")
        + ", Genre="
        + genre
        + ", Total="
        + totalCopies
        + ", Available="
        + availableCopies
        + "]";
  }
}
