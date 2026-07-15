package com.shivansh.org.exception;

/**
 * Exception thrown when input data fails validation checks,
 * such as invalid email format, weak passwords, or invalid names.
 * 
 * @author Shivansh
 * @version 1.0
 */
public class ValidationException extends LibraryException {

    public ValidationException(String message) {
        super(message);
    }
}
