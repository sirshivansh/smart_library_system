package com.shivansh.org.service.impl;

import com.shivansh.org.dao.BookDao;
import com.shivansh.org.dao.MemberDao;
import com.shivansh.org.dao.TransactionDao;
import com.shivansh.org.dao.impl.BookDaoImpl;
import com.shivansh.org.dao.impl.MemberDaoImpl;
import com.shivansh.org.dao.impl.TransactionDaoImpl;
import com.shivansh.org.dto.Book;
import com.shivansh.org.dto.Member;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.service.TransactionService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionDao transactionDao = new TransactionDaoImpl();
    private final BookDao bookDao = new BookDaoImpl();
    private final MemberDao memberDao = new MemberDaoImpl();

    @Override
    public boolean borrowBook(int memberId, int bookId) {
        // 1. Check if book exists and has available copies
        Book book = bookDao.getBookById(bookId);
        if (book == null) {
            System.out.println("Book with ID " + bookId + " does not exist.");
            return false;
        }
        if (book.getAvailableCopies() <= 0) {
            System.out.println("Book '" + book.getTitle() + "' is currently out of stock.");
            return false;
        }

        // 2. Check if member has already borrowed this book and hasn't returned it yet
        Transaction activeTx = transactionDao.getActiveTransaction(memberId, bookId);
        if (activeTx != null) {
            System.out.println("You have already issued this book and not returned it yet (Transaction ID: " + activeTx.getTransactionId() + ").");
            return false;
        }

        // Check borrowing limit based on membership type
        Member member = memberDao.getMemberById(memberId);
        if (member == null) {
            System.out.println("Member does not exist.");
            return false;
        }

        List<Transaction> memberTxs = transactionDao.getTransactionsByMemberId(memberId);
        long activeCount = memberTxs.stream()
                .filter(t -> "ISSUED".equals(t.getStatus()))
                .count();

        int limit = 3; // Regular limit
        String type = member.getMembershipType() != null ? member.getMembershipType().toUpperCase() : "REGULAR";
        if ("FACULTY".equals(type)) {
            limit = 10;
        } else if ("STUDENT".equals(type)) {
            limit = 5;
        }

        if (activeCount >= limit) {
            System.out.println("Borrowing limit reached! As a " + type + " member, you can borrow a maximum of " + limit + " books at a time. Currently borrowed: " + activeCount);
            return false;
        }

        // 3. Issue the book
        Date issueDate = Date.valueOf(LocalDate.now());
        Date dueDate = Date.valueOf(LocalDate.now().plusDays(14)); // 14-day borrowing period
        
        return transactionDao.issueBook(memberId, bookId, issueDate, dueDate);
    }

    @Override
    public boolean returnBook(int memberId, int bookId) {
        // 1. Find active transaction
        Transaction activeTx = transactionDao.getActiveTransaction(memberId, bookId);
        if (activeTx == null) {
            System.out.println("No active borrowing record found for this member and book.");
            return false;
        }

        // 2. Return the book
        Date returnDate = Date.valueOf(LocalDate.now());
        boolean success = transactionDao.returnBook(activeTx.getTransactionId(), returnDate);
        if (success) {
            activeTx.setReturnDate(returnDate);
            double fine = activeTx.getCalculatedFine();
            if (fine > 0) {
                System.out.printf("NOTICE: This book was returned late! An overdue fine of %.2f rupees has been recorded.%n", fine);
            }
        }
        return success;
    }

    @Override
    public Transaction getTransactionById(int transactionId) {
        return transactionDao.getTransactionById(transactionId);
    }

    @Override
    public List<Transaction> getTransactionsByMemberId(int memberId) {
        return transactionDao.getTransactionsByMemberId(memberId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }
}
