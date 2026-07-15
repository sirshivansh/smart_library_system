// Initial mock seed data matching our database
const INITIAL_BOOKS = [
  { book_id: 1, title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', isbn: '978-0743273565', genre: 'Fiction', total_copies: 5, available_copies: 4 },
  { book_id: 2, title: 'To Kill a Mockingbird', author: 'Harper Lee', isbn: '978-0061120084', genre: 'Classic', total_copies: 3, available_copies: 3 },
  { book_id: 3, title: '1984', author: 'George Orwell', isbn: '978-0451524935', genre: 'Dystopian', total_copies: 4, available_copies: 3 },
  { book_id: 4, title: 'The Hobbit', author: 'J.R.R. Tolkien', isbn: '978-0547928227', genre: 'Fantasy', total_copies: 6, available_copies: 5 },
  { book_id: 5, title: 'Pride and Prejudice', author: 'Jane Austen', isbn: '978-0141439518', genre: 'Romance', total_copies: 4, available_copies: 4 },
  { book_id: 6, title: 'The Catcher in the Rye', author: 'J.D. Salinger', isbn: '978-0316769488', genre: 'Fiction', total_copies: 3, available_copies: 2 },
  { book_id: 7, title: 'Harry Potter and the Sorcerer\'s Stone', author: 'J.K. Rowling', isbn: '978-0590353427', genre: 'Fantasy', total_copies: 8, available_copies: 8 },
  { book_id: 8, title: 'The Lord of the Rings', author: 'J.R.R. Tolkien', isbn: '978-0618640157', genre: 'Fantasy', total_copies: 5, available_copies: 5 },
  { book_id: 9, title: 'Brave New World', author: 'Aldous Huxley', isbn: '978-0060850524', genre: 'Dystopian', total_copies: 3, available_copies: 3 },
  { book_id: 10, title: 'The Alchemist', author: 'Paulo Coelho', isbn: '978-0062315007', genre: 'Philosophy', total_copies: 4, available_copies: 3 }
];

const INITIAL_MEMBERS = [
  { member_id: 1, first_name: 'Shivansh', last_name: 'Mishra', email: 'shivnsh01@gmail.com', phone: '9876543210', password: 'password123', membership_type: 'STUDENT' },
  { member_id: 2, first_name: 'Jane', last_name: 'Smith', email: 'jane.smith@example.com', phone: '9876543211', password: 'password123', membership_type: 'FACULTY' }
];

const INITIAL_TRANSACTIONS = [
  { transaction_id: 100, member_id: 1, book_id: 1, issue_date: '2026-06-15', due_date: '2026-06-29', return_date: '2026-06-28', status: 'RETURNED' },
  { transaction_id: 101, member_id: 1, book_id: 3, issue_date: '2026-06-20', due_date: '2026-07-04', return_date: '2026-07-10', status: 'RETURNED' },
  { transaction_id: 102, member_id: 1, book_id: 4, issue_date: '2026-07-01', due_date: '2026-07-15', return_date: null, status: 'ISSUED' },
  { transaction_id: 103, member_id: 1, book_id: 6, issue_date: '2026-06-25', due_date: '2026-07-09', return_date: null, status: 'ISSUED' },
  { transaction_id: 104, member_id: 2, book_id: 7, issue_date: '2026-06-28', due_date: '2026-07-12', return_date: '2026-07-05', status: 'RETURNED' },
  { transaction_id: 105, member_id: 2, book_id: 10, issue_date: '2026-07-05', due_date: '2026-07-19', return_date: null, status: 'ISSUED' }
];

function getStorage(key, initialData) {
  if (!localStorage.getItem(key)) {
    localStorage.setItem(key, JSON.stringify(initialData));
  }
  return JSON.parse(localStorage.getItem(key));
}

function setStorage(key, data) {
  localStorage.setItem(key, JSON.stringify(data));
}

export function dbFetchBooks() {
  return getStorage('lib_books', INITIAL_BOOKS);
}

export function dbFetchMembers() {
  return getStorage('lib_members', INITIAL_MEMBERS);
}

export function dbFetchTransactions() {
  const txs = getStorage('lib_transactions', INITIAL_TRANSACTIONS);
  const books = dbFetchBooks();
  const members = dbFetchMembers();

  // Hydrate transaction with titles and names
  return txs.map(t => {
    const book = books.find(b => b.book_id === t.book_id);
    const member = members.find(m => m.member_id === t.member_id);
    return {
      ...t,
      bookTitle: book ? book.title : `Book #${t.book_id}`,
      memberName: member ? `${member.first_name} ${member.last_name}` : `Member #${t.member_id}`
    };
  });
}

export function dbFetchMemberTransactions(memberId) {
  return dbFetchTransactions().filter(t => t.member_id === memberId);
}

export function dbAddBook(book) {
  const books = dbFetchBooks();
  const nextId = books.reduce((max, b) => Math.max(max, b.book_id), 0) + 1;
  const newBook = {
    ...book,
    book_id: nextId,
    available_copies: book.total_copies
  };
  books.push(newBook);
  setStorage('lib_books', books);
  return newBook;
}

export function dbUpdateBook(bookId, updates) {
  const books = dbFetchBooks();
  const index = books.findIndex(b => b.book_id === bookId);
  if (index === -1) throw new Error('Book not found');

  const updatedBook = {
    ...books[index],
    ...updates
  };
  books[index] = updatedBook;
  setStorage('lib_books', books);
  return updatedBook;
}

export function dbDeleteBook(bookId) {
  const books = dbFetchBooks();
  const filtered = books.filter(b => b.book_id !== bookId);
  setStorage('lib_books', filtered);
}

export function dbAddMember(member) {
  const members = dbFetchMembers();
  const nextId = members.reduce((max, m) => Math.max(max, m.member_id), 0) + 1;
  const newMember = {
    ...member,
    member_id: nextId
  };
  members.push(newMember);
  setStorage('lib_members', members);
  return newMember;
}

export function dbUpdateMember(memberId, updates) {
  const members = dbFetchMembers();
  const index = members.findIndex(m => m.member_id === memberId);
  if (index === -1) throw new Error('Member not found');

  const updatedMember = {
    ...members[index],
    ...updates
  };
  members[index] = updatedMember;
  setStorage('lib_members', members);
  return updatedMember;
}

export function dbDeleteMember(memberId) {
  const members = dbFetchMembers();
  const filtered = members.filter(m => m.member_id !== memberId);
  setStorage('lib_members', filtered);
}

export function dbIssueBookTx(memberId, bookId) {
  const books = dbFetchBooks();
  const bookIndex = books.findIndex(b => b.book_id === bookId);
  if (bookIndex === -1) throw new Error('Book not found');
  if (books[bookIndex].available_copies <= 0) throw new Error('No available copies');

  // Decrement copies
  books[bookIndex].available_copies -= 1;
  setStorage('lib_books', books);

  // Add transaction
  const txs = getStorage('lib_transactions', INITIAL_TRANSACTIONS);
  const nextId = txs.reduce((max, t) => Math.max(max, t.transaction_id), 0) + 1;
  
  const today = new Date();
  const due = new Date(today);
  due.setDate(due.getDate() + 14);

  const newTx = {
    transaction_id: nextId,
    member_id: memberId,
    book_id: bookId,
    issue_date: today.toISOString().slice(0, 10),
    due_date: due.toISOString().slice(0, 10),
    return_date: null,
    status: 'ISSUED'
  };

  txs.push(newTx);
  setStorage('lib_transactions', txs);
  return newTx;
}

export function dbReturnBookTx(transactionId) {
  const txs = getStorage('lib_transactions', INITIAL_TRANSACTIONS);
  const index = txs.findIndex(t => t.transaction_id === transactionId);
  if (index === -1) throw new Error('Transaction not found');

  const tx = txs[index];
  if (tx.status === 'RETURNED') return tx;

  const today = new Date().toISOString().slice(0, 10);
  tx.return_date = today;
  tx.status = 'RETURNED';
  setStorage('lib_transactions', txs);

  // Increment copies
  const books = dbFetchBooks();
  const bookIndex = books.findIndex(b => b.book_id === tx.book_id);
  if (bookIndex !== -1 && books[bookIndex].available_copies < books[bookIndex].total_copies) {
    books[bookIndex].available_copies += 1;
    setStorage('lib_books', books);
  }

  return tx;
}

export function dbLogin(emailOrUsername, password) {
  const members = dbFetchMembers();
  const member = members.find(m => m.email === emailOrUsername);
  if (!member) return null;
  if (password !== 'password123') return null; // accept demo password
  return member;
}
