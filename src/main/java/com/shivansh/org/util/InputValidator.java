package com.shivansh.org.util;

import java.util.regex.Pattern;

public class InputValidator {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  /**
   * Validates that an email is structurally correct.
   *
   * @param email the email string
   * @return true if valid, false otherwise
   */
  public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Validates a password (must be at least 6 characters long).
   *
   * @param password the password
   * @return true if valid, false otherwise
   */
  public static boolean isValidPassword(String password) {
    if (password == null) {
      return false;
    }
    return password.trim().length() >= 6;
  }

  /**
   * Validates if a name is valid (contains only alphabetic characters and spaces).
   *
   * @param name the name (first name or last name)
   * @return true if valid, false otherwise
   */
  public static boolean isValidName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return false;
    }
    // Name should only contain letters and single spaces, no digits or symbols
    return name.trim().matches("^[a-zA-Z\\s]{2,50}$");
  }

  /**
   * Validates that a string is a positive integer.
   *
   * @param numberStr the string representing the number
   * @return true if it's a valid positive integer (>= 1)
   */
  public static boolean isPositiveInteger(String numberStr) {
    if (numberStr == null) {
      return false;
    }
    try {
      int num = Integer.parseInt(numberStr.trim());
      return num > 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
