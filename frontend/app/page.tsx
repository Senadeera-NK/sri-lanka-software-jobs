import JobCharts from "../app/components/JobCharts";

export default function Home() {
  return (
    <div className="min-h-screen bg-[#f8fafc] dark:bg-black font-sans text-slate-900 dark:text-slate-100">
      {/* Sleek Navbar */}
      <nav className="sticky top-0 z-50 border-b border-slate-200 dark:border-zinc-800 bg-white/80 dark:bg-black/80 backdrop-blur-md px-6 py-3">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">L</div>
            <h1 className="text-lg font-bold tracking-tight uppercase">LankaJob <span className="text-blue-600">Pulse</span></h1>
          </div>
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5 text-[10px] bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-400 px-2.5 py-1 rounded-full font-bold border border-emerald-100 dark:border-emerald-900">
              <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse"></span>
              LIVE DATA
            </span>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-10 px-6">
        {/* Header Section */}
        <div className="mb-10">
          <h2 className="text-3xl font-black tracking-tight">Market Intelligence</h2>
          <p className="text-slate-500 dark:text-zinc-500 mt-1 font-medium">
            Analyzing engineering vacancies across the island's top recruiters.
          </p>
        </div>

        {/* Quick Stats Ribbon */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
            <div className="bg-white dark:bg-zinc-900 p-5 rounded-2xl border border-slate-200 dark:border-zinc-800">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Total Indexed</p>
                <p className="text-2xl font-black mt-1">428 Jobs</p>
            </div>
            <div className="bg-white dark:bg-zinc-900 p-5 rounded-2xl border border-slate-200 dark:border-zinc-800">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Trending Hub</p>
                <p className="text-2xl font-black mt-1 text-blue-600">Colombo</p>
            </div>
            <div className="bg-white dark:bg-zinc-900 p-5 rounded-2xl border border-slate-200 dark:border-zinc-800">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Active Boards</p>
                <p className="text-2xl font-black mt-1">4 Platforms</p>
            </div>
        </div>

        <div className="bg-white dark:bg-zinc-950 rounded-3xl p-4 border border-slate-200 dark:border-zinc-900 shadow-xl shadow-slate-200/50 dark:shadow-none">
            <JobCharts />
        </div>
      </main>
    </div>
  );
}