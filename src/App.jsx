import { useState } from 'react'
import LoginPage from './components/LoginPage'
import AdminDashboard from './components/AdminDashboard'
import MemberPortal from './components/MemberPortal'

export default function App() {
  const [user, setUser] = useState(null)

  if (!user) {
    return <LoginPage onLogin={setUser} />
  }

  if (user.role === 'ADMIN') {
    return <AdminDashboard user={user} onLogout={() => setUser(null)} />
  }

  return <MemberPortal user={user} onLogout={() => setUser(null)} />
}
