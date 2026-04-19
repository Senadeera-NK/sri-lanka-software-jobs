'use client'

import { useEffect, useState } from "react";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend } from "recharts";
import { read_marker_share, read_seniority } from "../api/api";
// 1. Define interfaces for your data
interface MarketShareData {
  name: string;
  value: number;
}

interface SeniorityData {
  source: string;
  [key: string]: any; // This allows for "Senior", "Junior", etc. as dynamic keys
}
const COLORS = ["#4f46e5", "#0ea5e9", "#10b981", "#f59e0b", "#6366f1"];

export default function JobCharts() {
  const [marketData, setMarketData] = useState<MarketShareData[]>([]);
  const [seniorityData, setSeniorityData] = useState<SeniorityData[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      const market = await read_marker_share();
      const seniority = await read_seniority();
      if (Array.isArray(market)) setMarketData(market as MarketShareData[]);
      if (Array.isArray(seniority)) setSeniorityData(seniority as SeniorityData[]);
    };
    fetchData();
  }, []);

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full p-6">
      {/* Market Share Donut Chart */}
      <div className="bg-white dark:bg-zinc-900 p-6 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
        <h2 className="text-xl font-bold mb-4">Market Share</h2>
        <div className="h-[300px]">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={marketData}
                innerRadius={60}
                outerRadius={90}
                paddingAngle={0}
                dataKey="value"
                stroke="none"
              >
                {marketData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Seniority Distribution Stacked Bar Chart */}
      <div className="bg-white dark:bg-zinc-900 p-6 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm">
        <h2 className="text-xl font-bold mb-4">Seniority by Source</h2>
        <div className="h-[300px]">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={seniorityData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="source" />
              <YAxis />
              <Tooltip />
              <Legend />
              {/* Note: Keys here match the levels returned by your FastAPI service */}
              <Bar dataKey="Senior" fill="#0088FE" stackId="a" />
              <Bar dataKey="Junior" fill="#00C49F" stackId="a" />
              <Bar dataKey="Intern" fill="#FFBB28" stackId="a" />
              <Bar dataKey="not specified" fill="#94a3b8" stackId="a" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}