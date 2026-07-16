package com.shivansh.org.controller;

import com.shivansh.org.dto.Book;
import com.shivansh.org.dto.Member;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.exception.TransactionException;
import com.shivansh.org.service.BookService;
import com.shivansh.org.service.MemberService;
import com.shivansh.org.service.TransactionService;
import com.shivansh.org.service.impl.BookServiceImpl;
import com.shivansh.org.service.impl.MemberServiceImpl;
import com.shivansh.org.service.impl.TransactionServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for Transaction-related operations. Handles book borrowing, returning,
 * transaction history, overdue reporting, and library statistics aggregation.
 *
 * @author Shivansh
 * @version 1.0
 */
public class TransactionController {

  private final TransactionService transactionService;
  private final BookService bookService;
  private final MemberService memberService;

  public TransactionController() {
    this.transactionService = new TransactionServiceImpl();
    this.bookService = new BookServiceImpl();
    this.memberService = new MemberServiceImpl();
  }

  /**
   * Issues a book to a member (borrow operation).
   *
   * @param memberId the member ID
   * @param bookId the book ID
   * @return true if issued successfully
   * @throws TransactionException if business rules are violated
   */
  public boolean borrowBook(int memberId, int bookId) throws TransactionException {
    boolean result = transactionService.borrowBook(memberId, bookId);
    if (!result) {
      throw new TransactionException(
          "Failed to issue book. Check availability and borrowing limits.");
    }
    return true;
  }

  /**
   * Returns a book from a member.
   *
   * @param memberId the member ID
   * @param bookId the book ID
   * @return the calculated fine (0.0 if no fine)
   * @throws TransactionException if no active borrow record exists
   */
  public double returnBook(int memberId, int bookId) throws TransactionException {
    // Get the active transaction before returning (to calculate fine)
    List<Transaction> memberTxs = transactionService.getTransactionsByMemberId(memberId);
    Transaction activeTx =
        memberTxs.stream()
            .filter(t -> "ISSUED".equals(t.getStatus()) && t.getBookId() == bookId)
            .findFirst()
            .orElse(null);

    boolean result = transactionService.returnBook(memberId, bookId);
    if (!result) {
      throw new TransactionException(
          "Return failed. Ensure you have an active borrow for this book.");
    }

    // Calculate fine for the returned transaction
    if (activeTx != null) {
      activeTx.setReturnDate(java.sql.Date.valueOf(LocalDate.now()));
      return activeTx.getCalculatedFine();
    }
    return 0.0;
  }

  /**
   * Retrieves all transactions for a specific member.
   *
   * @param memberId the member ID
   * @return list of transactions
   */
  public List<Transaction> getMemberTransactions(int memberId) {
    return transactionService.getTransactionsByMemberId(memberId);
  }

  /**
   * Retrieves all transactions in the system.
   *
   * @return list of all transactions
   */
  public List<Transaction> getAllTransactions() {
    return transactionService.getAllTransactions();
  }

  /**
   * Retrieves all currently overdue transactions (ISSUED status with due date in the past). This is
   * used by the admin Overdue Report feature.
   *
   * @return list of overdue transactions
   */
  public List<Transaction> getOverdueTransactions() {
    return transactionService.getAllTransactions().stream()
        .filter(t -> "ISSUED".equals(t.getStatus()))
        .filter(
            t -> t.getDueDate() != null && t.getDueDate().toLocalDate().isBefore(LocalDate.now()))
        .collect(Collectors.toList());
  }

  /**
   * Computes library-wide statistics for the admin dashboard. Returns an array: [totalTitles,
   * totalCopies, availableCopies, totalMembers, activeBorrows, overdueCount, totalFines]
   *
   * @return double array of statistics
   */
  public double[] getLibraryStatistics() {
    List<Book> books = bookService.getAllBooks();
    List<Member> members = memberService.getAllMembers();
    List<Transaction> transactions = transactionService.getAllTransactions();

    double totalTitles = books.size();
    double totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
    double availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
    double totalMembers = members.size();
    double activeBorrows =
        transactions.stream().filter(t -> "ISSUED".equals(t.getStatus())).count();

    double overdueCount =
        transactions.stream()
            .filter(t -> "ISSUED".equals(t.getStatus()))
            .filter(
                t ->
                    t.getDueDate() != null
                        && t.getDueDate().toLocalDate().isBefore(LocalDate.now()))
            .count();

    double totalFines = transactions.stream().mapToDouble(Transaction::getCalculatedFine).sum();

    return new double[] {
      totalTitles,
      totalCopies,
      availableCopies,
      totalMembers,
      activeBorrows,
      overdueCount,
      totalFines
    };
  }
}
