from supabase import create_client, Client
import psycopg2
from dotenv import load_dotenv
import os

# 1. MOVE THIS TO THE TOP
load_dotenv()

class JobRepository:
    def __init__(self):
        # 2. Fix the spelling: SUPABASE_URL (added the 'A')
        url = os.environ.get("SUPABASE_URL") 
        key = os.environ.get("SUPABASE_KEY")

        print(f"DEBUG: Found URL: {url}") # If this prints 'None', the spelling or path is wrong
        print(f"DEBUG: Found Key: {'Yes' if key else 'No'}")
       
        if url is None or key is None:
            raise ValueError("Missing SUPABASE_URL or SUPABASE_KEY in environment variables")

        self.supabase: Client = create_client(url, key)

        # Optional: Raw Postgres connection
        DATABASE_URL = os.getenv("DATABASE_URL")
        if DATABASE_URL:
            self.connection = psycopg2.connect(DATABASE_URL)
            print("Database connected successfully via psycopg2")

    def get_all_jobs(self):
        return self.supabase.table("jobs").select("*").execute()
    
    def get_count_by_source(self):
        response = self.supabase.table("jobs")\
            .select("source_name")\
            .execute() # type: ignore
        return response
    
    def get_columns(self, columns: list):
        """Helper for your analytics service"""
        cols_query = ",".join(columns)
        return self.supabase.table("jobs").select(cols_query).execute()