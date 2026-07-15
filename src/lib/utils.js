export function calculateFine(dueDate, returnDate) {
  if (!dueDate) return 0
  const due = new Date(dueDate)
  const end = returnDate ? new Date(returnDate) : new Date()
  const diffMs = end - due
  if (diffMs <= 0) return 0
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  return days * 5.0
}

export function isOverdue(dueDate, returnDate) {
  if (!dueDate || returnDate) return false
  return new Date(dueDate) < new Date()
}

export function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

export function getBorrowLimit(membershipType) {
  const type = (membershipType || 'REGULAR').toUpperCase()
  if (type === 'FACULTY') return 10
  if (type === 'STUDENT') return 5
  return 3
}
