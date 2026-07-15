package com.shivansh.org.dao;

import com.shivansh.org.dto.Transaction;
import java.sql.Date;
import java.util.List;

public interface TransactionDao {
    boolean issueBook(int memberId, int bookId, Date issueDate, Date dueDate);
    boolean returnBook(int transactionId, Date returnDate);
    Transaction getTransactionById(int transactionId);
    List<Transaction> getTransactionsByMemberId(int memberId);
    List<Transaction> getAllTransactions();
    // Helper to find an active transaction (issued and not returned) for a member and book
    Transaction getActiveTransaction(int memberId, int bookId);
}
