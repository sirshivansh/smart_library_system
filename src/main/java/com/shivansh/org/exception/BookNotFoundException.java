package com.shivansh.org.exception;

/**
 * Exception thrown when a requested book is not found in the library catalog.
 *
 * @author Shivansh
 * @version 1.0
 */
public class BookNotFoundException extends LibraryException {

  public BookNotFoundException(int bookId) {
    super("Book with ID " + bookId + " was not found in the catalog.");
  }

  public BookNotFoundException(String message) {
    super(message);
  }
}
