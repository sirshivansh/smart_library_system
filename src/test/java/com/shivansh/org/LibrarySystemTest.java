package com.shivansh.org;

import static org.junit.Assert.*;

import com.shivansh.org.dto.Book;
import com.shivansh.org.dto.Member;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.util.InputValidator;
import com.shivansh.org.util.PasswordUtil;
import java.sql.Date;
import java.time.LocalDate;
import org.junit.Test;

/**
 * Comprehensive unit tests for the Smart Library Management System. Tests cover: - Password hashing
 * (SHA-256 determinism, null safety, hash length) - Input validation (email, password, name,
 * integer parsing) - Fine calculation (on-time, early, late, and overdue scenarios) - DTO
 * getters/setters (Book, Member, Transaction) - Edge cases (zero-day overdue, exact due date
 * return, large overdue)
 *
 * @author Shivansh
 * @version 2.0
 */
public class LibrarySystemTest {

  // ═══════════════════════════════════════════════════════════════
  //  TEST 1: Password Hashing (SHA-256)
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testPasswordHashing() {
    String plain = "mySecretPassword123";
    String hashed1 = PasswordUtil.hashPassword(plain);
    String hashed2 = PasswordUtil.hashPassword(plain);

    assertNotNull(hashed1);
    assertEquals(64, hashed1.length()); // SHA-256 is 64 hex characters
    assertEquals(hashed1, hashed2); // Deterministic

    String emptyHash = PasswordUtil.hashPassword("");
    assertNotNull(emptyHash);
    assertNull(PasswordUtil.hashPassword(null));

    // Different passwords should produce different hashes
    String differentHash = PasswordUtil.hashPassword("differentPassword");
    assertNotEquals(hashed1, differentHash);
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 2: Input Validation (Email, Password, Name, Integer)
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testInputValidation() {
    // Email validations
    assertTrue(InputValidator.isValidEmail("test@example.com"));
    assertTrue(InputValidator.isValidEmail("user.name+tag@sub.domain.org"));
    assertFalse(InputValidator.isValidEmail("plainaddress"));
    assertFalse(InputValidator.isValidEmail("@missingusername.com"));
    assertFalse(InputValidator.isValidEmail("username@.com"));
    assertFalse(InputValidator.isValidEmail(null));

    // Name validations
    assertTrue(InputValidator.isValidName("Shivansh Mishra"));
    assertTrue(InputValidator.isValidName("Alice"));
    assertFalse(InputValidator.isValidName("Shivansh123")); // numeric
    assertFalse(InputValidator.isValidName("J")); // too short
    assertFalse(InputValidator.isValidName("")); // empty

    // Password validations
    assertTrue(InputValidator.isValidPassword("secret123"));
    assertFalse(InputValidator.isValidPassword("short")); // < 6 chars
    assertFalse(InputValidator.isValidPassword(null));

    // Positive integer validations
    assertTrue(InputValidator.isPositiveInteger("10"));
    assertTrue(InputValidator.isPositiveInteger("1"));
    assertFalse(InputValidator.isPositiveInteger("0"));
    assertFalse(InputValidator.isPositiveInteger("-5"));
    assertFalse(InputValidator.isPositiveInteger("abc"));
    assertFalse(InputValidator.isPositiveInteger(null));
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 3: Fine Calculation Logic
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testFineCalculation() {
    Transaction tx = new Transaction();

    // Scenario 1: Book is not overdue (due date in future, no return date yet)
    tx.setDueDate(Date.valueOf(LocalDate.now().plusDays(5)));
    tx.setReturnDate(null);
    assertEquals(0.0, tx.getCalculatedFine(), 0.001);

    // Scenario 2: Book returned early/on-time (returned before due date)
    tx.setDueDate(Date.valueOf(LocalDate.now().plusDays(5)));
    tx.setReturnDate(Date.valueOf(LocalDate.now().plusDays(2)));
    assertEquals(0.0, tx.getCalculatedFine(), 0.001);

    // Scenario 3: Book is overdue but returned late (due date was 3 days ago, returned today)
    tx.setDueDate(Date.valueOf(LocalDate.now().minusDays(3)));
    tx.setReturnDate(Date.valueOf(LocalDate.now()));
    // 3 days late * 5.0 units/day = 15.0 units fine
    assertEquals(15.0, tx.getCalculatedFine(), 0.001);

    // Scenario 4: Book is currently overdue and not yet returned (due date was 5 days ago, no
    // return date)
    tx.setDueDate(Date.valueOf(LocalDate.now().minusDays(5)));
    tx.setReturnDate(null);
    // 5 days late * 5.0 units/day = 25.0 units fine
    assertEquals(25.0, tx.getCalculatedFine(), 0.001);
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 4: Book DTO — Getters, Setters, and Constructors
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testBookDTO() {
    // Test no-args constructor and setters
    Book book = new Book();
    book.setBookId(1);
    book.setTitle("The Great Gatsby");
    book.setAuthor("F. Scott Fitzgerald");
    book.setIsbn("978-0743273565");
    book.setGenre("Fiction");
    book.setTotalCopies(5);
    book.setAvailableCopies(3);

    assertEquals(1, book.getBookId());
    assertEquals("The Great Gatsby", book.getTitle());
    assertEquals("F. Scott Fitzgerald", book.getAuthor());
    assertEquals("978-0743273565", book.getIsbn());
    assertEquals("Fiction", book.getGenre());
    assertEquals(5, book.getTotalCopies());
    assertEquals(3, book.getAvailableCopies());

    // Test parameterized constructor (without ISBN)
    Book book2 = new Book("1984", "George Orwell", "Dystopian", 4, 4);
    assertEquals("1984", book2.getTitle());
    assertEquals("George Orwell", book2.getAuthor());
    assertEquals("Dystopian", book2.getGenre());
    assertEquals(4, book2.getTotalCopies());
    assertEquals(4, book2.getAvailableCopies());
    assertNull(book2.getIsbn()); // ISBN not set in this constructor

    // Test full constructor with ISBN
    Book book3 = new Book(10, "The Hobbit", "J.R.R. Tolkien", "978-0547928227", "Fantasy", 6, 6);
    assertEquals(10, book3.getBookId());
    assertEquals("978-0547928227", book3.getIsbn());

    // Test toString contains key info
    String str = book.toString();
    assertTrue(str.contains("The Great Gatsby"));
    assertTrue(str.contains("F. Scott Fitzgerald"));
    assertTrue(str.contains("978-0743273565"));
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 5: Member DTO — Getters, Setters, and Constructors
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testMemberDTO() {
    // Test no-args constructor and setters
    Member member = new Member();
    member.setMemberId(1);
    member.setFirstName("Shivansh");
    member.setLastName("Mishra");
    member.setEmail("shivnsh01@gmail.com");
    member.setPhone("9876543210");
    member.setPassword("hashedpassword");
    member.setMembershipType("STUDENT");

    assertEquals(1, member.getMemberId());
    assertEquals("Shivansh", member.getFirstName());
    assertEquals("Mishra", member.getLastName());
    assertEquals("shivnsh01@gmail.com", member.getEmail());
    assertEquals("9876543210", member.getPhone());
    assertEquals("hashedpassword", member.getPassword());
    assertEquals("STUDENT", member.getMembershipType());

    // Test registration constructor (without ID and phone)
    Member member2 = new Member("Jane", "Smith", "jane@email.com", "password", "FACULTY");
    assertEquals("Jane", member2.getFirstName());
    assertEquals("FACULTY", member2.getMembershipType());
    assertNull(member2.getPhone()); // Phone not set in this constructor

    // Test full constructor with phone
    Member member3 =
        new Member(5, "Alice", "Brown", "alice@test.com", "1234567890", "pwd", "REGULAR");
    assertEquals(5, member3.getMemberId());
    assertEquals("1234567890", member3.getPhone());

    // Test toString contains key info
    String str = member.toString();
    assertTrue(str.contains("Shivansh"));
    assertTrue(str.contains("Mishra"));
    assertTrue(str.contains("shivnsh01@gmail.com"));
    assertTrue(str.contains("STUDENT"));
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 6: Transaction DTO — Getters, Setters, and Helper Fields
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testTransactionDTO() {
    // Test setters/getters
    Transaction tx = new Transaction();
    tx.setTransactionId(100);
    tx.setMemberId(1);
    tx.setBookId(5);
    tx.setIssueDate(Date.valueOf("2026-07-01"));
    tx.setDueDate(Date.valueOf("2026-07-15"));
    tx.setReturnDate(null);
    tx.setStatus("ISSUED");
    tx.setBookTitle("The Great Gatsby");
    tx.setMemberName("Shivansh Mishra");

    assertEquals(100, tx.getTransactionId());
    assertEquals(1, tx.getMemberId());
    assertEquals(5, tx.getBookId());
    assertEquals(Date.valueOf("2026-07-01"), tx.getIssueDate());
    assertEquals(Date.valueOf("2026-07-15"), tx.getDueDate());
    assertNull(tx.getReturnDate());
    assertEquals("ISSUED", tx.getStatus());
    assertEquals("The Great Gatsby", tx.getBookTitle());
    assertEquals("Shivansh Mishra", tx.getMemberName());

    // Test parameterized constructor
    Transaction tx2 =
        new Transaction(
            2,
            3,
            Date.valueOf("2026-06-01"),
            Date.valueOf("2026-06-15"),
            Date.valueOf("2026-06-14"),
            "RETURNED");
    assertEquals(2, tx2.getMemberId());
    assertEquals(3, tx2.getBookId());
    assertEquals("RETURNED", tx2.getStatus());

    // Test toString
    String str = tx.toString();
    assertTrue(str.contains("Shivansh Mishra"));
    assertTrue(str.contains("The Great Gatsby"));
    assertTrue(str.contains("ISSUED"));
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 7: Fine Calculation Edge Cases
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testFineCalculationEdgeCases() {
    Transaction tx = new Transaction();

    // Edge case 1: No due date set — fine should be 0
    tx.setDueDate(null);
    tx.setReturnDate(null);
    assertEquals(0.0, tx.getCalculatedFine(), 0.001);

    // Edge case 2: Returned exactly on the due date — no fine
    tx.setDueDate(Date.valueOf(LocalDate.now()));
    tx.setReturnDate(Date.valueOf(LocalDate.now()));
    assertEquals(0.0, tx.getCalculatedFine(), 0.001);

    // Edge case 3: Returned 1 day late — 5.0 fine
    tx.setDueDate(Date.valueOf(LocalDate.now().minusDays(1)));
    tx.setReturnDate(Date.valueOf(LocalDate.now()));
    assertEquals(5.0, tx.getCalculatedFine(), 0.001);

    // Edge case 4: Large overdue — 30 days late = 150.0 fine
    tx.setDueDate(Date.valueOf(LocalDate.now().minusDays(30)));
    tx.setReturnDate(Date.valueOf(LocalDate.now()));
    assertEquals(150.0, tx.getCalculatedFine(), 0.001);

    // Edge case 5: Returned early (before due date) — no fine
    tx.setDueDate(Date.valueOf(LocalDate.now().plusDays(10)));
    tx.setReturnDate(Date.valueOf(LocalDate.now()));
    assertEquals(0.0, tx.getCalculatedFine(), 0.001);
  }

  // ═══════════════════════════════════════════════════════════════
  //  TEST 8: Book Available Copies Logic
  // ═══════════════════════════════════════════════════════════════

  @Test
  public void testBookAvailability() {
    Book book = new Book("Test Book", "Test Author", "Fiction", 5, 5);

    // Simulate borrowing
    book.setAvailableCopies(book.getAvailableCopies() - 1);
    assertEquals(4, book.getAvailableCopies());
    assertEquals(5, book.getTotalCopies()); // Total doesn't change

    // Simulate returning
    book.setAvailableCopies(book.getAvailableCopies() + 1);
    assertEquals(5, book.getAvailableCopies());

    // Available can't exceed total (business rule)
    assertTrue(book.getAvailableCopies() <= book.getTotalCopies());

    // Test out of stock scenario
    book.setAvailableCopies(0);
    assertEquals(0, book.getAvailableCopies());
    assertTrue(book.getAvailableCopies() == 0); // No books available
  }
}
