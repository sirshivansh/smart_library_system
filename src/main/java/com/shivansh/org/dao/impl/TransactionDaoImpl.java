package com.shivansh.org.dao.impl;

import com.shivansh.org.dao.TransactionDao;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.util.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDaoImpl implements TransactionDao {

    @Override
    public boolean issueBook(int memberId, int bookId, Date issueDate, Date dueDate) {
        String insertTxSql = "INSERT INTO transactions (member_id, book_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')";
        String updateBookSql = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";

        Connection conn = null;
        try {
            conn = DbConnection.getMysqlConnection();
            conn.setAutoCommit(false); // start transaction

            // 1. Decrement available copies
            try (PreparedStatement updatePs = conn.prepareStatement(updateBookSql)) {
                updatePs.setInt(1, bookId);
                int updatedRows = updatePs.executeUpdate();
                if (updatedRows == 0) {
                    conn.rollback();
                    return false; // No copies available
                }
            }

            // 2. Insert transaction record
            try (PreparedStatement insertPs = conn.prepareStatement(insertTxSql)) {
                insertPs.setInt(1, memberId);
                insertPs.setInt(2, bookId);
                insertPs.setDate(3, issueDate);
                insertPs.setDate(4, dueDate);
                insertPs.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error issuing book: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean returnBook(int transactionId, Date returnDate) {
        String selectTxSql = "SELECT book_id, status FROM transactions WHERE transaction_id = ?";
        String updateTxSql = "UPDATE transactions SET return_date = ?, status = 'RETURNED' WHERE transaction_id = ?";
        String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";

        Connection conn = null;
        try {
            conn = DbConnection.getMysqlConnection();
            conn.setAutoCommit(false); // start transaction

            int bookId = -1;
            // 1. Get book ID and verify transaction status
            try (PreparedStatement selectPs = conn.prepareStatement(selectTxSql)) {
                selectPs.setInt(1, transactionId);
                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        if ("RETURNED".equals(rs.getString("status"))) {
                            conn.rollback();
                            return false; // Already returned
                        }
                        bookId = rs.getInt("book_id");
                    } else {
                        conn.rollback();
                        return false; // Transaction not found
                    }
                }
            }

            // 2. Update transaction status
            try (PreparedStatement updateTxPs = conn.prepareStatement(updateTxSql)) {
                updateTxPs.setDate(1, returnDate);
                updateTxPs.setInt(2, transactionId);
                updateTxPs.executeUpdate();
            }

            // 3. Increment book availability
            try (PreparedStatement updateBookPs = conn.prepareStatement(updateBookSql)) {
                updateBookPs.setInt(1, bookId);
                updateBookPs.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT t.*, b.title AS book_title, CONCAT(m.first_name, ' ', m.last_name) AS member_name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.transaction_id = ?";
        try (Connection conn = DbConnection.getMysqlConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("member_id"),
                            rs.getInt("book_id"),
                            rs.getDate("issue_date"),
                            rs.getDate("due_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")
                    );
                    tx.setBookTitle(rs.getString("book_title"));
                    tx.setMemberName(rs.getString("member_name"));
                    return tx;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByMemberId(int memberId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, CONCAT(m.first_name, ' ', m.last_name) AS member_name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.member_id = ? " +
                     "ORDER BY t.issue_date DESC";
        try (Connection conn = DbConnection.getMysqlConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("member_id"),
                            rs.getInt("book_id"),
                            rs.getDate("issue_date"),
                            rs.getDate("due_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")
                    );
                    tx.setBookTitle(rs.getString("book_title"));
                    tx.setMemberName(rs.getString("member_name"));
                    transactions.add(tx);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching member transactions: " + e.getMessage());
        }
        return transactions;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title AS book_title, CONCAT(m.first_name, ' ', m.last_name) AS member_name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "ORDER BY t.issue_date DESC";
        try (Connection conn = DbConnection.getMysqlConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaction tx = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("member_id"),
                        rs.getInt("book_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getString("status")
                );
                tx.setBookTitle(rs.getString("book_title"));
                tx.setMemberName(rs.getString("member_name"));
                transactions.add(tx);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all transactions: " + e.getMessage());
        }
        return transactions;
    }

    @Override
    public Transaction getActiveTransaction(int memberId, int bookId) {
        String sql = "SELECT t.*, b.title AS book_title, CONCAT(m.first_name, ' ', m.last_name) AS member_name " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.member_id = ? AND t.book_id = ? AND t.status = 'ISSUED'";
        try (Connection conn = DbConnection.getMysqlConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("member_id"),
                            rs.getInt("book_id"),
                            rs.getDate("issue_date"),
                            rs.getDate("due_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")
                    );
                    tx.setBookTitle(rs.getString("book_title"));
                    tx.setMemberName(rs.getString("member_name"));
                    return tx;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active transaction: " + e.getMessage());
        }
        return null;
    }
}
