// ── API Configuration ─────────────────────────────────────────────
const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  try {
    const response = await fetch(url, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers },
    });
    if (response.status === 204) return {};
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
    return data;
  } catch (err) {
    if (err.name === 'TypeError') throw new Error('Cannot connect to the library server. Make sure you have run: mvn exec:java');
    throw err;
  }
}

// ── Books API ─────────────────────────────────────────────────────
const api = {
  getBooks:   () => request('/books'),
  getMembers: () => request('/members'),
  getTransactions: () => request('/transactions'),
  getMemberTransactions: (id) => request(`/transactions?memberId=${id}`),

  addBook: (book)     => request('/books', { method: 'POST', body: JSON.stringify(book) }),
  updateBook: (id, b) => request(`/books?id=${id}`, { method: 'PUT', body: JSON.stringify(b) }),
  deleteBook: (id)    => request(`/books?id=${id}`, { method: 'DELETE' }),

  addMember: (m)      => request('/members', { method: 'POST', body: JSON.stringify(m) }),
  updateMember: (id, m) => request(`/members?id=${id}`, { method: 'PUT', body: JSON.stringify(m) }),
  deleteMember: (id)  => request(`/members?id=${id}`, { method: 'DELETE' }),

  issueBook: (memberId, bookId) => request('/transactions/issue', {
    method: 'POST', body: JSON.stringify({ member_id: memberId, book_id: bookId })
  }),
  returnBook: (txId) => request(`/transactions/return?id=${txId}`, {
    method: 'POST', body: JSON.stringify({})
  }),

  login: (email, password) => request('/login', {
    method: 'POST', body: JSON.stringify({ email, password })
  }),
};

// ── Fine Calculation ──────────────────────────────────────────────
function calculateFine(dueDate, returnDate) {
  if (!dueDate) return 0;
  const due = new Date(dueDate);
  const end = returnDate ? new Date(returnDate) : new Date();
  const days = Math.floor((end - due) / (1000 * 60 * 60 * 24));
  return days > 0 ? days * 5.0 : 0;
}

function isOverdue(dueDate, returnDate) {
  if (returnDate) return false;
  return new Date(dueDate) < new Date();
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ── Toast Notifications ───────────────────────────────────────────
function showToast(message, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icon = type === 'success' ? '✓' : '✕';
  toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
  container.appendChild(toast);
  requestAnimationFrame(() => { requestAnimationFrame(() => { toast.classList.add('show'); }); });
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ── Session Storage ───────────────────────────────────────────────
const session = {
  set: (user) => sessionStorage.setItem('lib_user', JSON.stringify(user)),
  get: () => { try { return JSON.parse(sessionStorage.getItem('lib_user')); } catch { return null; } },
  clear: () => sessionStorage.removeItem('lib_user'),
};
