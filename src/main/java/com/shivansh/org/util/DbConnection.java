package com.shivansh.org.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for establishing MySQL database connections and initializing database tables with
 * seed data. Implements a centralized connection management strategy for the DAO layer.
 *
 * @author Shivansh
 * @version 2.0
 */
public class DbConnection {
  private static final String URL =
      "jdbc:mysql://localhost:3306/smart_library_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
  private static final String USER = "root";
  private static final String PASSWORD = "root";

  /**
   * Obtains a new MySQL database connection. Loads the MySQL JDBC driver and returns a Connection
   * object.
   *
   * @return a new Connection object
   * @throws SQLException if driver loading or connection fails
   */
  public static Connection getMysqlConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new SQLException("MySQL Driver not found", e);
    }
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  /**
   * Initializes the database schema and seeds it with default data. Creates books, members, and
   * transactions tables if they don't exist. Inserts default book catalog (10 titles) and sample
   * members if tables are empty.
   */
  public static void initializeDatabase() {
    String createBooksTable =
        "CREATE TABLE IF NOT EXISTS books ("
            + "book_id INT AUTO_INCREMENT PRIMARY KEY, "
            + "title VARCHAR(255) NOT NULL, "
            + "author VARCHAR(255) NOT NULL, "
            + "isbn VARCHAR(20) DEFAULT NULL, "
            + "genre VARCHAR(100), "
            + "total_copies INT NOT NULL DEFAULT 1, "
            + "available_copies INT NOT NULL DEFAULT 1"
            + ");";

    String createMembersTable =
        "CREATE TABLE IF NOT EXISTS members ("
            + "member_id INT AUTO_INCREMENT PRIMARY KEY, "
            + "first_name VARCHAR(100) NOT NULL, "
            + "last_name VARCHAR(100) NOT NULL, "
            + "email VARCHAR(255) UNIQUE NOT NULL, "
            + "phone VARCHAR(20) DEFAULT NULL, "
            + "password VARCHAR(255) NOT NULL, "
            + "membership_type VARCHAR(50) DEFAULT 'REGULAR'"
            + ");";

    String createTransactionsTable =
        "CREATE TABLE IF NOT EXISTS transactions ("
            + "transaction_id INT AUTO_INCREMENT PRIMARY KEY, "
            + "member_id INT NOT NULL, "
            + "book_id INT NOT NULL, "
            + "issue_date DATE NOT NULL, "
            + "due_date DATE NOT NULL, "
            + "return_date DATE NULL, "
            + "status VARCHAR(50) DEFAULT 'ISSUED', "
            + "FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE, "
            + "FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE"
            + ");";

    try (Connection conn = getMysqlConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute(createBooksTable);
      stmt.execute(createMembersTable);
      stmt.execute(createTransactionsTable);

      // ── Attempt to add isbn column if table existed before upgrade ──
      try {
        stmt.execute("ALTER TABLE books ADD COLUMN isbn VARCHAR(20) DEFAULT NULL AFTER author");
      } catch (SQLException ignored) {
        /* Column already exists */
      }

      // ── Attempt to add phone column if table existed before upgrade ──
      try {
        stmt.execute("ALTER TABLE members ADD COLUMN phone VARCHAR(20) DEFAULT NULL AFTER email");
      } catch (SQLException ignored) {
        /* Column already exists */
      }

      // ── Seed books if table is empty ──
      String checkBooks = "SELECT COUNT(*) FROM books";
      var rs = stmt.executeQuery(checkBooks);
      if (rs.next() && rs.getInt(1) == 0) {
        stmt.execute(
            "INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) VALUES "
                + "('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Fiction', 5, 5), "
                + "('To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'Classic', 3, 3), "
                + "('1984', 'George Orwell', '978-0451524935', 'Dystopian', 4, 4), "
                + "('The Hobbit', 'J.R.R. Tolkien', '978-0547928227', 'Fantasy', 6, 6), "
                + "('Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Romance', 4, 4), "
                + "('The Catcher in the Rye', 'J.D. Salinger', '978-0316769488', 'Fiction', 3, 3), "
                + "('Harry Potter and the Sorcerers Stone', 'J.K. Rowling', '978-0590353427', 'Fantasy', 8, 8), "
                + "('The Lord of the Rings', 'J.R.R. Tolkien', '978-0618640157', 'Fantasy', 5, 5), "
                + "('Brave New World', 'Aldous Huxley', '978-0060850524', 'Dystopian', 3, 3), "
                + "('The Alchemist', 'Paulo Coelho', '978-0062315007', 'Philosophy', 4, 4);");
      }

      // ── Seed members if table is empty ──
      String checkMembers = "SELECT COUNT(*) FROM members";
      var rsMem = stmt.executeQuery(checkMembers);
      if (rsMem.next() && rsMem.getInt(1) == 0) {
        String hashedPass = PasswordUtil.hashPassword("password123");
        stmt.execute(
            "INSERT INTO members (first_name, last_name, email, phone, password, membership_type) VALUES "
                + "('Shivansh', 'Mishra', 'shivnsh01@gmail.com', '9876543210', '"
                + hashedPass
                + "', 'STUDENT'), "
                + "('Jane', 'Smith', 'jane.smith@example.com', '9876543211', '"
                + hashedPass
                + "', 'FACULTY');");
      }

      System.out.println("Database tables checked/initialized successfully.");
    } catch (SQLException e) {
      System.err.println("Database initialization failed: " + e.getMessage());
    }
  }
}
