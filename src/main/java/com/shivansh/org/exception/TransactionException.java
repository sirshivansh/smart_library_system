package com.shivansh.org.exception;

/**
 * Exception thrown when a borrowing transaction violates business rules,
 * such as exceeding checkout limits or attempting to borrow an unavailable book.
 * 
 * @author Shivansh
 * @version 1.0
 */
public class TransactionException extends LibraryException {

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
