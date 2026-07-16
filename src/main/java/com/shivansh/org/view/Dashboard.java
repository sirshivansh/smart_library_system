package com.shivansh.org.view;

import com.shivansh.org.controller.BookController;
import com.shivansh.org.controller.MemberController;
import com.shivansh.org.controller.TransactionController;
import com.shivansh.org.dto.Book;
import com.shivansh.org.dto.Member;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.exception.BookNotFoundException;
import com.shivansh.org.exception.LibraryException;
import com.shivansh.org.exception.MemberNotFoundException;
import com.shivansh.org.exception.TransactionException;
import com.shivansh.org.exception.ValidationException;
import com.shivansh.org.util.DbConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Main View class for the Smart Library Management System (Book Haven).
 *
 * <p>Implements the View layer of the MVC architecture: - Model: DTOs (Book, Member, Transaction) +
 * DAO layer - View: This class (Dashboard) — Console UI rendering - Controller: BookController,
 * MemberController, TransactionController
 *
 * <p>Features a premium CLI interface with ANSI colors, Unicode box-drawing, ASCII art branding,
 * real-time library statistics, and overdue reports.
 *
 * @author Shivansh
 * @version 2.0
 */
public class Dashboard {

  // ── Controller instances (MVC pattern) ────────────────────────
  private static final BookController bookController = new BookController();
  private static final MemberController memberController = new MemberController();
  private static final TransactionController transactionController = new TransactionController();

  // ── ANSI Escape Codes for CLI Styling ─────────────────────────
  private static final String RESET = "\u001B[0m";
  private static final String BOLD = "\u001B[1m";
  private static final String DIM = "\u001B[2m";
  private static final String RED = "\u001B[31m";
  private static final String GREEN = "\u001B[32m";
  private static final String YELLOW = "\u001B[33m";
  private static final String BLUE = "\u001B[34m";
  private static final String MAGENTA = "\u001B[35m";
  private static final String CYAN = "\u001B[36m";
  private static final String WHITE_BOLD = "\u001B[1;37m";
  private static final String CYAN_BOLD = "\u001B[1;36m";
  private static final String GREEN_BOLD = "\u001B[1;32m";
  private static final String RED_BOLD = "\u001B[1;31m";
  private static final String YELLOW_BOLD = "\u001B[1;33m";
  private static final String BLUE_BOLD = "\u001B[1;34m";
  private static final String MAGENTA_BOLD = "\u001B[1;35m";

  // ═══════════════════════════════════════════════════════════════
  //  MAIN ENTRY POINT
  // ═══════════════════════════════════════════════════════════════

  public static void main(String[] args) {
    System.out.println(CYAN + "\n  Initializing Smart Library Management System..." + RESET);
    DbConnection.initializeDatabase();

    // Start Embedded HTTP Web Server for Frontend Integration

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int mainOption = 0;

    do {
      printBanner();
      printSectionTitle("MAIN MENU");
      System.out.println("  " + YELLOW_BOLD + " 1 " + RESET + " │ Admin Console Login");
      System.out.println("  " + YELLOW_BOLD + " 2 " + RESET + " │ Member Portal Login");
      System.out.println("  " + YELLOW_BOLD + " 3 " + RESET + " │ Register New Member Account");
      System.out.println("  " + YELLOW_BOLD + " 4 " + RESET + " │ Exit System");
      printDivider();

      mainOption = readInt(br, "  Enter your choice: ", 1, 4);

      switch (mainOption) {
        case 1:
          handleAdminLogin(br);
          break;
        case 2:
          handleMemberLogin(br);
          break;
        case 3:
          handleMemberRegistration(br);
          break;
        case 4:
          System.out.println(
              GREEN_BOLD
                  + "\n  ✓ Thank you for using Book Haven Smart Library. Goodbye!\n"
                  + RESET);
          break;
      }
      if (mainOption != 4) pressEnterToContinue(br);
    } while (mainOption != 4);
  }

  // ═══════════════════════════════════════════════════════════════
  //  BRANDING & UI COMPONENTS
  // ═══════════════════════════════════════════════════════════════

  private static void printBanner() {
    System.out.println("\n");
    System.out.println(
        CYAN_BOLD
            + "  ██████╗  ██████╗  ██████╗ ██╗  ██╗    ██╗  ██╗ █████╗ ██╗   ██╗███████╗███╗   ██╗"
            + RESET);
    System.out.println(
        CYAN_BOLD
            + "  ██╔══██╗██╔═══██╗██╔═══██╗██║ ██╔╝    ██║  ██║██╔══██╗██║   ██║██╔════╝████╗  ██║"
            + RESET);
    System.out.println(
        CYAN_BOLD
            + "  ██████╔╝██║   ██║██║   ██║█████╔╝     ███████║███████║██║   ██║█████╗  ██╔██╗ ██║"
            + RESET);
    System.out.println(
        CYAN_BOLD
            + "  ██╔══██╗██║   ██║██║   ██║██╔═██╗     ██╔══██║██╔══██║╚██╗ ██╔╝██╔══╝  ██║╚██╗██║"
            + RESET);
    System.out.println(
        CYAN_BOLD
            + "  ██████╔╝╚██████╔╝╚██████╔╝██║  ██╗    ██║  ██║██║  ██║ ╚████╔╝ ███████╗██║ ╚████║"
            + RESET);
    System.out.println(
        CYAN_BOLD
            + "  ╚══════╝  ╚═════╝  ╚═════╝ ╚═╝  ╚═╝    ╚═╝  ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝"
            + RESET);
    System.out.println(
        WHITE_BOLD
            + "              S M A R T   L I B R A R Y   M A N A G E M E N T   S Y S T E M"
            + RESET);
    System.out.println(
        DIM + "                          MVC Architecture  •  MySQL  •  Java 11" + RESET);
    System.out.println(
        CYAN
            + "  ════════════════════════════════════════════════════════════════════════════════"
            + RESET);
  }

  private static void printBoxHeader(String title) {
    int width = 76;
    int pad = (width - title.length()) / 2;
    StringBuilder sb = new StringBuilder();
    sb.append("  ").append(CYAN_BOLD).append("╔");
    for (int i = 0; i < width; i++) sb.append("═");
    sb.append("╗\n  ║");
    for (int i = 0; i < pad; i++) sb.append(" ");
    sb.append(WHITE_BOLD).append(title).append(CYAN_BOLD);
    int rem = width - pad - title.length();
    for (int i = 0; i < rem; i++) sb.append(" ");
    sb.append("║\n  ╚");
    for (int i = 0; i < width; i++) sb.append("═");
    sb.append("╝").append(RESET);
    System.out.println(sb.toString());
  }

  private static void printSectionTitle(String title) {
    System.out.println(
        "  " + CYAN_BOLD + "┌─── " + WHITE_BOLD + title + CYAN_BOLD + " ───┐" + RESET);
  }

  private static void printDivider() {
    System.out.println(DIM + "  ────────────────────────────────────────────────" + RESET);
  }

  private static void printSuccess(String msg) {
    System.out.println(GREEN_BOLD + "\n  ✓ " + msg + RESET);
  }

  private static void printError(String msg) {
    System.out.println(RED_BOLD + "\n  ✗ " + msg + RESET);
  }

  private static void printWarning(String msg) {
    System.out.println(YELLOW_BOLD + "\n  ⚠ " + msg + RESET);
  }

  private static void printInfo(String msg) {
    System.out.println(CYAN + "\n  ℹ " + msg + RESET);
  }

  // ═══════════════════════════════════════════════════════════════
  //  ADMIN FLOW
  // ═══════════════════════════════════════════════════════════════

  private static void handleAdminLogin(BufferedReader br) {
    printSectionTitle("ADMIN SECURE LOGIN");
    String username = readString(br, "  Username: ", true);
    String password = readString(br, "  Password: ", true);

    if (memberController.authenticateAdmin(username, password)) {
      printSuccess("Authentication Successful! Loading Admin Dashboard...");
      showAdminDashboard(br);
    } else {
      printError("Invalid Admin Credentials!");
    }
  }

  private static void showAdminDashboard(BufferedReader br) {
    int choice = 0;
    do {
      printBanner();
      printBoxHeader("A D M I N   D A S H B O A R D");
      printAdminStats();
      System.out.println();
      printSectionTitle("OPERATIONS");
      System.out.println(
          "  "
              + YELLOW_BOLD
              + " 1 "
              + RESET
              + " │ "
              + GREEN
              + "Add"
              + RESET
              + " New Book to Catalog");
      System.out.println(
          "  "
              + YELLOW_BOLD
              + " 2 "
              + RESET
              + " │ "
              + BLUE
              + "Update"
              + RESET
              + " Book Catalog Entry");
      System.out.println(
          "  "
              + YELLOW_BOLD
              + " 3 "
              + RESET
              + " │ "
              + RED
              + "Delete"
              + RESET
              + " Book from System");
      System.out.println("  " + YELLOW_BOLD + " 4 " + RESET + " │ View Full Book Catalog");
      System.out.println(
          "  " + YELLOW_BOLD + " 5 " + RESET + " │ Search Catalog (Title/Author/Genre)");
      printDivider();
      System.out.println("  " + YELLOW_BOLD + " 6 " + RESET + " │ View All Registered Members");
      System.out.println("  " + YELLOW_BOLD + " 7 " + RESET + " │ Search Member (by ID or Email)");
      System.out.println(
          "  "
              + YELLOW_BOLD
              + " 8 "
              + RESET
              + " │ "
              + RED
              + "Delete"
              + RESET
              + " Member from System");
      printDivider();
      System.out.println("  " + YELLOW_BOLD + " 9 " + RESET + " │ View All Transaction Logs");
      System.out.println(
          "  "
              + YELLOW_BOLD
              + "10 "
              + RESET
              + " │ "
              + RED_BOLD
              + "★"
              + RESET
              + " Generate Overdue Report");
      System.out.println("  " + YELLOW_BOLD + "11 " + RESET + " │ Sign Out");
      printDivider();

      choice = readInt(br, "  Select Operation (1-11): ", 1, 11);

      try {
        switch (choice) {
          case 1:
            adminAddBook(br);
            break;
          case 2:
            adminUpdateBook(br);
            break;
          case 3:
            adminDeleteBook(br);
            break;
          case 4:
            printSectionTitle("FULL BOOK CATALOG");
            printBookTable(bookController.getAllBooks());
            break;
          case 5:
            printSectionTitle("SEARCH CATALOG");
            String q = readString(br, "  Enter keyword: ", true);
            printBookTable(bookController.searchBooks(q));
            break;
          case 6:
            printSectionTitle("REGISTERED MEMBERS");
            printMemberTable(memberController.getAllMembers());
            break;
          case 7:
            adminSearchMember(br);
            break;
          case 8:
            adminDeleteMember(br);
            break;
          case 9:
            printSectionTitle("TRANSACTION AUDIT LOG");
            printTransactionTable(transactionController.getAllTransactions());
            break;
          case 10:
            adminOverdueReport();
            break;
          case 11:
            printInfo("Signing out from Admin Console...");
            break;
        }
      } catch (LibraryException e) {
        printError(e.getMessage());
      } catch (Exception e) {
        printError("Unexpected error: " + e.getMessage());
      }
      if (choice != 11) pressEnterToContinue(br);
    } while (choice != 11);
  }

  // ── Admin Sub-Operations ──────────────────────────────────────

  private static void adminAddBook(BufferedReader br) throws ValidationException {
    printSectionTitle("ADD NEW BOOK");
    String title = readString(br, "  Book Title: ", true);
    String author = readString(br, "  Book Author: ", true);
    String isbn = readString(br, "  ISBN (blank = skip): ", false);
    String genre = readString(br, "  Genre: ", true);
    int qty = readInt(br, "  Total Copies: ", 1, 1000);

    if (bookController.addBook(title, author, isbn, genre, qty)) {
      printSuccess("Book '" + title + "' successfully added to catalog.");
    } else {
      printError("Failed to add book. Check inputs.");
    }
  }

  private static void adminUpdateBook(BufferedReader br)
      throws BookNotFoundException, ValidationException {
    printSectionTitle("UPDATE BOOK ENTRY");
    int id = readInt(br, "  Enter Book ID: ", 1, Integer.MAX_VALUE);
    Book existing = bookController.getBookById(id);
    if (existing == null) {
      throw new BookNotFoundException(id);
    }
    System.out.println("  " + DIM + "Current: " + existing + RESET);
    String t = readString(br, "  New Title (blank = keep): ", false);
    String a = readString(br, "  New Author (blank = keep): ", false);
    String i = readString(br, "  New ISBN (blank = keep): ", false);
    String g = readString(br, "  New Genre (blank = keep): ", false);
    String c = readString(br, "  New Total Copies (blank = keep): ", false);

    if (bookController.updateBook(id, t, a, i, g, c)) {
      printSuccess("Book ID " + id + " updated successfully.");
    } else {
      printError("Failed to update book.");
    }
  }

  private static void adminDeleteBook(BufferedReader br) throws BookNotFoundException {
    printSectionTitle("DELETE BOOK");
    int id = readInt(br, "  Enter Book ID to delete: ", 1, Integer.MAX_VALUE);
    String confirm =
        readString(br, "  " + RED + "Confirm permanent deletion? (yes/no): " + RESET, true);
    if ("yes".equalsIgnoreCase(confirm.trim())) {
      if (bookController.deleteBook(id)) {
        printSuccess("Book ID " + id + " permanently removed.");
      } else {
        printError("Deletion failed.");
      }
    } else {
      printInfo("Deletion cancelled.");
    }
  }

  private static void adminSearchMember(BufferedReader br) throws MemberNotFoundException {
    printSectionTitle("SEARCH MEMBER");
    System.out.println("  " + YELLOW_BOLD + "1" + RESET + " │ Search by ID");
    System.out.println("  " + YELLOW_BOLD + "2" + RESET + " │ Search by Email");
    int opt = readInt(br, "  Select (1-2): ", 1, 2);
    Member m;
    if (opt == 1) {
      int memId = readInt(br, "  Member ID: ", 1, Integer.MAX_VALUE);
      m = memberController.getMemberById(memId);
    } else {
      String email = readString(br, "  Email Address: ", true).trim();
      m = memberController.getMemberByEmail(email);
    }
    printMemberCard(m);
  }

  private static void adminDeleteMember(BufferedReader br) throws MemberNotFoundException {
    printSectionTitle("DELETE MEMBER");
    int id = readInt(br, "  Enter Member ID to delete: ", 1, Integer.MAX_VALUE);
    Member m = memberController.getMemberById(id);
    printMemberCard(m);
    String confirm =
        readString(br, "  " + RED + "Confirm permanent deletion? (yes/no): " + RESET, true);
    if ("yes".equalsIgnoreCase(confirm.trim())) {
      if (memberController.deleteMember(id)) {
        printSuccess(
            "Member ID "
                + id
                + " ("
                + m.getFirstName()
                + " "
                + m.getLastName()
                + ") permanently removed.");
      } else {
        printError("Deletion failed.");
      }
    } else {
      printInfo("Deletion cancelled.");
    }
  }

  private static void adminOverdueReport() {
    printSectionTitle("★ OVERDUE BOOKS REPORT");
    List<Transaction> overdue = transactionController.getOverdueTransactions();
    if (overdue.isEmpty()) {
      printSuccess("No overdue books! All borrowers are on schedule.");
    } else {
      printWarning(overdue.size() + " book(s) currently overdue:");
      printTransactionTable(overdue);

      double totalFines = overdue.stream().mapToDouble(Transaction::getCalculatedFine).sum();
      System.out.println(
          "  " + RED_BOLD + "  Total Overdue Fines: ₹" + String.format("%.2f", totalFines) + RESET);
    }
  }

  // ═══════════════════════════════════════════════════════════════
  //  MEMBER FLOW
  // ═══════════════════════════════════════════════════════════════

  private static void handleMemberLogin(BufferedReader br) {
    printSectionTitle("MEMBER PORTAL LOGIN");
    String email = readString(br, "  Email: ", true).trim();
    String password = readString(br, "  Password: ", true);

    try {
      Member loggedIn = memberController.authenticateMember(email, password);
      if (loggedIn != null) {
        printSuccess("Welcome back, " + loggedIn.getFirstName() + "!");
        showMemberDashboard(br, loggedIn);
      } else {
        printError("Invalid Email or Password.");
      }
    } catch (ValidationException e) {
      printError(e.getMessage());
    }
  }

  private static void handleMemberRegistration(BufferedReader br) {
    printSectionTitle("NEW MEMBER REGISTRATION");
    String fName = readString(br, "  First Name: ", true).trim();
    String lName = readString(br, "  Last Name: ", true).trim();
    String email = readString(br, "  Email Address: ", true).trim();
    String phone = readString(br, "  Phone Number (blank = skip): ", false).trim();
    String password = readString(br, "  Password (min 6 chars): ", true);
    System.out.println("  Select Membership Type:");
    System.out.println("    " + YELLOW_BOLD + "1" + RESET + " │ REGULAR  (3 books max)");
    System.out.println("    " + YELLOW_BOLD + "2" + RESET + " │ STUDENT  (5 books max)");
    System.out.println("    " + YELLOW_BOLD + "3" + RESET + " │ FACULTY  (10 books max)");
    int typeChoice = readInt(br, "  Select (1-3): ", 1, 3);
    String type = typeChoice == 2 ? "STUDENT" : typeChoice == 3 ? "FACULTY" : "REGULAR";

    try {
      if (memberController.registerMember(fName, lName, email, phone, password, type)) {
        printSuccess("Registration complete! You can now log in with your credentials.");
      } else {
        printError("Registration failed. Email may already be in use.");
      }
    } catch (ValidationException e) {
      printError(e.getMessage());
    }
  }

  private static void showMemberDashboard(BufferedReader br, Member member) {
    int choice = 0;
    do {
      printBanner();
      printBoxHeader(
          "MEMBER DASHBOARD: "
              + member.getFirstName().toUpperCase()
              + " "
              + member.getLastName().toUpperCase());
      System.out.println();
      printSectionTitle("OPERATIONS");
      System.out.println("  " + YELLOW_BOLD + "1" + RESET + " │ Browse All Available Books");
      System.out.println("  " + YELLOW_BOLD + "2" + RESET + " │ Search Catalog by Keyword");
      System.out.println("  " + YELLOW_BOLD + "3" + RESET + " │ Borrow / Check Out a Book");
      System.out.println("  " + YELLOW_BOLD + "4" + RESET + " │ Return a Borrowed Book");
      System.out.println("  " + YELLOW_BOLD + "5" + RESET + " │ View My Borrowing History");
      System.out.println("  " + YELLOW_BOLD + "6" + RESET + " │ View / Edit My Profile");
      System.out.println("  " + YELLOW_BOLD + "7" + RESET + " │ Sign Out");
      printDivider();

      choice = readInt(br, "  Select (1-7): ", 1, 7);

      try {
        switch (choice) {
          case 1:
            printSectionTitle("LIBRARY CATALOG");
            printBookTable(bookController.getAllBooks());
            break;
          case 2:
            printSectionTitle("SEARCH");
            String q = readString(br, "  Keyword: ", true);
            printBookTable(bookController.searchBooks(q));
            break;
          case 3:
            printSectionTitle("BORROW BOOK");
            int borrowId = readInt(br, "  Book ID: ", 1, Integer.MAX_VALUE);
            try {
              transactionController.borrowBook(member.getMemberId(), borrowId);
              printSuccess("Book issued! Return within 14 days to avoid fines.");
            } catch (TransactionException e) {
              printError(e.getMessage());
            }
            break;
          case 4:
            printSectionTitle("RETURN BOOK");
            int retId = readInt(br, "  Book ID: ", 1, Integer.MAX_VALUE);
            try {
              double fine = transactionController.returnBook(member.getMemberId(), retId);
              printSuccess("Book returned successfully!");
              if (fine > 0) {
                printWarning(String.format("Late return fine: ₹%.2f", fine));
              }
            } catch (TransactionException e) {
              printError(e.getMessage());
            }
            break;
          case 5:
            printSectionTitle("MY BORROWING HISTORY");
            printTransactionTable(
                transactionController.getMemberTransactions(member.getMemberId()));
            break;
          case 6:
            memberViewEditProfile(br, member);
            break;
          case 7:
            printInfo("Signing out...");
            break;
        }
      } catch (Exception e) {
        printError("Unexpected error: " + e.getMessage());
      }
      if (choice != 7) pressEnterToContinue(br);
    } while (choice != 7);
  }

  private static void memberViewEditProfile(BufferedReader br, Member member) {
    printSectionTitle("MY PROFILE");
    printMemberCard(member);
    System.out.println();
    String edit = readString(br, "  Edit profile? (y/n): ", true).trim().toLowerCase();
    if ("y".equals(edit) || "yes".equals(edit)) {
      String f = readString(br, "  New First Name (blank = keep): ", false).trim();
      if (!f.isEmpty()) member.setFirstName(f);
      String l = readString(br, "  New Last Name (blank = keep): ", false).trim();
      if (!l.isEmpty()) member.setLastName(l);
      String ph = readString(br, "  New Phone (blank = keep): ", false).trim();
      if (!ph.isEmpty()) member.setPhone(ph);
      String p = readString(br, "  New Password (blank = keep): ", false);
      if (!p.isEmpty()) member.setPassword(p);

      try {
        if (memberController.updateMemberProfile(member)) {
          printSuccess("Profile updated.");
          Member refreshed = memberController.refreshMember(member.getMemberId());
          if (refreshed != null) {
            member.setFirstName(refreshed.getFirstName());
            member.setLastName(refreshed.getLastName());
            member.setPhone(refreshed.getPhone());
            member.setPassword(refreshed.getPassword());
          }
        } else {
          printError("Update failed. Check input format.");
        }
      } catch (ValidationException e) {
        printError(e.getMessage());
      }
    }
  }

  // ═══════════════════════════════════════════════════════════════
  //  DATA TABLE RENDERERS
  // ═══════════════════════════════════════════════════════════════

  private static void printBookTable(List<Book> books) {
    if (books.isEmpty()) {
      printWarning("No books found.");
      return;
    }
    String border = CYAN_BOLD;
    System.out.println(
        border
            + "  ╔════════╤════════════════════════════════╤══════════════════════════╤════════════════╤════════════════╤══════════╤══════════╗");
    System.out.printf(
        border + "  ║ %-6s │ %-30s │ %-24s │ %-14s │ %-14s │ %-8s │ %-8s ║%n" + RESET,
        "ID",
        "Title",
        "Author",
        "ISBN",
        "Genre",
        "Total",
        "Avail");
    System.out.println(
        border
            + "  ╠════════╪════════════════════════════════╪══════════════════════════╪════════════════╪════════════════╪══════════╪══════════╣"
            + RESET);
    for (Book b : books) {
      String availColor = b.getAvailableCopies() > 0 ? GREEN : RED;
      System.out.printf(
          "  ║ %-6d │ %-30s │ %-24s │ %-14s │ %-14s │ %-8d │ "
              + availColor
              + "%-8d"
              + RESET
              + " ║%n",
          b.getBookId(),
          trunc(b.getTitle(), 30),
          trunc(b.getAuthor(), 24),
          trunc(b.getIsbn() != null ? b.getIsbn() : "—", 14),
          trunc(b.getGenre(), 14),
          b.getTotalCopies(),
          b.getAvailableCopies());
    }
    System.out.println(
        border
            + "  ╚════════╧════════════════════════════════╧══════════════════════════╧════════════════╧════════════════╧══════════╧══════════╝"
            + RESET);
    System.out.println(DIM + "  Showing " + books.size() + " record(s)" + RESET);
  }

  private static void printMemberTable(List<Member> members) {
    if (members.isEmpty()) {
      printWarning("No members found.");
      return;
    }
    String b = CYAN_BOLD;
    System.out.println(
        b
            + "  ╔════════╤════════════════════════════════╤════════════════════════════════════╤══════════════╤═════════════════╗");
    System.out.printf(
        b + "  ║ %-6s │ %-30s │ %-34s │ %-12s │ %-15s ║%n" + RESET,
        "ID",
        "Name",
        "Email",
        "Phone",
        "Type");
    System.out.println(
        b
            + "  ╠════════╪════════════════════════════════╪════════════════════════════════════╪══════════════╪═════════════════╣"
            + RESET);
    for (Member m : members) {
      System.out.printf(
          "  ║ %-6d │ %-30s │ %-34s │ %-12s │ %-15s ║%n",
          m.getMemberId(),
          trunc(m.getFirstName() + " " + m.getLastName(), 30),
          trunc(m.getEmail(), 34),
          m.getPhone() != null ? m.getPhone() : "—",
          m.getMembershipType());
    }
    System.out.println(
        b
            + "  ╚════════╧════════════════════════════════╧════════════════════════════════════╧══════════════╧═════════════════╝"
            + RESET);
    System.out.println(DIM + "  Showing " + members.size() + " record(s)" + RESET);
  }

  private static void printTransactionTable(List<Transaction> txs) {
    if (txs.isEmpty()) {
      printWarning("No transactions found.");
      return;
    }
    String b = CYAN_BOLD;
    System.out.println(
        b
            + "  ╔═══════╤═════════════════════════╤═════════════════════════╤════════════╤════════════╤════════════╤══════════╤══════════╗");
    System.out.printf(
        b + "  ║ %-5s │ %-23s │ %-23s │ %-10s │ %-10s │ %-10s │ %-8s │ %-8s ║%n" + RESET,
        "TxID",
        "Member",
        "Book",
        "Issued",
        "Due",
        "Returned",
        "Status",
        "Fine");
    System.out.println(
        b
            + "  ╠═══════╪═════════════════════════╪═════════════════════════╪════════════╪════════════╪════════════╪══════════╪══════════╣"
            + RESET);
    for (Transaction t : txs) {
      String retDate = t.getReturnDate() != null ? t.getReturnDate().toString() : "—";
      double fine = t.getCalculatedFine();
      String statusColor = "RETURNED".equals(t.getStatus()) ? GREEN : RED;
      String fineStr = fine > 0 ? String.format("₹%.2f", fine) : "₹0.00";

      System.out.printf(
          "  ║ %-5d │ %-23s │ %-23s │ %-10s │ %-10s │ %-10s │ "
              + statusColor
              + "%-8s"
              + RESET
              + " │ %-8s ║%n",
          t.getTransactionId(),
          trunc(
              t.getMemberName() != null ? t.getMemberName() : String.valueOf(t.getMemberId()), 23),
          trunc(t.getBookTitle() != null ? t.getBookTitle() : String.valueOf(t.getBookId()), 23),
          t.getIssueDate(),
          t.getDueDate(),
          retDate,
          t.getStatus(),
          fineStr);
    }
    System.out.println(
        b
            + "  ╚═══════╧═════════════════════════╧═════════════════════════╧════════════╧════════════╧════════════╧══════════╧══════════╝"
            + RESET);
    System.out.println(DIM + "  Showing " + txs.size() + " record(s)" + RESET);
  }

  private static void printMemberCard(Member m) {
    System.out.println(
        "  " + MAGENTA_BOLD + "┌──────────────────────────────────────────────────┐" + RESET);
    System.out.printf(
        "  "
            + MAGENTA_BOLD
            + "│"
            + RESET
            + "  Member ID:       "
            + BOLD
            + "%-30d"
            + MAGENTA_BOLD
            + "│%n"
            + RESET,
        m.getMemberId());
    System.out.printf(
        "  "
            + MAGENTA_BOLD
            + "│"
            + RESET
            + "  Full Name:       %-30s"
            + MAGENTA_BOLD
            + "│%n"
            + RESET,
        m.getFirstName() + " " + m.getLastName());
    System.out.printf(
        "  "
            + MAGENTA_BOLD
            + "│"
            + RESET
            + "  Email:           %-30s"
            + MAGENTA_BOLD
            + "│%n"
            + RESET,
        m.getEmail());
    System.out.printf(
        "  "
            + MAGENTA_BOLD
            + "│"
            + RESET
            + "  Phone:           %-30s"
            + MAGENTA_BOLD
            + "│%n"
            + RESET,
        m.getPhone() != null ? m.getPhone() : "—");
    System.out.printf(
        "  "
            + MAGENTA_BOLD
            + "│"
            + RESET
            + "  Membership:      %-30s"
            + MAGENTA_BOLD
            + "│%n"
            + RESET,
        m.getMembershipType());
    System.out.println(
        "  " + MAGENTA_BOLD + "└──────────────────────────────────────────────────┘" + RESET);
  }

  // ═══════════════════════════════════════════════════════════════
  //  ADMIN STATISTICS PANEL
  // ═══════════════════════════════════════════════════════════════

  private static void printAdminStats() {
    double[] stats = transactionController.getLibraryStatistics();
    int totalTitles = (int) stats[0];
    int totalCopies = (int) stats[1];
    int availCopies = (int) stats[2];
    int totalMembers = (int) stats[3];
    int activeBorrows = (int) stats[4];
    int overdueCount = (int) stats[5];
    double totalFines = stats[6];

    System.out.println(
        "  "
            + YELLOW_BOLD
            + "┌─────────────────────────────────────┬──────────────────────────────────────┐"
            + RESET);
    System.out.printf(
        "  "
            + YELLOW_BOLD
            + "│  "
            + CYAN_BOLD
            + "%-34s"
            + YELLOW_BOLD
            + " │  "
            + CYAN_BOLD
            + "%-35s"
            + YELLOW_BOLD
            + "│%n"
            + RESET,
        "CATALOG SUMMARY",
        "CIRCULATION METRICS");
    System.out.println(
        "  "
            + YELLOW_BOLD
            + "├─────────────────────────────────────┼──────────────────────────────────────┤"
            + RESET);
    System.out.printf(
        "  "
            + YELLOW_BOLD
            + "│  "
            + RESET
            + "Book Titles:       "
            + BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │  "
            + RESET
            + "Active Borrows:    "
            + BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │%n"
            + RESET,
        totalTitles,
        activeBorrows);
    System.out.printf(
        "  "
            + YELLOW_BOLD
            + "│  "
            + RESET
            + "Physical Copies:   "
            + BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │  "
            + RESET
            + "Overdue Items:     "
            + RED_BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │%n"
            + RESET,
        totalCopies,
        overdueCount);
    System.out.printf(
        "  "
            + YELLOW_BOLD
            + "│  "
            + RESET
            + "Available Copies:  "
            + GREEN_BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │  "
            + RESET
            + "Pending Fines:     "
            + RED_BOLD
            + "₹%-15.2f"
            + YELLOW_BOLD
            + " │%n"
            + RESET,
        availCopies,
        totalFines);
    System.out.printf(
        "  "
            + YELLOW_BOLD
            + "│  "
            + RESET
            + "Registered Members:"
            + BLUE_BOLD
            + "%-16d"
            + YELLOW_BOLD
            + " │  "
            + RESET
            + "                                     "
            + YELLOW_BOLD
            + "│%n"
            + RESET,
        totalMembers);
    System.out.println(
        "  "
            + YELLOW_BOLD
            + "└─────────────────────────────────────┴──────────────────────────────────────┘"
            + RESET);
  }

  // ═══════════════════════════════════════════════════════════════
  //  INPUT HELPERS
  // ═══════════════════════════════════════════════════════════════

  private static int readInt(BufferedReader br, String prompt, int min, int max) {
    while (true) {
      try {
        System.out.print(prompt);
        String line = br.readLine();
        if (line == null) return -1;
        line = line.trim();
        if (line.isEmpty()) continue;
        int val = Integer.parseInt(line);
        if (val >= min && val <= max) return val;
        printError("Enter a value between " + min + " and " + max + ".");
      } catch (NumberFormatException e) {
        printError("Please enter a numeric value.");
      } catch (Exception e) {
        printError("Input error: " + e.getMessage());
      }
    }
  }

  private static String readString(BufferedReader br, String prompt, boolean required) {
    while (true) {
      try {
        System.out.print(prompt);
        String line = br.readLine();
        if (line == null) return "";
        line = line.trim();
        if (required && line.isEmpty()) {
          printError("This field is required.");
          continue;
        }
        return line;
      } catch (Exception e) {
        printError("Input error: " + e.getMessage());
      }
    }
  }

  private static void pressEnterToContinue(BufferedReader br) {
    System.out.println(DIM + "\n  Press Enter to continue..." + RESET);
    try {
      br.readLine();
    } catch (Exception ignored) {
    }
  }

  private static String trunc(String val, int max) {
    if (val == null) return "";
    return val.length() <= max ? val : val.substring(0, max - 3) + "...";
  }
}
