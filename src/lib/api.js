const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
  }
  return response.json();
}

export async function fetchBooks() {
  return request('/books');
}

export async function fetchMembers() {
  return request('/members');
}

export async function fetchTransactions() {
  return request('/transactions');
}

export async function fetchMemberTransactions(memberId) {
  return request(`/transactions?memberId=${memberId}`);
}

export async function addBook(book) {
  return request('/books', {
    method: 'POST',
    body: JSON.stringify(book),
  });
}

export async function updateBook(bookId, updates) {
  return request(`/books?id=${bookId}`, {
    method: 'PUT',
    body: JSON.stringify(updates),
  });
}

export async function deleteBook(bookId) {
  return request(`/books?id=${bookId}`, {
    method: 'DELETE',
  });
}

export async function addMember(member) {
  return request('/members', {
    method: 'POST',
    body: JSON.stringify(member),
  });
}

export async function updateMember(memberId, updates) {
  return request(`/members?id=${memberId}`, {
    method: 'PUT',
    body: JSON.stringify(updates),
  });
}

export async function deleteMember(memberId) {
  return request(`/members?id=${memberId}`, {
    method: 'DELETE',
  });
}

export async function issueBookTx(memberId, bookId) {
  return request('/transactions/issue', {
    method: 'POST',
    body: JSON.stringify({
      member_id: memberId,
      book_id: bookId,
    }),
  });
}

export async function returnBookTx(transactionId) {
  return request(`/transactions/return?id=${transactionId}`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
}
