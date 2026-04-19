'use client'

import { useEffect, useState } from "react";
import { 
  PieChart, Pie, Cell, ResponsiveContainer, Tooltip, 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend, 
  AreaChart, Area 
} from "recharts";
import { 
  read_marker_share, 
  read_seniority, 
  get_daily_trends, 
  get_top_companies 
} from "../api/api";

// 1. Interfaces
interface MarketShareData { name: string; value: number; }
interface SeniorityData { source: string; [key: string]: any; }
interface TrendData { date: string; jobs: number; }
interface CompanyData { name: string; count: number; }

const COLORS = ["#4f46e5", "#0ea5e9", "#10b981", "#f59e0b", "#6366f1"];

export default function JobCharts() {

  const [marketData, setMarketData] = useState<MarketShareData[]>([]);
  const [seniorityData, setSeniorityData] = useState<SeniorityData[]>([]);
  const [trendData, setTrendData] = useState<TrendData[]>([]);
  const [companyData, setCompanyData] = useState<CompanyData[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      const [ market, seniority, trends, companies] = await Promise.all([
        read_marker_share(),
        read_seniority(),
        get_daily_trends(),
        get_top_companies()
      ]);
      if (Array.isArray(market)) setMarketData(market);
      if (Array.isArray(seniority)) setSeniorityData(seniority);
      if (Array.isArray(trends)) setTrendData(trends);
      if (Array.isArray(companies)) setCompanyData(companies);
    };
    fetchData();
  }, []);

  return (
    <div className="flex flex-col gap-8 w-full p-6 bg-zinc-50 dark:bg-black">
      
      {/* Top Row: Market Share & Seniority */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Donut Chart */}
        <div className="bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
          <h2 className="text-lg font-bold mb-4">Market Share</h2>
          <div className="h-[300px] w-full min-h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={marketData}
                  innerRadius={70}
                  outerRadius={100}
                  paddingAngle={3}
                  dataKey="value"
                  stroke="none"
                >
                  {marketData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ borderRadius: '12px', border: 'none' }} />
                <Legend iconType="circle" />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Seniority Bar Chart */}
        <div className="bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
          <h2 className="text-lg font-bold mb-4">Seniority by Source</h2>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={seniorityData} margin={{ left: -20 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#333" />
                <XAxis dataKey="source" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
                <Tooltip cursor={{fill: 'transparent'}} />
                <Bar dataKey="Senior" fill="#4f46e5" stackId="a" barSize={30} />
                <Bar dataKey="Junior" fill="#10b981" stackId="a" barSize={30} />
                <Bar dataKey="Intern" fill="#f59e0b" stackId="a" barSize={30} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Middle Row: Top Hiring Companies (Horizontal Bar) */}
      <div className="bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
        <h2 className="text-lg font-bold mb-4">Top Hiring Companies</h2>
        <div className="h-[400px]">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={companyData} layout="vertical" margin={{ left: 40, right: 40 }}>
              <XAxis type="number" hide />
              <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} width={120} />
              <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} />
              <Bar dataKey="count" fill="#0ea5e9" radius={[0, 4, 4, 0]} barSize={20} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Bottom Row: Daily Scrape Trends (Area Chart) */}
      <div className="bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
        <h2 className="text-lg font-bold mb-4">Daily Job Volume Trends</h2>
        <div className="h-[300px]">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trendData}>
              <defs>
                <linearGradient id="colorJobs" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#333" />
              <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
              <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
              <Tooltip />
              <Area type="monotone" dataKey="jobs" stroke="#4f46e5" fillOpacity={1} fill="url(#colorJobs)" strokeWidth={3} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

    </div>
  );
}