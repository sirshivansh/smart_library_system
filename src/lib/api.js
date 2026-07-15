import { supabase } from './supabase'

export async function fetchBooks() {
  const { data, error } = await supabase.from('books').select('*').order('book_id')
  if (error) throw error
  return data
}

export async function fetchMembers() {
  const { data, error } = await supabase.from('members').select('*').order('member_id')
  if (error) throw error
  return data
}

export async function fetchTransactions() {
  const { data, error } = await supabase.from('transactions').select('*').order('transaction_id', { ascending: false })
  if (error) throw error
  return data
}

export async function fetchMemberTransactions(memberId) {
  const { data, error } = await supabase
    .from('transactions')
    .select('*')
    .eq('member_id', memberId)
    .order('transaction_id', { ascending: false })
  if (error) throw error
  return data
}

export async function addBook(book) {
  const { data, error } = await supabase.from('books').insert(book).select().single()
  if (error) throw error
  return data
}

export async function updateBook(bookId, updates) {
  const { data, error } = await supabase.from('books').update(updates).eq('book_id', bookId).select().single()
  if (error) throw error
  return data
}

export async function deleteBook(bookId) {
  const { error } = await supabase.from('books').delete().eq('book_id', bookId)
  if (error) throw error
}

export async function addMember(member) {
  const { data, error } = await supabase.from('members').insert(member).select().single()
  if (error) throw error
  return data
}

export async function updateMember(memberId, updates) {
  const { data, error } = await supabase.from('members').update(updates).eq('member_id', memberId).select().single()
  if (error) throw error
  return data
}

export async function deleteMember(memberId) {
  const { error } = await supabase.from('members').delete().eq('member_id', memberId)
  if (error) throw error
}

export async function issueBookTx(memberId, bookId) {
  const today = new Date()
  const due = new Date(today)
  due.setDate(due.getDate() + 14)

  const { data: tx, error: txError } = await supabase.from('transactions').insert({
    member_id: memberId,
    book_id: bookId,
    issue_date: today.toISOString().slice(0, 10),
    due_date: due.toISOString().slice(0, 10),
    status: 'ISSUED',
  }).select().single()
  if (txError) throw txError

  const { data: book } = await supabase.from('books').select('available_copies').eq('book_id', bookId).single()
  if (book && book.available_copies > 0) {
    await supabase.from('books').update({ available_copies: book.available_copies - 1 }).eq('book_id', bookId)
  }

  return tx
}

export async function returnBookTx(transactionId) {
  const today = new Date().toISOString().slice(0, 10)

  const { data: tx, error: txError } = await supabase
    .from('transactions')
    .select('*')
    .eq('transaction_id', transactionId)
    .single()
  if (txError) throw txError

  const { error: updateError } = await supabase
    .from('transactions')
    .update({ return_date: today, status: 'RETURNED' })
    .eq('transaction_id', transactionId)
  if (updateError) throw updateError

  const { data: book } = await supabase.from('books').select('available_copies, total_copies').eq('book_id', tx.book_id).single()
  if (book && book.available_copies < book.total_copies) {
    await supabase.from('books').update({ available_copies: book.available_copies + 1 }).eq('book_id', tx.book_id)
  }

  return { ...tx, return_date: today, status: 'RETURNED' }
}
