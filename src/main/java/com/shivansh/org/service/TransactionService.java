package com.shivansh.org.service;

import com.shivansh.org.dto.Transaction;
import java.util.List;

public interface TransactionService {
  boolean borrowBook(int memberId, int bookId);

  boolean returnBook(int memberId, int bookId);

  Transaction getTransactionById(int transactionId);

  List<Transaction> getTransactionsByMemberId(int memberId);

  List<Transaction> getAllTransactions();
}
