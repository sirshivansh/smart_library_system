package com.shivansh.org.service.impl;

import com.shivansh.org.dao.BookDao;
import com.shivansh.org.dao.impl.BookDaoImpl;
import com.shivansh.org.dto.Book;
import com.shivansh.org.service.BookService;
import java.util.List;

public class BookServiceImpl implements BookService {
  private final BookDao bookDao = new BookDaoImpl();

  @Override
  public boolean addBook(Book book) {
    if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
      System.out.println("Book title cannot be empty!");
      return false;
    }
    if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
      System.out.println("Book author cannot be empty!");
      return false;
    }
    if (book.getTotalCopies() <= 0) {
      System.out.println("Book total copies must be at least 1!");
      return false;
    }
    return bookDao.addBook(book);
  }

  @Override
  public boolean updateBook(Book book) {
    if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
      System.out.println("Book title cannot be empty!");
      return false;
    }
    if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
      System.out.println("Book author cannot be empty!");
      return false;
    }
    if (book.getTotalCopies() < 0) {
      System.out.println("Book total copies cannot be negative!");
      return false;
    }
    if (book.getAvailableCopies() < 0) {
      System.out.println("Book available copies cannot be negative!");
      return false;
    }
    if (book.getAvailableCopies() > book.getTotalCopies()) {
      System.out.println("Book available copies cannot exceed total copies!");
      return false;
    }
    return bookDao.updateBook(book);
  }

  @Override
  public boolean deleteBook(int bookId) {
    return bookDao.deleteBook(bookId);
  }

  @Override
  public Book getBookById(int bookId) {
    return bookDao.getBookById(bookId);
  }

  @Override
  public List<Book> getAllBooks() {
    return bookDao.getAllBooks();
  }

  @Override
  public List<Book> searchBooks(String query) {
    return bookDao.searchBooks(query);
  }
}
