'use client'

export async function get_total_jobs_count(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/total_jobs`);
            const data = await res.json();
            console.log("totak jobs data: ", data)
            return data;
    }catch(err){
        console.log("total jobs error: ", err);
        return err;
    }
}

export async function get_total_platforms(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/total_platforms`);
            const data = await res.json();
            console.log("total platforms data: ", data)
            return data;
    }catch(err){
        console.log("total platforms error: ", err);
        return err;
    }
}

export async function read_seniority(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/seniority`);
            const data = await res.json();
            console.log("seniority data: ", data)
            return data;
    }catch(err){
        console.log("read_seniority error: ", err);
        return err;
    }
}

export async function read_marker_share(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/market-share`);
            const data = await res.json();
            console.log("market share data: ", data)
            return data;
    }catch(err){
        console.log("market share data error: ", err);
        return err;
    }
}

export async function get_daily_trends(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/daily-trends`);
            const data = await res.json();
            console.log("daily trends data: ", data)
            return data;
    }catch(err){
        console.log("daily trends data error: ", err);
        return err;
    }
}

export async function get_top_companies(){
    try{
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/analytics/top_companies`);
            const data = await res.json();
            console.log("top companies data: ", data)
            return data;
    }catch(err){
        console.log("top companies data error: ", err);
        return err;
    }
}