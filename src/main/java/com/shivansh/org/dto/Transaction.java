package com.shivansh.org.dto;

import java.sql.Date;

public class Transaction {
  private int transactionId;
  private int memberId;
  private int bookId;
  private Date issueDate;
  private Date dueDate;
  private Date returnDate;
  private String status;

  // Helper fields for display
  private String bookTitle;
  private String memberName;

  public Transaction() {}

  public Transaction(
      int memberId, int bookId, Date issueDate, Date dueDate, Date returnDate, String status) {
    this.memberId = memberId;
    this.bookId = bookId;
    this.issueDate = issueDate;
    this.dueDate = dueDate;
    this.returnDate = returnDate;
    this.status = status;
  }

  public Transaction(
      int transactionId,
      int memberId,
      int bookId,
      Date issueDate,
      Date dueDate,
      Date returnDate,
      String status) {
    this.transactionId = transactionId;
    this.memberId = memberId;
    this.bookId = bookId;
    this.issueDate = issueDate;
    this.dueDate = dueDate;
    this.returnDate = returnDate;
    this.status = status;
  }

  public int getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(int transactionId) {
    this.transactionId = transactionId;
  }

  public int getMemberId() {
    return memberId;
  }

  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }

  public int getBookId() {
    return bookId;
  }

  public void setBookId(int bookId) {
    this.bookId = bookId;
  }

  public Date getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(Date issueDate) {
    this.issueDate = issueDate;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(Date returnDate) {
    this.returnDate = returnDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public String getMemberName() {
    return memberName;
  }

  public void setMemberName(String memberName) {
    this.memberName = memberName;
  }

  public double getCalculatedFine() {
    if (dueDate == null) return 0.0;
    java.time.LocalDate due = dueDate.toLocalDate();
    java.time.LocalDate end =
        (returnDate != null) ? returnDate.toLocalDate() : java.time.LocalDate.now();
    if (end.isAfter(due)) {
      long days = java.time.temporal.ChronoUnit.DAYS.between(due, end);
      return days * 5.0; // 5.0 rupees fine per day
    }
    return 0.0;
  }

  @Override
  public String toString() {
    String retStr = returnDate != null ? returnDate.toString() : "N/A";
    double fine = getCalculatedFine();
    return "Transaction [ID="
        + transactionId
        + ", Member="
        + (memberName != null ? memberName : memberId)
        + ", Book="
        + (bookTitle != null ? bookTitle : bookId)
        + ", Issued="
        + issueDate
        + ", Due="
        + dueDate
        + ", Returned="
        + retStr
        + ", Status="
        + status
        + (fine > 0 ? ", Fine=" + String.format("%.2f", fine) : "")
        + "]";
  }
}
