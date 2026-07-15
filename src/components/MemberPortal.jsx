import { useState, useEffect, useCallback } from 'react'
import Logo from './Logo'
import { fetchBooks, fetchMemberTransactions, issueBookTx, returnBookTx } from '../lib/api'
import { calculateFine, formatDate, isOverdue, getBorrowLimit } from '../lib/utils'

export default function MemberPortal({ user, onLogout }) {
  const [tab, setTab] = useState('catalog')
  const [books, setBooks] = useState([])
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState(null)
  const [search, setSearch] = useState('')

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const [b, t] = await Promise.all([fetchBooks(), fetchMemberTransactions(user.memberId)])
      setBooks(b)
      setTransactions(t)
    } catch (err) {
      showToast('Failed to load data: ' + err.message, 'error')
    } finally {
      setLoading(false)
    }
  }, [user.memberId])

  useEffect(() => { loadData() }, [loadData])

  const activeBorrows = transactions.filter(t => t.status === 'ISSUED')
  const borrowLimit = getBorrowLimit(user.membershipType)
  const bookMap = Object.fromEntries(books.map(b => [b.book_id, b]))
  const totalFines = transactions.reduce((s, t) => s + calculateFine(t.due_date, t.return_date), 0)

  async function handleBorrow(bookId) {
    if (activeBorrows.length >= borrowLimit) {
      showToast(`Borrowing limit reached! Your ${user.membershipType} membership allows ${borrowLimit} books.`, 'error')
      return
    }
    try {
      await issueBookTx(user.memberId, bookId)
      showToast('Book issued successfully! Return within 14 days.')
      await loadData()
    } catch (err) {
      showToast('Failed to borrow: ' + err.message, 'error')
    }
  }

  async function handleReturn(transactionId) {
    try {
      await returnBookTx(transactionId)
      showToast('Book returned successfully!')
      await loadData()
    } catch (err) {
      showToast('Failed to return: ' + err.message, 'error')
    }
  }

  const filteredBooks = books.filter(b =>
    !search ||
    b.title.toLowerCase().includes(search.toLowerCase()) ||
    b.author.toLowerCase().includes(search.toLowerCase()) ||
    (b.genre || '').toLowerCase().includes(search.toLowerCase())
  )

  const tabs = [
    { id: 'catalog', label: 'Browse Catalog', icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253' },
    { id: 'borrows', label: 'Current Borrows', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
    { id: 'history', label: 'History', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' },
    { id: 'profile', label: 'My Profile', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
  ]

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-40 glass border-b border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <Logo />
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-3 px-4 py-2 rounded-xl bg-emerald-500/10 border border-emerald-500/20">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center text-white text-sm font-bold">
                {user.firstName[0]}
              </div>
              <div>
                <p className="text-sm font-semibold text-white">{user.name}</p>
                <p className="text-xs text-slate-500">{user.membershipType} Member</p>
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
        {/* Quick stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <QuickStat label="Books Borrowed" value={activeBorrows.length} sub={`of ${borrowLimit} limit`} color="indigo" />
          <QuickStat label="Available Titles" value={books.filter(b => b.available_copies > 0).length} sub={`${books.length} total`} color="emerald" />
          <QuickStat label="Overdue Items" value={activeBorrows.filter(t => isOverdue(t.due_date, t.return_date)).length} sub="needs return" color="orange" />
          <QuickStat label="Total Fines" value={`₹${totalFines.toFixed(2)}`} sub="accumulated" color="rose" />
        </div>

        {/* Tabs */}
        <div className="flex gap-1 mb-6 overflow-x-auto pb-1">
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
            {tab === 'catalog' && (
              <div className="space-y-6 animate-fade-in">
                <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-white mb-1">Library Catalog</h2>
                    <p className="text-sm text-slate-500">Browse and borrow available books</p>
                  </div>
                  <div className="relative w-full sm:w-72">
                    <svg className="w-5 h-5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input
                      type="text"
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      className="input-field pl-10"
                      placeholder="Search by title, author, or genre..."
                    />
                  </div>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {filteredBooks.map((b, i) => (
                    <BookCard key={b.book_id} book={b} delay={i * 30} onBorrow={handleBorrow} disabled={activeBorrows.length >= borrowLimit} />
                  ))}
                </div>
                {filteredBooks.length === 0 && (
                  <div className="text-center py-12 text-slate-500">No books found matching your search.</div>
                )}
              </div>
            )}

            {tab === 'borrows' && (
              <div className="space-y-6 animate-fade-in">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">Current Borrows</h2>
                  <p className="text-sm text-slate-500">{activeBorrows.length} active {activeBorrows.length === 1 ? 'borrow' : 'borrows'} • Fines calculated dynamically (₹5/day overdue)</p>
                </div>
                {activeBorrows.length === 0 ? (
                  <div className="glass-card p-12 text-center">
                    <div className="w-16 h-16 rounded-2xl bg-indigo-500/10 flex items-center justify-center mx-auto mb-4">
                      <svg className="w-8 h-8 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                      </svg>
                    </div>
                    <p className="text-slate-400 font-medium">No active borrows</p>
                    <p className="text-sm text-slate-600 mt-1">Browse the catalog to borrow your first book!</p>
                  </div>
                ) : (
                  <div className="glass-card overflow-hidden">
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="border-b border-white/5">
                            <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Book</th>
                            <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Issued</th>
                            <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Due Date</th>
                            <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Status</th>
                            <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Fine</th>
                            <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {activeBorrows.map((t, i) => {
                            const book = bookMap[t.book_id]
                            const fine = calculateFine(t.due_date, t.return_date)
                            const overdue = isOverdue(t.due_date, t.return_date)
                            return (
                              <tr key={t.transaction_id} className={`border-b border-white/5 hover:bg-white/[0.02] transition-colors ${i % 2 === 0 ? '' : 'bg-white/[0.01]'}`}>
                                <td className="px-5 py-4 text-sm font-medium text-white">{book ? book.title : `Book #${t.book_id}`}</td>
                                <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.issue_date)}</td>
                                <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.due_date)}</td>
                                <td className="px-5 py-4">
                                  {overdue ? <span className="badge-red">OVERDUE</span> : <span className="badge-orange">ISSUED</span>}
                                </td>
                                <td className="px-5 py-4 text-right">
                                  {fine > 0 ? <span className="text-sm font-semibold text-rose-400">₹{fine.toFixed(2)}</span> : <span className="text-sm text-slate-600">₹0.00</span>}
                                </td>
                                <td className="px-5 py-4 text-right">
                                  <button onClick={() => handleReturn(t.transaction_id)} className="btn-primary text-sm px-4 py-2">
                                    Return
                                  </button>
                                </td>
                              </tr>
                            )
                          })}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}

            {tab === 'history' && (
              <div className="space-y-6 animate-fade-in">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">Borrowing History</h2>
                  <p className="text-sm text-slate-500">{transactions.length} total transaction records</p>
                </div>
                <div className="glass-card overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b border-white/5">
                          <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-5 py-4">TxID</th>
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
                          const fine = calculateFine(t.due_date, t.return_date)
                          const overdue = isOverdue(t.due_date, t.return_date)
                          return (
                            <tr key={t.transaction_id} className={`border-b border-white/5 hover:bg-white/[0.02] transition-colors ${i % 2 === 0 ? '' : 'bg-white/[0.01]'}`}>
                              <td className="px-5 py-4 text-sm text-slate-500 font-mono">#{t.transaction_id}</td>
                              <td className="px-5 py-4 text-sm font-medium text-white">{book ? book.title : `Book #${t.book_id}`}</td>
                              <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.issue_date)}</td>
                              <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.due_date)}</td>
                              <td className="px-5 py-4 text-sm text-slate-400">{formatDate(t.return_date)}</td>
                              <td className="px-5 py-4">
                                {t.status === 'RETURNED' ? <span className="badge-green">RETURNED</span> : overdue ? <span className="badge-red">OVERDUE</span> : <span className="badge-orange">ISSUED</span>}
                              </td>
                              <td className="px-5 py-4 text-right">
                                {fine > 0 ? <span className="text-sm font-semibold text-rose-400">₹{fine.toFixed(2)}</span> : <span className="text-sm text-slate-600">₹0.00</span>}
                              </td>
                            </tr>
                          )
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {tab === 'profile' && (
              <div className="space-y-6 animate-fade-in max-w-2xl">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">My Profile</h2>
                  <p className="text-sm text-slate-500">View your membership details</p>
                </div>
                <div className="glass-card p-8 glow-border">
                  <div className="flex items-center gap-5 mb-8">
                    <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center text-white text-3xl font-bold shadow-lg shadow-emerald-500/20">
                      {user.firstName[0]}
                    </div>
                    <div>
                      <h3 className="text-2xl font-bold text-white">{user.name}</h3>
                      <p className="text-sm text-slate-400">{user.email}</p>
                      <span className="badge-green mt-2 inline-block">{user.membershipType} Member</span>
                    </div>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <ProfileField label="First Name" value={user.firstName} />
                    <ProfileField label="Last Name" value={user.lastName} />
                    <ProfileField label="Email" value={user.email} />
                    <ProfileField label="Phone" value={user.phone || '—'} />
                    <ProfileField label="Membership Type" value={user.membershipType} />
                    <ProfileField label="Borrow Limit" value={`${borrowLimit} books`} />
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>

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

function BookCard({ book, onBorrow, disabled, delay }) {
  const available = book.available_copies > 0
  return (
    <div
      className="glass-card p-5 glow-border glow-border-hover animate-slide-up flex flex-col"
      style={{ animationDelay: `${delay}ms` }}
    >
      <div className="flex items-start justify-between mb-3">
        <div className="w-12 h-16 rounded-lg bg-gradient-to-br from-indigo-500/20 to-indigo-700/20 border border-indigo-500/20 flex items-center justify-center">
          <svg className="w-6 h-6 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
        </div>
        <span className={`badge ${available ? 'badge-green' : 'badge-red'}`}>
          {available ? `${book.available_copies} Available` : 'Out of Stock'}
        </span>
      </div>
      <h3 className="font-bold text-white text-base mb-1 line-clamp-2">{book.title}</h3>
      <p className="text-sm text-slate-400 mb-1">{book.author}</p>
      <p className="text-xs text-slate-500 mb-4">{book.genre || '—'}</p>
      <div className="mt-auto">
        <button
          onClick={() => onBorrow(book.book_id)}
          disabled={!available || disabled}
          className="btn-primary w-full text-sm disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {available ? 'Borrow Book' : 'Unavailable'}
        </button>
      </div>
    </div>
  )
}

function QuickStat({ label, value, sub, color }) {
  const colorMap = {
    indigo: 'text-indigo-300',
    emerald: 'text-emerald-300',
    orange: 'text-orange-300',
    rose: 'text-rose-300',
  }
  return (
    <div className="glass-card p-4 glow-border">
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wider mb-1">{label}</p>
      <p className={`text-2xl font-bold ${colorMap[color]}`}>{value}</p>
      <p className="text-xs text-slate-600 mt-0.5">{sub}</p>
    </div>
  )
}

function ProfileField({ label, value }) {
  return (
    <div className="bg-ink-700/30 rounded-xl px-4 py-3 border border-white/5">
      <p className="text-xs text-slate-500 uppercase tracking-wider mb-1">{label}</p>
      <p className="text-sm font-medium text-white">{value}</p>
    </div>
  )
}
