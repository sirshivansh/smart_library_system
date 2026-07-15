import { useState, useEffect } from 'react'

export default function BookModal({ book, onSave, onClose }) {
  const [form, setForm] = useState({
    title: '',
    author: '',
    isbn: '',
    genre: '',
    total_copies: 1,
    available_copies: 1,
  })
  const [errors, setErrors] = useState({})

  useEffect(() => {
    if (book) {
      setForm({
        title: book.title || '',
        author: book.author || '',
        isbn: book.isbn || '',
        genre: book.genre || '',
        total_copies: book.total_copies ?? 1,
        available_copies: book.available_copies ?? 1,
      })
    }
  }, [book])

  function validate() {
    const e = {}
    if (!form.title.trim()) e.title = 'Title is required'
    if (!form.author.trim()) e.author = 'Author is required'
    if (!form.total_copies || form.total_copies < 1) e.total_copies = 'Must be at least 1'
    if (form.available_copies < 0) e.available_copies = 'Cannot be negative'
    if (form.available_copies > form.total_copies) e.available_copies = 'Cannot exceed total copies'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    onSave({
      ...form,
      total_copies: parseInt(form.total_copies),
      available_copies: parseInt(form.available_copies),
    })
  }

  return (
    <ModalShell title={book ? 'Edit Book' : 'Add New Book'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <Field label="Book Title" error={errors.title}>
          <input className="input-field" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="The Great Gatsby" />
        </Field>
        <Field label="Author" error={errors.author}>
          <input className="input-field" value={form.author} onChange={(e) => setForm({ ...form, author: e.target.value })} placeholder="F. Scott Fitzgerald" />
        </Field>
        <div className="grid grid-cols-2 gap-4">
          <Field label="ISBN" error={errors.isbn}>
            <input className="input-field" value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} placeholder="978-0743273565" />
          </Field>
          <Field label="Genre">
            <input className="input-field" value={form.genre} onChange={(e) => setForm({ ...form, genre: e.target.value })} placeholder="Fiction" />
          </Field>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Total Copies" error={errors.total_copies}>
            <input type="number" min="1" className="input-field" value={form.total_copies} onChange={(e) => setForm({ ...form, total_copies: e.target.value })} />
          </Field>
          <Field label="Available Copies" error={errors.available_copies}>
            <input type="number" min="0" className="input-field" value={form.available_copies} onChange={(e) => setForm({ ...form, available_copies: e.target.value })} />
          </Field>
        </div>
        <FormActions onClose={onClose} isEdit={!!book} />
      </form>
    </ModalShell>
  )
}

export function MemberModal({ member, onSave, onClose }) {
  const [form, setForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    password: '',
    membership_type: 'STUDENT',
  })
  const [errors, setErrors] = useState({})

  useEffect(() => {
    if (member) {
      setForm({
        first_name: member.first_name || '',
        last_name: member.last_name || '',
        email: member.email || '',
        phone: member.phone || '',
        password: '',
        membership_type: member.membership_type || 'STUDENT',
      })
    }
  }, [member])

  function validate() {
    const e = {}
    if (!form.first_name.trim() || form.first_name.trim().length < 2) e.first_name = 'Min 2 letters'
    if (!form.last_name.trim() || form.last_name.trim().length < 2) e.last_name = 'Min 2 letters'
    if (!form.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Valid email required'
    if (!member && (!form.password || form.password.length < 6)) e.password = 'Min 6 characters'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    const payload = { ...form }
    if (member && !payload.password) delete payload.password
    onSave(payload)
  }

  return (
    <ModalShell title={member ? 'Edit Member' : 'Register New Member'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Field label="First Name" error={errors.first_name}>
            <input className="input-field" value={form.first_name} onChange={(e) => setForm({ ...form, first_name: e.target.value })} placeholder="Shivansh" />
          </Field>
          <Field label="Last Name" error={errors.last_name}>
            <input className="input-field" value={form.last_name} onChange={(e) => setForm({ ...form, last_name: e.target.value })} placeholder="Mishra" />
          </Field>
        </div>
        <Field label="Email" error={errors.email}>
          <input className="input-field" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="member@email.com" />
        </Field>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Phone">
            <input className="input-field" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="9876543210" />
          </Field>
          <Field label="Membership Type">
            <select className="input-field" value={form.membership_type} onChange={(e) => setForm({ ...form, membership_type: e.target.value })}>
              <option value="REGULAR">REGULAR (3 books)</option>
              <option value="STUDENT">STUDENT (5 books)</option>
              <option value="FACULTY">FACULTY (10 books)</option>
            </select>
          </Field>
        </div>
        <Field label={member ? 'New Password (leave blank to keep)' : 'Password'} error={errors.password}>
          <input type="password" className="input-field" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="••••••••" />
        </Field>
        <FormActions onClose={onClose} isEdit={!!member} />
      </form>
    </ModalShell>
  )
}

function ModalShell({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-fade-in">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative w-full max-w-lg glass-card p-6 glow-border animate-scale-in max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-white">{title}</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors">
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

function Field({ label, error, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
      {children}
      {error && <p className="text-xs text-rose-400 mt-1">{error}</p>}
    </div>
  )
}

function FormActions({ onClose, isEdit }) {
  return (
    <div className="flex gap-3 pt-2">
      <button type="button" onClick={onClose} className="btn-ghost flex-1">Cancel</button>
      <button type="submit" className="btn-primary flex-1">{isEdit ? 'Save Changes' : 'Create'}</button>
    </div>
  )
}
