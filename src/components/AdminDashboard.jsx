import { useState, useEffect, useCallback } from 'react'
import Logo from './Logo'
import StatCard from './StatCard'
import BookModal, { MemberModal } from './Modals'
import { fetchBooks, fetchMembers, fetchTransactions, addBook, updateBook, deleteBook, addMember, updateMember, deleteMember } from '../lib/api'
import { calculateFine, formatDate, isOverdue } from '../lib/utils'

export default function AdminDashboard({ user, onLogout }) {
  const [tab, setTab] = useState('overview')
  const [books, setBooks] = useState([])
  const [members, setMembers] = useState([])
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null) // { type: 'book'|'member', data: obj|null }
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const loadAll = useCallback(async () => {
    setLoading(true)
    try {
      const [b, m, t] = await Promise.all([fetchBooks(), fetchMembers(), fetchTransactions()])
      setBooks(b)
      setMembers(m)
      setTransactions(t)
    } catch (err) {
      showToast('Failed to load data: ' + err.message, 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadAll() }, [loadAll])

  // Stats
  const totalTitles = books.length
  const totalCopies = books.reduce((s, b) => s + b.total_copies, 0)
  const availCopies = books.reduce((s, b) => s + b.available_copies, 0)
  const totalMembers = members.length
  const activeBorrows = transactions.filter(t => t.status === 'ISSUED').length
  const overdueCount = transactions.filter(t => t.status === 'ISSUED' && isOverdue(t.due_date, t.return_date)).length
  const totalFines = transactions.reduce((s, t) => s + calculateFine(t.due_date, t.return_date), 0)

  async function handleSaveBook(bookData) {
    try {
      if (modal.data) {
        await updateBook(modal.data.book_id, bookData)
        showToast('Book updated successfully')
      } else {
        const maxId = books.reduce((m, b) => Math.max(m, b.book_id), 0)
        await addBook({ ...bookData, book_id: maxId + 1 })
        showToast('Book added successfully')
      }
      setModal(null)
      await loadAll()
    } catch (err) {
      showToast('Error: ' + err.message, 'error')
    }
  }

  async function handleSaveMember(memberData) {
    try {
      if (modal.data) {
        const updates = { ...memberData }
        if (!updates.password) delete updates.password
        await updateMember(modal.data.member_id, updates)
        showToast('Member updated successfully')
      } else {
        const maxId = members.reduce((m, b) => Math.max(m, b.member_id), 0)
        await addMember({ ...memberData, member_id: maxId + 1, password: memberData.password || 'password123' })
        showToast('Member registered successfully')
      }
      setModal(null)
      await loadAll()
    } catch (err) {
      showToast('Error: ' + err.message, 'error')
    }
  }

  async function handleDeleteBook(bookId) {
    if (!confirm('Delete this book permanently?')) return
    try {
      await deleteBook(bookId)
      showToast('Book deleted')
      await loadAll()
    } catch (err) {
      showToast('Error: ' + err.message, 'error')
    }
  }

  async function handleDeleteMember(memberId) {
    if (!confirm('Delete this member permanently? This will also remove their transactions.')) return
    try {
      await deleteMember(memberId)
      showToast('Member deleted')
      await loadAll()
    } catch (err) {
      showToast('Error: ' + err.message, 'error')
    }
  }

  const tabs = [
    { id: 'overview', label: 'Overview', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { id: 'books', label: 'Books', icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253' },
    { id: 'members', label: 'Members', icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z' },
    { id: 'transactions', label: 'Transactions', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4' },
  ]

  return (
    <div className="min-h-screen">
      {/* Top bar */}
      <header className="sticky top-0 z-40 glass border-b border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <Logo />
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-3 px-4 py-2 rounded-xl bg-indigo-500/10 border border-indigo-500/20">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-400 to-indigo-600 flex items-center justify-center text-white text-sm font-bold">A</div>
              <div>
                <p className="text-sm font-semibold text-white">Administrator</p>
                <p className="text-xs text-slate-500">Admin Console</p>
              </div>
            </div>
            <button onClick={onLogout} className="btn-ghost flex items-center gap-2">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span className="hidden sm:inline">Sign Out</span>
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
        {/* Tabs */}
        <div className="flex gap-1 mb-8 overflow-x-auto pb-1">
          {tabs.map(t => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 whitespace-nowrap ${
                tab === t.id
                  ? 'bg-indigo-500/15 text-indigo-300 border border-indigo-500/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
              }`}
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d={t.icon} />
              </svg>
              {t.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <svg className="animate-spin w-8 h-8 text-indigo-400" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
          </div>
        ) : (
          <>
            {tab === 'overview' && (
              <div className="space-y-6 animate-fade-in">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">Library Overview</h2>
                  <p className="text-sm text-slate-500">Real-time statistics across your library system</p>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  <StatCard label="Book Titles" value={totalTitles} delay={0} icon={<Icon name="book" />} color="indigo" />
                  <StatCard label="Total Copies" value={totalCopies} delay={50} icon={<Icon name="layers" />} color="violet" />
                  <StatCard label="Available Copies" value={availCopies} delay={100} icon={<Icon name="check" />} color="emerald" />
                  <StatCard label="Registered Members" value={totalMembers} delay={150} icon={<Icon name="users" />} color="sky" />
                  <StatCard label="Active Borrows" value={activeBorrows} delay={200} icon={<Icon name="clock" />} color="amber" />
                  <StatCard label="Overdue Items" value={overdueCount} delay={250} icon={<Icon name="alert" />} color="orange" />
                  <StatCard label="Total Overdue Fines" value={`₹${totalFines.toFixed(2)}`} delay={300} icon={<Icon name="currency" />} color="rose" />
                </div>

                {/* Recent transactions */}
                <div className="mt-8">
                  <h3 className="text-lg font-bold text-white mb-4">Recent Transactions</h3>
                  <TransactionTable transactions={transactions.slice(0, 5)} books={books} members={members} />
                </div>
              </div>
            )}

            {tab === 'books' && (
              <div className="space-y-6 animate-fade-in">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-white mb-1">Book Catalog</h2>
                    <p className="text-sm text-slate-500">{books.length} titles in the library</p>
                  </div>
                  <button onClick={() => setModal({ type: 'book', data: null })} className="btn-primary flex items-center gap-2">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                    </svg>
                    Add Book
                  </button>
                </div>
                <BooksTable books={books} onEdit={(b) => setModal({ type: 'book', data: b })} onDelete={handleDeleteBook} />
              </div>
            )}

            {tab === 'members' && (
              <div className="space-y-6 animate-fade-in">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-white mb-1">Member Management</h2>
                    <p className="text-sm text-slate-500">{members.length} registered members</p>
                  </div>
                  <button onClick={() => setModal({ type: 'member', data: null })} className="btn-primary flex items-center gap-2">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                    </svg>
                    Register Member
                  </button>
                </div>
                <MembersTable members={members} onEdit={(m) => setModal({ type: 'member', data: m })} onDelete={handleDeleteMember} />
              </div>
            )}

            {tab === 'transactions' && (
              <div className="space-y-6 animate-fade-in">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">Transaction Audit Log</h2>
                  <p className="text-sm text-slate-500">{transactions.length} total transaction records</p>
                </div>
                <TransactionTable transactions={transactions} books={books} members={members} />
              </div>
            )}
          </>
        )}
      </div>

      {modal?.type === 'book' && <BookModal book={modal.data} onSave={handleSaveBook} onClose={() => setModal(null)} />}
      {modal?.type === 'member' && <MemberModal member={modal.data} onSave={handleSaveMember} onClose={() => setModal(null)} />}

      {toast && (
        <div className="fixed bottom-6 right-6 z-50 animate-slide-up">
          <div className={`glass-card px-5 py-3 glow-border flex items-center gap-3 ${toast.type === 'error' ? 'border-rose-500/30' : 'border-emerald-500/30'}`}>
            <div className={`w-2 h-2 rounded-full ${toast.type === 'error' ? 'bg-rose-400' : 'bg-emerald-400'}`} />
            <span className="text-sm font-medium text-white">{toast.msg}</span>
          </div>
        </div>
      )}
    </div>
  )
}

function BooksTable({ books, onEdit, onDelete }) {
  return (
    <div className="glass-card overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">ID</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Title</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Author</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Genre</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">ISBN</th>
              <th className="text-center text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Total</th>
              <th className="text-center text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Available</th>
              <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Actions</th>
            </tr>
          </thead>
          <tbody>
            {books.map((b, i) => (
              <tr key={b.book_id} className={`border-b border-white/5 hover:bg-white/[0.02] transition-colors ${i % 2 === 0 ? '' : 'bg-white/[0.01]'}`}>
                <td className="px-5 py-4 text-sm text-slate-500 font-mono">#{b.book_id}</td>
                <td className="px-5 py-4 text-sm font-medium text-white">{b.title}</td>
                <td className="px-5 py-4 text-sm text-slate-400">{b.author}</td>
                <td className="px-5 py-4 text-sm text-slate-400">{b.genre || '—'}</td>
                <td className="px-5 py-4 text-sm text-slate-500 font-mono">{b.isbn || '—'}</td>
                <td className="px-5 py-4 text-sm text-slate-300 text-center">{b.total_copies}</td>
                <td className="px-5 py-4 text-center">
                  <span className={`badge ${b.available_copies > 0 ? 'badge-green' : 'badge-red'}`}>
                    {b.available_copies}
                  </span>
                </td>
                <td className="px-5 py-4">
                  <div className="flex items-center justify-end gap-2">
                    <button onClick={() => onEdit(b)} className="p-2 rounded-lg text-slate-400 hover:text-indigo-300 hover:bg-indigo-500/10 transition-all" title="Edit">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    </button>
                    <button onClick={() => onDelete(b.book_id)} className="p-2 rounded-lg text-slate-400 hover:text-rose-300 hover:bg-rose-500/10 transition-all" title="Delete">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function MembersTable({ members, onEdit, onDelete }) {
  return (
    <div className="glass-card overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">ID</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Name</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Email</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Phone</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Type</th>
              <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Actions</th>
            </tr>
          </thead>
          <tbody>
            {members.map((m, i) => (
              <tr key={m.member_id} className={`border-b border-white/5 hover:bg-white/[0.02] transition-colors ${i % 2 === 0 ? '' : 'bg-white/[0.01]'}`}>
                <td className="px-5 py-4 text-sm text-slate-500 font-mono">#{m.member_id}</td>
                <td className="px-5 py-4 text-sm font-medium text-white">{m.first_name} {m.last_name}</td>
                <td className="px-5 py-4 text-sm text-slate-400">{m.email}</td>
                <td className="px-5 py-4 text-sm text-slate-400">{m.phone || '—'}</td>
                <td className="px-5 py-4">
                  <span className={`badge ${m.membership_type === 'FACULTY' ? 'badge-indigo' : m.membership_type === 'STUDENT' ? 'badge-green' : 'badge-orange'}`}>
                    {m.membership_type}
                  </span>
                </td>
                <td className="px-5 py-4">
                  <div className="flex items-center justify-end gap-2">
                    <button onClick={() => onEdit(m)} className="p-2 rounded-lg text-slate-400 hover:text-indigo-300 hover:bg-indigo-500/10 transition-all" title="Edit">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    </button>
                    <button onClick={() => onDelete(m.member_id)} className="p-2 rounded-lg text-slate-400 hover:text-rose-300 hover:bg-rose-500/10 transition-all" title="Delete">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function TransactionTable({ transactions, books, members }) {
  const bookMap = Object.fromEntries(books.map(b => [b.book_id, b]))
  const memberMap = Object.fromEntries(members.map(m => [m.member_id, m]))

  return (
    <div className="glass-card overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">TxID</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Member</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Book</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Issued</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Due</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Returned</th>
              <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Status</th>
              <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Fine</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t, i) => {
              const book = bookMap[t.book_id]
              const member = memberMap[t.member_id]
              const fine = calculateFine(t.due_date, t.return_date)
              const overdue = isOverdue(t.due_date, t.return_date)
              return (
                <tr key={t.transaction_id} className={`border-b border-white/5 hover:bg-white/[0.02] transition-colors ${i % 2 === 0 ? '' : 'bg-white/[0.01]'}`}>
                  <td className="px-5 py-4 text-sm text-slate-500 font-mono">#{t.transaction_id}</td>
                  <td className="px-5 py-4 text-sm font-medium text-white">{member ? `${member.first_name} ${member.last_name}` : `Member #${t.member_id}`}</td>
                  <td className="px-5 py-4 text-sm text-slate-400">{book ? book.title : `Book #${t.book_id}`}</td>
                  <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.issue_date)}</td>
                  <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.due_date)}</td>
                  <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.return_date)}</td>
                  <td className="px-5 py-4">
                    {t.status === 'RETURNED' ? (
                      <span className="badge-green">RETURNED</span>
                    ) : overdue ? (
                      <span className="badge-red">OVERDUE</span>
                    ) : (
                      <span className="badge-orange">ISSUED</span>
                    )}
                  </td>
                  <td className="px-5 py-4 text-right">
                    {fine > 0 ? (
                      <span className="text-sm font-semibold text-rose-400">₹{fine.toFixed(2)}</span>
                    ) : (
                      <span className="text-sm text-slate-600">₹0.00</span>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function Icon({ name }) {
  const icons = {
    book: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
    layers: 'M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10',
    check: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
    users: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z',
    clock: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
    alert: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
    currency: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
  }
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d={icons[name]} />
    </svg>
  )
}
