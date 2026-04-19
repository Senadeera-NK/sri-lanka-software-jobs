'use client'

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