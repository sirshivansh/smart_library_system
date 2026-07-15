// ── State ─────────────────────────────────────────────────────────
let books = [], members = [], transactions = [];
let editingBook = null, editingMember = null;
let currentTab = 'overview';

// ── Auth Guard ────────────────────────────────────────────────────
const user = session.get();
if (!user || user.role !== 'ADMIN') {
  window.location.href = 'index.html';
}

document.getElementById('user-name').textContent = user.name || 'Administrator';
document.getElementById('user-avatar').textContent = (user.name || 'A')[0].toUpperCase();

function logout() {
  session.clear();
  window.location.href = 'index.html';
}

// ── Tab Navigation ────────────────────────────────────────────────
const TABS = ['overview', 'books', 'members', 'transactions'];

function switchTab(tab) {
  currentTab = tab;
  TABS.forEach(t => {
    document.getElementById(`panel-${t}`)?.classList.toggle('active', t === tab);
    document.getElementById(`tab-${t}`)?.classList.toggle('active', t === tab);
    document.getElementById(`mob-tab-${t}`)?.classList.toggle('active', t === tab);
  });
}

// ── Load All Data ─────────────────────────────────────────────────
async function loadAll() {
  try {
    [books, members, transactions] = await Promise.all([api.getBooks(), api.getMembers(), api.getTransactions()]);
    renderStats();
    renderOverdue();
    renderBooks();
    renderMembers();
    renderTransactions();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ── Stats ─────────────────────────────────────────────────────────
function renderStats() {
  const totalTitles   = books.length;
  const totalCopies   = books.reduce((s, b) => s + (b.total_copies ?? 0), 0);
  const availCopies   = books.reduce((s, b) => s + (b.available_copies ?? 0), 0);
  const totalMembers  = members.length;
  const activeBorrows = transactions.filter(t => t.status === 'ISSUED').length;
  const overdueCount  = transactions.filter(t => t.status === 'ISSUED' && isOverdue(t.due_date, t.return_date)).length;
  const totalFines    = transactions.reduce((s, t) => s + calculateFine(t.due_date, t.return_date), 0);

  const stats = [
    { label: 'Book Titles', value: totalTitles, sub: 'In catalog', color: '#818CF8' },
    { label: 'Total Copies', value: totalCopies, sub: `${availCopies} available`, color: '#34D399' },
    { label: 'Members', value: totalMembers, sub: 'Registered', color: '#A78BFA' },
    { label: 'Active Borrows', value: activeBorrows, sub: 'Currently issued', color: '#60A5FA' },
    { label: 'Overdue', value: overdueCount, sub: 'Past due date', color: '#FB7185' },
    { label: 'Total Fines', value: `₹${totalFines.toFixed(2)}`, sub: 'Accumulated fines', color: '#FCD34D' },
  ];

  document.getElementById('stat-cards').innerHTML = stats.map(s => `
    <div class="glass-card stat-card" style="--color:${s.color}">
      <div class="stat-label">${s.label}</div>
      <div class="stat-value">${s.value}</div>
      <div class="stat-sub">${s.sub}</div>
    </div>
  `).join('');
}

// ── Overdue Table ─────────────────────────────────────────────────
function renderOverdue() {
  const overdueList = transactions.filter(t => t.status === 'ISSUED' && isOverdue(t.due_date, t.return_date));
  const memberMap = Object.fromEntries(members.map(m => [m.member_id, m]));
  const bookMap   = Object.fromEntries(books.map(b => [b.book_id, b]));

  if (overdueList.length === 0) {
    document.getElementById('overdue-table').innerHTML = `<div class="empty-state"><div class="empty-icon">✅</div>No overdue books! All borrowers are on schedule.</div>`;
    return;
  }

  document.getElementById('overdue-table').innerHTML = `
    <div class="table-wrap">
      <table>
        <thead><tr>
          <th>TxID</th><th>Member</th><th>Book</th><th>Due Date</th><th>Days Late</th><th class="text-right">Fine</th>
        </tr></thead>
        <tbody>${overdueList.map(t => {
          const m = memberMap[t.member_id];
          const b = bookMap[t.book_id];
          const fine = calculateFine(t.due_date, null);
          const daysLate = Math.floor((new Date() - new Date(t.due_date)) / (1000*60*60*24));
          return `<tr>
            <td class="mono">#${t.transaction_id}</td>
            <td class="text-white">${m ? `${m.first_name} ${m.last_name}` : `Member #${t.member_id}`}</td>
            <td>${b ? b.title : `Book #${t.book_id}`}</td>
            <td>${formatDate(t.due_date)}</td>
            <td><span class="badge badge-red">${daysLate} days</span></td>
            <td class="text-right fine-positive">₹${fine.toFixed(2)}</td>
          </tr>`;
        }).join('')}</tbody>
      </table>
    </div>`;
}

// ── Books Table ───────────────────────────────────────────────────
function renderBooks() {
  const q = (document.getElementById('book-search')?.value || '').toLowerCase();
  const filtered = books.filter(b =>
    b.title?.toLowerCase().includes(q) || b.author?.toLowerCase().includes(q) || b.genre?.toLowerCase().includes(q)
  );

  document.getElementById('book-count').textContent = `${filtered.length} books${q ? ' found' : ' in catalog'}`;

  if (filtered.length === 0) {
    document.getElementById('books-table').innerHTML = `<div class="empty-state"><div class="empty-icon">📚</div>No books found</div>`;
    return;
  }

  document.getElementById('books-table').innerHTML = `
    <table>
      <thead><tr>
        <th>ID</th><th>Title</th><th>Author</th><th>Genre</th><th>ISBN</th><th>Copies</th><th>Available</th><th class="text-right">Actions</th>
      </tr></thead>
      <tbody>${filtered.map(b => `<tr>
        <td class="mono">#${b.book_id}</td>
        <td class="text-white">${b.title}</td>
        <td>${b.author}</td>
        <td>${b.genre ? `<span class="badge badge-indigo">${b.genre}</span>` : '—'}</td>
        <td class="mono" style="font-size:11px;">${b.isbn || '—'}</td>
        <td>${b.total_copies}</td>
        <td><span class="badge ${b.available_copies > 0 ? 'badge-green' : 'badge-red'}">${b.available_copies}</span></td>
        <td class="text-right">
          <div class="d-flex gap-8" style="justify-content:flex-end;">
            <button class="btn btn-ghost btn-sm btn-icon" title="Edit" onclick="openBookModal(${JSON.stringify(JSON.stringify(b))})">✏️</button>
            <button class="btn btn-danger btn-sm btn-icon" title="Delete" onclick="deleteBook(${b.book_id})">🗑️</button>
          </div>
        </td>
      </tr>`).join('')}</tbody>
    </table>`;
}

// ── Members Table ─────────────────────────────────────────────────
function renderMembers() {
  const q = (document.getElementById('member-search')?.value || '').toLowerCase();
  const filtered = members.filter(m =>
    `${m.first_name} ${m.last_name}`.toLowerCase().includes(q) || m.email?.toLowerCase().includes(q)
  );

  document.getElementById('member-count').textContent = `${filtered.length} registered members`;

  if (filtered.length === 0) {
    document.getElementById('members-table').innerHTML = `<div class="empty-state"><div class="empty-icon">👥</div>No members found</div>`;
    return;
  }

  const badgeClass = { FACULTY: 'badge-indigo', STUDENT: 'badge-green', REGULAR: 'badge-orange' };

  document.getElementById('members-table').innerHTML = `
    <table>
      <thead><tr>
        <th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Type</th><th class="text-right">Actions</th>
      </tr></thead>
      <tbody>${filtered.map(m => `<tr>
        <td class="mono">#${m.member_id}</td>
        <td class="text-white">${m.first_name} ${m.last_name}</td>
        <td>${m.email}</td>
        <td>${m.phone || '—'}</td>
        <td><span class="badge ${badgeClass[m.membership_type] || 'badge-orange'}">${m.membership_type}</span></td>
        <td class="text-right">
          <div class="d-flex gap-8" style="justify-content:flex-end;">
            <button class="btn btn-ghost btn-sm btn-icon" title="Edit" onclick="openMemberModal(${JSON.stringify(JSON.stringify(m))})">✏️</button>
            <button class="btn btn-danger btn-sm btn-icon" title="Delete" onclick="deleteMember(${m.member_id})">🗑️</button>
          </div>
        </td>
      </tr>`).join('')}</tbody>
    </table>`;
}

// ── Transactions Table ────────────────────────────────────────────
function renderTransactions() {
  const memberMap = Object.fromEntries(members.map(m => [m.member_id, m]));
  const bookMap   = Object.fromEntries(books.map(b => [b.book_id, b]));

  document.getElementById('tx-count').textContent = `${transactions.length} total transactions`;

  if (transactions.length === 0) {
    document.getElementById('transactions-table').innerHTML = `<div class="empty-state"><div class="empty-icon">📋</div>No transactions yet</div>`;
    return;
  }

  document.getElementById('transactions-table').innerHTML = `
    <table>
      <thead><tr>
        <th>TxID</th><th>Member</th><th>Book</th><th>Issued</th><th>Due</th><th>Returned</th><th>Status</th><th class="text-right">Fine</th>
      </tr></thead>
      <tbody>${transactions.map(t => {
        const m = memberMap[t.member_id];
        const b = bookMap[t.book_id];
        const fine = calculateFine(t.due_date, t.return_date);
        const overdue = isOverdue(t.due_date, t.return_date);
        const statusBadge = t.status === 'RETURNED'
          ? '<span class="badge badge-green">RETURNED</span>'
          : overdue ? '<span class="badge badge-red">OVERDUE</span>'
          : '<span class="badge badge-orange">ISSUED</span>';
        return `<tr>
          <td class="mono">#${t.transaction_id}</td>
          <td class="text-white">${m ? `${m.first_name} ${m.last_name}` : `Member #${t.member_id}`}</td>
          <td>${b ? b.title : `Book #${t.book_id}`}</td>
          <td>${formatDate(t.issue_date)}</td>
          <td>${formatDate(t.due_date)}</td>
          <td>${formatDate(t.return_date)}</td>
          <td>${statusBadge}</td>
          <td class="text-right ${fine > 0 ? 'fine-positive' : 'text-dim'}">₹${fine.toFixed(2)}</td>
        </tr>`;
      }).join('')}</tbody>
    </table>`;
}

// ── Book Modal ────────────────────────────────────────────────────
function openBookModal(bookJson) {
  editingBook = bookJson ? JSON.parse(bookJson) : null;
  document.getElementById('book-modal-title').textContent = editingBook ? 'Edit Book' : 'Add New Book';
  document.getElementById('book-title').value  = editingBook?.title  || '';
  document.getElementById('book-author').value = editingBook?.author || '';
  document.getElementById('book-isbn').value   = editingBook?.isbn   || '';
  document.getElementById('book-genre').value  = editingBook?.genre  || '';
  document.getElementById('book-copies').value = editingBook?.total_copies || '';
  document.getElementById('book-modal').classList.add('open');
}

document.getElementById('book-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = document.getElementById('book-save-btn');
  btn.disabled = true; btn.textContent = 'Saving...';
  const payload = {
    title: document.getElementById('book-title').value.trim(),
    author: document.getElementById('book-author').value.trim(),
    isbn: document.getElementById('book-isbn').value.trim() || null,
    genre: document.getElementById('book-genre').value.trim(),
    total_copies: parseInt(document.getElementById('book-copies').value),
  };
  try {
    if (editingBook) {
      await api.updateBook(editingBook.book_id, payload);
      showToast('Book updated successfully');
    } else {
      await api.addBook(payload);
      showToast('Book added successfully');
    }
    closeModal('book-modal');
    await loadAll();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Save Book';
  }
});

async function deleteBook(id) {
  if (!confirm('Delete this book permanently?')) return;
  try {
    await api.deleteBook(id);
    showToast('Book deleted');
    await loadAll();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ── Member Modal ──────────────────────────────────────────────────
function openMemberModal(memberJson) {
  editingMember = memberJson ? JSON.parse(memberJson) : null;
  document.getElementById('member-modal-title').textContent = editingMember ? 'Edit Member' : 'Add New Member';
  document.getElementById('pass-hint').style.display = editingMember ? 'inline' : 'none';
  document.getElementById('mem-first').value = editingMember?.first_name || '';
  document.getElementById('mem-last').value  = editingMember?.last_name  || '';
  document.getElementById('mem-email').value = editingMember?.email || '';
  document.getElementById('mem-phone').value = editingMember?.phone || '';
  document.getElementById('mem-type').value  = editingMember?.membership_type || 'STUDENT';
  document.getElementById('mem-pass').value  = '';
  document.getElementById('member-modal').classList.add('open');
}

document.getElementById('member-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = document.getElementById('member-save-btn');
  btn.disabled = true; btn.textContent = 'Saving...';
  const payload = {
    first_name: document.getElementById('mem-first').value.trim(),
    last_name: document.getElementById('mem-last').value.trim(),
    email: document.getElementById('mem-email').value.trim(),
    phone: document.getElementById('mem-phone').value.trim() || null,
    membership_type: document.getElementById('mem-type').value,
    password: document.getElementById('mem-pass').value || undefined,
  };
  try {
    if (editingMember) {
      await api.updateMember(editingMember.member_id, payload);
      showToast('Member updated successfully');
    } else {
      await api.addMember(payload);
      showToast('Member registered successfully');
    }
    closeModal('member-modal');
    await loadAll();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Save Member';
  }
});

async function deleteMember(id) {
  if (!confirm('Delete this member and all their transactions permanently?')) return;
  try {
    await api.deleteMember(id);
    showToast('Member deleted');
    await loadAll();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ── Modal Helpers ─────────────────────────────────────────────────
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

// Close modals on backdrop click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) closeModal(overlay.id);
  });
});

// ── Init ──────────────────────────────────────────────────────────
loadAll();
