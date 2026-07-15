import { useState } from 'react'
import { login } from '../lib/auth'
import Logo from './Logo'

export default function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(email.trim(), password)
      if (!user) {
        setError('Invalid credentials. Please check your email/username and password.')
      } else {
        onLogin(user)
      }
    } catch (err) {
      setError('Login failed: ' + (err.message || 'Unknown error'))
    } finally {
      setLoading(false)
    }
  }

  function fillCredentials(type) {
    if (type === 'admin') {
      setEmail('admin')
      setPassword('123')
    } else {
      setEmail('shivnsh01@gmail.com')
      setPassword('password123')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background gradient orbs */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-[120px] -z-10" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-[120px] -z-10" />

      <div className="w-full max-w-md animate-slide-up">
        <div className="flex justify-center mb-8">
          <Logo size="lg" />
        </div>

        <div className="glass-card p-8 glow-border animate-glow-pulse">
          <h1 className="text-2xl font-bold text-white mb-1">Welcome Back</h1>
          <p className="text-slate-400 text-sm mb-6">Sign in to access your library portal</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1.5">
                Email or Username
              </label>
              <input
                type="text"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input-field"
                placeholder="admin or your@email.com"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1.5">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input-field"
                placeholder="••••••••"
                required
              />
            </div>

            {error && (
              <div className="bg-rose-500/10 border border-rose-500/20 rounded-xl px-4 py-3 text-sm text-rose-300 animate-fade-in">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <>
                  <svg className="animate-spin w-5 h-5" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-white/5">
            <p className="text-xs text-slate-500 mb-3 text-center font-medium">Quick Demo Access</p>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => fillCredentials('admin')}
                className="glass rounded-xl px-4 py-3 text-sm text-slate-300 hover:text-white hover:border-indigo-500/30 glow-border-hover"
              >
                <div className="font-semibold text-indigo-300">Admin</div>
                <div className="text-xs text-slate-500">admin / 123</div>
              </button>
              <button
                onClick={() => fillCredentials('member')}
                className="glass rounded-xl px-4 py-3 text-sm text-slate-300 hover:text-white hover:border-indigo-500/30 glow-border-hover"
              >
                <div className="font-semibold text-emerald-300">Member</div>
                <div className="text-xs text-slate-500">Student account</div>
              </button>
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-slate-600 mt-6">
          Book Haven Smart Library • MVC Architecture • MySQL • React
        </p>
      </div>
    </div>
  )
}
