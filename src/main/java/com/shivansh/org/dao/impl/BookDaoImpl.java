package com.shivansh.org.dao.impl;

import com.shivansh.org.dao.BookDao;
import com.shivansh.org.dto.Book;
import com.shivansh.org.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDaoImpl implements BookDao {

  @Override
  public boolean addBook(Book book) {
    String sql =
        "INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, book.getTitle());
      ps.setString(2, book.getAuthor());
      ps.setString(3, book.getIsbn());
      ps.setString(4, book.getGenre());
      ps.setInt(5, book.getTotalCopies());
      ps.setInt(6, book.getAvailableCopies());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error adding book: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean updateBook(Book book) {
    String sql =
        "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, book.getTitle());
      ps.setString(2, book.getAuthor());
      ps.setString(3, book.getIsbn());
      ps.setString(4, book.getGenre());
      ps.setInt(5, book.getTotalCopies());
      ps.setInt(6, book.getAvailableCopies());
      ps.setInt(7, book.getBookId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error updating book: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean deleteBook(int bookId) {
    String sql = "DELETE FROM books WHERE book_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, bookId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error deleting book: " + e.getMessage());
      return false;
    }
  }

  @Override
  public Book getBookById(int bookId) {
    String sql = "SELECT * FROM books WHERE book_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, bookId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Book(
              rs.getInt("book_id"),
              rs.getString("title"),
              rs.getString("author"),
              rs.getString("isbn"),
              rs.getString("genre"),
              rs.getInt("total_copies"),
              rs.getInt("available_copies"));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching book by ID: " + e.getMessage());
    }
    return null;
  }

  @Override
  public List<Book> getAllBooks() {
    List<Book> books = new ArrayList<>();
    String sql = "SELECT * FROM books";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        books.add(
            new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("genre"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching all books: " + e.getMessage());
    }
    return books;
  }

  @Override
  public List<Book> searchBooks(String query) {
    List<Book> books = new ArrayList<>();
    String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      String wildcardQuery = "%" + query + "%";
      ps.setString(1, wildcardQuery);
      ps.setString(2, wildcardQuery);
      ps.setString(3, wildcardQuery);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          books.add(
              new Book(
                  rs.getInt("book_id"),
                  rs.getString("title"),
                  rs.getString("author"),
                  rs.getString("isbn"),
                  rs.getString("genre"),
                  rs.getInt("total_copies"),
                  rs.getInt("available_copies")));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error searching books: " + e.getMessage());
    }
    return books;
  }
}
