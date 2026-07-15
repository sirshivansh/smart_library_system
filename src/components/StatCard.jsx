export default function StatCard({ label, value, icon, color = 'indigo', delay = 0 }) {
  const colorMap = {
    indigo: { glow: 'shadow-indigo-500/10', text: 'text-indigo-300', bg: 'bg-indigo-500/10', border: 'border-indigo-500/20' },
    emerald: { glow: 'shadow-emerald-500/10', text: 'text-emerald-300', bg: 'bg-emerald-500/10', border: 'border-emerald-500/20' },
    rose: { glow: 'shadow-rose-500/10', text: 'text-rose-300', bg: 'bg-rose-500/10', border: 'border-rose-500/20' },
    amber: { glow: 'shadow-amber-500/10', text: 'text-amber-300', bg: 'bg-amber-500/10', border: 'border-amber-500/20' },
    sky: { glow: 'shadow-sky-500/10', text: 'text-sky-300', bg: 'bg-sky-500/10', border: 'border-sky-500/20' },
    violet: { glow: 'shadow-violet-500/10', text: 'text-violet-300', bg: 'bg-violet-500/10', border: 'border-violet-500/20' },
    orange: { glow: 'shadow-orange-500/10', text: 'text-orange-300', bg: 'bg-orange-500/10', border: 'border-orange-500/20' },
  }
  const c = colorMap[color] || colorMap.indigo

  return (
    <div
      className="glass-card p-5 glow-border glow-border-hover animate-slide-up"
      style={{ animationDelay: `${delay}ms` }}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider mb-2">{label}</p>
          <p className="text-2xl font-bold text-white">{value}</p>
        </div>
        <div className={`w-11 h-11 rounded-xl ${c.bg} ${c.border} border flex items-center justify-center`}>
          {icon}
        </div>
      </div>
    </div>
  )
}
