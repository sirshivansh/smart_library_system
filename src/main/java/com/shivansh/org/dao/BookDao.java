package com.shivansh.org.dao;

import com.shivansh.org.dto.Book;
import java.util.List;

public interface BookDao {
    boolean addBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(int bookId);
    Book getBookById(int bookId);
    List<Book> getAllBooks();
    List<Book> searchBooks(String query);
}
