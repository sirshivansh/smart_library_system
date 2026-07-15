package com.shivansh.org.service;

import com.shivansh.org.dto.Book;
import java.util.List;

public interface BookService {
    boolean addBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(int bookId);
    Book getBookById(int bookId);
    List<Book> getAllBooks();
    List<Book> searchBooks(String query);
}
