package com.shivansh.org.exception;

/**
 * Base exception class for the Smart Library Management System. All custom application exceptions
 * extend this class to provide a unified exception hierarchy for the MVC architecture.
 *
 * @author Shivansh
 * @version 1.0
 */
public class LibraryException extends Exception {

  public LibraryException(String message) {
    super(message);
  }

  public LibraryException(String message, Throwable cause) {
    super(message, cause);
  }
}
