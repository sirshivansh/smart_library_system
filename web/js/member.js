// ── State ─────────────────────────────────────────────────────────
let allBooks = [], myTransactions = [], memberProfile = null;
let pendingBorrowBook = null;

// ── Auth Guard ────────────────────────────────────────────────────
const user = session.get();
if (!user || user.role === 'ADMIN') {
  window.location.href = user?.role === 'ADMIN' ? 'admin.html' : 'index.html';
}

document.getElementById('user-name').textContent = user.name || 'Member';
document.getElementById('user-avatar').textContent = (user.name || 'M')[0].toUpperCase();

function logout() {
  session.clear();
  window.location.href = 'index.html';
}

// ── Tab Navigation ────────────────────────────────────────────────
const TABS = ['browse', 'borrowed', 'profile'];

function switchTab(tab) {
  TABS.forEach(t => {
    document.getElementById(`panel-${t}`)?.classList.toggle('active', t === tab);
    document.getElementById(`tab-${t}`)?.classList.toggle('active', t === tab);
    document.getElementById(`mob-tab-${t}`)?.classList.toggle('active', t === tab);
  });
}

// ── Load All Data ─────────────────────────────────────────────────
async function loadAll() {
  try {
    const [books, txs] = await Promise.all([
      api.getBooks(),
      api.getMemberTransactions(user.id),
    ]);
    allBooks = books;
    myTransactions = txs;
    renderBrowse();
    renderMyBooks();
    renderProfile();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ── Browse Books ──────────────────────────────────────────────────
function renderBrowse() {
  const q = (document.getElementById('browse-search')?.value || '').toLowerCase();
  const filtered = allBooks.filter(b =>
    b.title?.toLowerCase().includes(q) || b.author?.toLowerCase().includes(q) || b.genre?.toLowerCase().includes(q)
  );

  document.getElementById('browse-count').textContent = `${filtered.length} books available`;

  if (filtered.length === 0) {
    document.getElementById('book-grid').innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><div class="empty-icon">📚</div>No books found for "<b>${q}</b>"</div>`;
    return;
  }

  // Check which books are actively borrowed by this member
  const borrowedBookIds = new Set(
    myTransactions.filter(t => t.status === 'ISSUED').map(t => t.book_id)
  );

  document.getElementById('book-grid').innerHTML = filtered.map(b => {
    const borrowed = borrowedBookIds.has(b.book_id);
    const canBorrow = !borrowed && b.available_copies > 0;
    const btnLabel = borrowed ? '📖 Currently Borrowed' : b.available_copies > 0 ? 'Borrow Book' : 'Unavailable';
    const btnClass = borrowed ? 'btn-ghost' : canBorrow ? 'btn-primary' : 'btn-ghost';
    const availClass = b.available_copies > 0 ? 'avail-ok' : 'avail-none';
    const availText = b.available_copies > 0 ? `✓ ${b.available_copies} of ${b.total_copies} available` : `✕ All copies borrowed`;

    return `
      <div class="glass-card book-card">
        ${b.genre ? `<div class="book-card-genre">${b.genre}</div>` : ''}
        <div class="book-card-title">${b.title}</div>
        <div class="book-card-author">by ${b.author}</div>
        <div class="book-card-avail ${availClass}">${availText}</div>
        <button class="btn ${btnClass} btn-sm" style="width:100%;"
          ${!canBorrow ? 'disabled' : ''}
          onclick="openBorrowModal(${JSON.stringify(JSON.stringify(b))})">
          ${btnLabel}
        </button>
      </div>`;
  }).join('');
}

// ── My Books ──────────────────────────────────────────────────────
function renderMyBooks() {
  const active = myTransactions.filter(t => t.status === 'ISSUED');
  const history = myTransactions.filter(t => t.status === 'RETURNED');
  const bookMap = Object.fromEntries(allBooks.map(b => [b.book_id, b]));

  // Active borrows
  if (active.length === 0) {
    document.getElementById('active-borrows').innerHTML = `<div class="empty-state"><div class="empty-icon">📚</div>You haven't borrowed any books yet. <a href="#" onclick="switchTab('browse')" style="color:var(--indigo-400);">Browse the catalog</a></div>`;
  } else {
    document.getElementById('active-borrows').innerHTML = `
      <div class="table-wrap">
        <table>
          <thead><tr>
            <th>Book</th><th>Issued</th><th>Due Date</th><th>Status</th><th class="text-right">Fine Accrued</th><th class="text-right">Action</th>
          </tr></thead>
          <tbody>${active.map(t => {
            const b = bookMap[t.book_id];
            const fine = calculateFine(t.due_date, null);
            const overdue = isOverdue(t.due_date, null);
            const statusBadge = overdue
              ? `<span class="badge badge-red">OVERDUE</span>`
              : `<span class="badge badge-orange">ISSUED</span>`;
            return `<tr>
              <td class="text-white">${b ? b.title : `Book #${t.book_id}`}<br><span style="font-size:11px;color:var(--text-dim);">${b ? b.author : ''}</span></td>
              <td>${formatDate(t.issue_date)}</td>
              <td>${formatDate(t.due_date)}</td>
              <td>${statusBadge}</td>
              <td class="text-right ${fine > 0 ? 'fine-positive' : 'text-dim'}">₹${fine.toFixed(2)}</td>
              <td class="text-right">
                <button class="btn btn-success btn-sm" onclick="returnBook(${t.transaction_id})">Return</button>
              </td>
            </tr>`;
          }).join('')}</tbody>
        </table>
      </div>`;
  }

  // History
  if (history.length === 0) {
    document.getElementById('borrow-history').innerHTML = `<div class="empty-state"><div class="empty-icon">📋</div>No return history yet</div>`;
  } else {
    document.getElementById('borrow-history').innerHTML = `
      <div class="table-wrap">
        <table>
          <thead><tr>
            <th>Book</th><th>Issued</th><th>Returned</th><th class="text-right">Fine Paid</th>
          </tr></thead>
          <tbody>${history.map(t => {
            const b = bookMap[t.book_id];
            const fine = calculateFine(t.due_date, t.return_date);
            return `<tr>
              <td class="text-white">${b ? b.title : `Book #${t.book_id}`}<br><span style="font-size:11px;color:var(--text-dim);">${b ? b.author : ''}</span></td>
              <td>${formatDate(t.issue_date)}</td>
              <td>${formatDate(t.return_date)}</td>
              <td class="text-right ${fine > 0 ? 'fine-positive' : 'text-emerald'}">₹${fine.toFixed(2)}</td>
            </tr>`;
          }).join('')}</tbody>
        </table>
      </div>`;
  }
}

// ── Profile ───────────────────────────────────────────────────────
function renderProfile() {
  const totalBorrowed  = myTransactions.length;
  const activeCount    = myTransactions.filter(t => t.status === 'ISSUED').length;
  const returnedCount  = myTransactions.filter(t => t.status === 'RETURNED').length;
  const totalFines     = myTransactions.reduce((s, t) => s + calculateFine(t.due_date, t.return_date), 0);
  const typeColors     = { STUDENT: 'badge-green', FACULTY: 'badge-indigo', REGULAR: 'badge-orange' };

  document.getElementById('profile-card').innerHTML = `
    <div style="display:flex;align-items:center;gap:16px;margin-bottom:24px;">
      <div style="width:60px;height:60px;border-radius:50%;background:linear-gradient(135deg,#10B981,#6366F1);display:flex;align-items:center;justify-content:center;font-size:24px;font-weight:800;color:white;flex-shrink:0;">
        ${(user.name || 'M')[0].toUpperCase()}
      </div>
      <div>
        <div style="font-size:20px;font-weight:800;color:var(--text-primary);">${user.name || 'Member'}</div>
        <div style="font-size:13px;color:var(--text-dim);">${user.email || ''}</div>
        <div style="margin-top:4px;"><span class="badge ${typeColors[user.membershipType] || 'badge-orange'}">${user.membershipType || 'STUDENT'}</span></div>
      </div>
    </div>

    <div class="divider-h"></div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-top:16px;">
      <div class="glass-card" style="padding:16px;text-align:center;">
        <div style="font-size:28px;font-weight:800;color:var(--indigo-400);">${totalBorrowed}</div>
        <div style="font-size:12px;color:var(--text-dim);">Total Borrowed</div>
      </div>
      <div class="glass-card" style="padding:16px;text-align:center;">
        <div style="font-size:28px;font-weight:800;color:var(--amber);">${activeCount}</div>
        <div style="font-size:12px;color:var(--text-dim);">Currently Active</div>
      </div>
      <div class="glass-card" style="padding:16px;text-align:center;">
        <div style="font-size:28px;font-weight:800;color:var(--emerald);">${returnedCount}</div>
        <div style="font-size:12px;color:var(--text-dim);">Returned</div>
      </div>
      <div class="glass-card" style="padding:16px;text-align:center;">
        <div style="font-size:28px;font-weight:800;color:${totalFines > 0 ? 'var(--rose)' : 'var(--emerald)'};">₹${totalFines.toFixed(0)}</div>
        <div style="font-size:12px;color:var(--text-dim);">Total Fines</div>
      </div>
    </div>

    <div class="divider-h"></div>
    <div style="font-size:12px;color:var(--text-dim);">
      <div style="margin-bottom:6px;">📋 Member ID: <span style="color:var(--text-muted);">#${user.id}</span></div>
      <div>📧 Email: <span style="color:var(--text-muted);">${user.email}</span></div>
    </div>`;
}

// ── Borrow Book ───────────────────────────────────────────────────
function openBorrowModal(bookJson) {
  pendingBorrowBook = JSON.parse(bookJson);
  document.getElementById('borrow-book-title').textContent = pendingBorrowBook.title;
  document.getElementById('borrow-book-author').textContent = `by ${pendingBorrowBook.author}`;
  document.getElementById('borrow-modal').classList.add('open');
}

async function confirmBorrow() {
  if (!pendingBorrowBook) return;
  const btn = document.getElementById('confirm-borrow-btn');
  btn.disabled = true; btn.textContent = 'Processing...';
  try {
    await api.issueBook(user.id, pendingBorrowBook.book_id);
    closeModal('borrow-modal');
    showToast(`"${pendingBorrowBook.title}" borrowed successfully!`);
    await loadAll();
    switchTab('borrowed');
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Confirm Borrow';
    pendingBorrowBook = null;
  }
}

// ── Return Book ───────────────────────────────────────────────────
async function returnBook(txId) {
  if (!confirm('Return this book?')) return;
  try {
    await api.returnBook(txId);
    showToast('Book returned successfully!');
    await loadAll();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ── Modal Helper ──────────────────────────────────────────────────
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}
document.querySelectorAll('.modal-overlay').forEach(o => {
  o.addEventListener('click', (e) => { if (e.target === o) closeModal(o.id); });
});

// ── Init ──────────────────────────────────────────────────────────
loadAll();
