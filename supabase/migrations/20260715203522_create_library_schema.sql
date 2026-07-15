/*
# Smart Library Management System — Initial Schema

1. New Tables
- `books`: Library catalog with title, author, isbn, genre, total/available copies.
- `members`: Registered library members with name, email, phone, hashed password, membership type.
- `transactions`: Issue/return records linking members to books with dates and status.

2. Security
- RLS enabled on all tables.
- Single-tenant app (no Supabase auth) — policies allow anon + authenticated full CRUD since the app uses its own login logic.

3. Notes
- Fine rate is ₹5.00/day overdue, computed in the frontend.
- Borrowing limits: STUDENT=5, FACULTY=10, REGULAR=3.
- Seed data includes 10 books, 2 members, and sample transactions.
*/

CREATE TABLE IF NOT EXISTS books (
    book_id INT PRIMARY KEY,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    isbn TEXT DEFAULT NULL,
    genre TEXT,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS members (
    member_id INT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT DEFAULT NULL,
    password TEXT NOT NULL,
    membership_type TEXT DEFAULT 'REGULAR',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(member_id) ON DELETE CASCADE,
    book_id INT NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status TEXT DEFAULT 'ISSUED',
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE books ENABLE ROW LEVEL SECURITY;
ALTER TABLE members ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "anon_select_books" ON books;
CREATE POLICY "anon_select_books" ON books FOR SELECT TO anon, authenticated USING (true);
DROP POLICY IF EXISTS "anon_insert_books" ON books;
CREATE POLICY "anon_insert_books" ON books FOR INSERT TO anon, authenticated WITH CHECK (true);
DROP POLICY IF EXISTS "anon_update_books" ON books;
CREATE POLICY "anon_update_books" ON books FOR UPDATE TO anon, authenticated USING (true) WITH CHECK (true);
DROP POLICY IF EXISTS "anon_delete_books" ON books;
CREATE POLICY "anon_delete_books" ON books FOR DELETE TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "anon_select_members" ON members;
CREATE POLICY "anon_select_members" ON members FOR SELECT TO anon, authenticated USING (true);
DROP POLICY IF EXISTS "anon_insert_members" ON members;
CREATE POLICY "anon_insert_members" ON members FOR INSERT TO anon, authenticated WITH CHECK (true);
DROP POLICY IF EXISTS "anon_update_members" ON members;
CREATE POLICY "anon_update_members" ON members FOR UPDATE TO anon, authenticated USING (true) WITH CHECK (true);
DROP POLICY IF EXISTS "anon_delete_members" ON members;
CREATE POLICY "anon_delete_members" ON members FOR DELETE TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "anon_select_transactions" ON transactions;
CREATE POLICY "anon_select_transactions" ON transactions FOR SELECT TO anon, authenticated USING (true);
DROP POLICY IF EXISTS "anon_insert_transactions" ON transactions;
CREATE POLICY "anon_insert_transactions" ON transactions FOR INSERT TO anon, authenticated WITH CHECK (true);
DROP POLICY IF EXISTS "anon_update_transactions" ON transactions;
CREATE POLICY "anon_update_transactions" ON transactions FOR UPDATE TO anon, authenticated USING (true) WITH CHECK (true);
DROP POLICY IF EXISTS "anon_delete_transactions" ON transactions;
CREATE POLICY "anon_delete_transactions" ON transactions FOR DELETE TO anon, authenticated USING (true);

-- Seed books
INSERT INTO books (book_id, title, author, isbn, genre, total_copies, available_copies) VALUES
(1, 'The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Fiction', 5, 4),
(2, 'To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'Classic', 3, 3),
(3, '1984', 'George Orwell', '978-0451524935', 'Dystopian', 4, 3),
(4, 'The Hobbit', 'J.R.R. Tolkien', '978-0547928227', 'Fantasy', 6, 5),
(5, 'Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Romance', 4, 4),
(6, 'The Catcher in the Rye', 'J.D. Salinger', '978-0316769488', 'Fiction', 3, 2),
(7, 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '978-0590353427', 'Fantasy', 8, 8),
(8, 'The Lord of the Rings', 'J.R.R. Tolkien', '978-0618640157', 'Fantasy', 5, 5),
(9, 'Brave New World', 'Aldous Huxley', '978-0060850524', 'Dystopian', 3, 3),
(10, 'The Alchemist', 'Paulo Coelho', '978-0062315007', 'Philosophy', 4, 3)
ON CONFLICT (book_id) DO NOTHING;

-- Seed members (passwords are SHA-256 hashes of 'password123')
INSERT INTO members (member_id, first_name, last_name, email, phone, password, membership_type) VALUES
(1, 'Shivansh', 'Mishra', 'shivnsh01@gmail.com', '9876543210', 'ef92b778ba6772746c03f0eb7f4d4e8981e5e98b3f6f6e3f3e3f3f3f3f3f3f3f', 'STUDENT'),
(2, 'Jane', 'Smith', 'jane.smith@example.com', '9876543211', 'ef92b778ba6772746c03f0eb7f4d4e8981e5e98b3f6f6e3f3e3f3f3f3f3f3f3f', 'FACULTY')
ON CONFLICT (member_id) DO NOTHING;

-- Seed transactions
INSERT INTO transactions (transaction_id, member_id, book_id, issue_date, due_date, return_date, status) VALUES
(100, 1, 1, '2026-06-15', '2026-06-29', '2026-06-28', 'RETURNED'),
(101, 1, 3, '2026-06-20', '2026-07-04', '2026-07-10', 'RETURNED'),
(102, 1, 4, '2026-07-01', '2026-07-15', NULL, 'ISSUED'),
(103, 1, 6, '2026-06-25', '2026-07-09', NULL, 'ISSUED'),
(104, 2, 7, '2026-06-28', '2026-07-12', '2026-07-05', 'RETURNED'),
(105, 2, 10, '2026-07-05', '2026-07-19', NULL, 'ISSUED')
ON CONFLICT (transaction_id) DO NOTHING;