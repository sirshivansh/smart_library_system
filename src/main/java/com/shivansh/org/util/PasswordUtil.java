package com.shivansh.org.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

  /**
   * Hashes a plaintext password using SHA-256 algorithm.
   *
   * @param password the plaintext password
   * @return the hexadecimal hash string, or null if hashing fails
   */
  public static String hashPassword(String password) {
    if (password == null) {
      return null;
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(password.getBytes());
      return bytesToHex(encodedhash);
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Error initializing password hashing algorithm: " + e.getMessage());
      return password; // Fallback to plaintext if error, but SHA-256 should always be available in
      // JVM
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
