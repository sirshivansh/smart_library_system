package com.shivansh.org.exception;

/**
 * Exception thrown when a requested member is not found in the system.
 *
 * @author Shivansh
 * @version 1.0
 */
public class MemberNotFoundException extends LibraryException {

  public MemberNotFoundException(int memberId) {
    super("Member with ID " + memberId + " was not found in the system.");
  }

  public MemberNotFoundException(String message) {
    super(message);
  }
}
