-- ============================================================
-- SCHEMA: Smart Library Management System (Book Haven)
-- Author: Shivansh
-- Database: MySQL 8.x
-- Architecture: MVC (Model-View-Controller) with DAO Pattern
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_library_system;
USE smart_library_system;

-- ============================================================
-- TABLE: books
-- Description: Stores the library's book catalog with
--              inventory tracking (total vs available copies).
-- ============================================================
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) DEFAULT NULL,
    genre VARCHAR(100),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_genre (genre)
);

-- ============================================================
-- TABLE: members
-- Description: Stores registered library members with
--              SHA-256 hashed passwords and membership types.
-- ============================================================
CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    password VARCHAR(255) NOT NULL,
    membership_type VARCHAR(50) DEFAULT 'REGULAR',
    INDEX idx_email (email)
);

-- ============================================================
-- TABLE: transactions
-- Description: Records all book issue/return transactions
--              with date tracking for fine computation.
--              Fine rate: 5.0 rupees per overdue day.
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    book_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(50) DEFAULT 'ISSUED',
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    INDEX idx_member_status (member_id, status),
    INDEX idx_book_status (book_id, status)
);

-- ============================================================
-- SEED DATA: Initial Book Catalog
-- ============================================================
INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) 
SELECT 'The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Fiction', 5, 4
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Great Gatsby');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) 
SELECT 'To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'Classic', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'To Kill a Mockingbird');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) 
SELECT '1984', 'George Orwell', '978-0451524935', 'Dystopian', 4, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '1984');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) 
SELECT 'The Hobbit', 'J.R.R. Tolkien', '978-0547928227', 'Fantasy', 6, 6
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Hobbit');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Romance', 4, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Pride and Prejudice');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'The Catcher in the Rye', 'J.D. Salinger', '978-0316769488', 'Fiction', 3, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Catcher in the Rye');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '978-0590353427', 'Fantasy', 8, 8
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Harry Potter and the Sorcerer''s Stone');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'The Lord of the Rings', 'J.R.R. Tolkien', '978-0618640157', 'Fantasy', 5, 5
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Lord of the Rings');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Brave New World', 'Aldous Huxley', '978-0060850524', 'Dystopian', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Brave New World');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'The Alchemist', 'Paulo Coelho', '978-0062315007', 'Philosophy', 4, 4
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Alchemist');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Thinking, Fast and Slow', 'Daniel Kahneman', '978-0374533557', 'Psychology', 4, 4
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Thinking, Fast and Slow');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'History', 5, 5
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Sapiens: A Brief History of Humankind');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Atomic Habits', 'James Clear', '978-0735211292', 'Self-Help', 7, 7
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Atomic Habits');

INSERT INTO books (title, author, isbn, genre, total_copies, available_copies)
SELECT 'Dune', 'Frank Herbert', '978-0441172719', 'Science Fiction', 6, 6
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Dune');

-- ============================================================
-- SEED DATA: Default Members (passwords are hashed via app)
-- ============================================================
INSERT INTO members (first_name, last_name, email, phone, password, membership_type)
SELECT 'Shivansh', 'Mishra', 'shivnsh01@gmail.com', '9876543210', 'password123', 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'shivnsh01@gmail.com');

INSERT INTO members (first_name, last_name, email, phone, password, membership_type)
SELECT 'Jane', 'Smith', 'jane.smith@example.com', '9876543211', 'password123', 'FACULTY'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'jane.smith@example.com');

INSERT INTO members (first_name, last_name, email, phone, password, membership_type)
SELECT 'Alice', 'Johnson', 'alice.j@example.com', '9876543212', 'password123', 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'alice.j@example.com');

INSERT INTO members (first_name, last_name, email, phone, password, membership_type)
SELECT 'Bob', 'Williams', 'bob.w@example.com', '9876543213', 'password123', 'REGULAR'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'bob.w@example.com');

-- ============================================================
-- SEED DATA: Transactions
-- ============================================================
-- Note: Adjusting available_copies in the books table to account for ISSUED books.
-- Book 1 (The Great Gatsby) - Issued
INSERT INTO transactions (member_id, book_id, issue_date, due_date, return_date, status)
SELECT 1, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), NULL, 'ISSUED'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE member_id = 1 AND book_id = 1);

-- Book 3 (1984) - Issued (Overdue)
INSERT INTO transactions (member_id, book_id, issue_date, due_date, return_date, status)
SELECT 1, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 'ISSUED'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE member_id = 1 AND book_id = 3);

-- Book 5 (Pride and Prejudice) - Returned
INSERT INTO transactions (member_id, book_id, issue_date, due_date, return_date, status)
SELECT 1, 5, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'RETURNED'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE member_id = 1 AND book_id = 5 AND status = 'RETURNED');

-- Book 6 (The Catcher in the Rye) - Issued
INSERT INTO transactions (member_id, book_id, issue_date, due_date, return_date, status)
SELECT 2, 6, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 'ISSUED'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE member_id = 2 AND book_id = 6);

-- Book 5 (Pride and Prejudice) - Issued
INSERT INTO transactions (member_id, book_id, issue_date, due_date, return_date, status)
SELECT 3, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 13 DAY), NULL, 'ISSUED'
WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE member_id = 3 AND book_id = 5 AND status = 'ISSUED');
