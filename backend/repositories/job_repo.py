from supabase import create_client, Client
import os

class JobRepository:
    def __init__(self):
        url = os.environ.get("SUPBASE_URL")
        key = os.environ.get("SUPABASE_KEY")
        self.supabase: Client = create_client(url,key)

    def get_all_jobs(self):
        return self.supabase.table("jobs").select("*").execute()
    
    def get_count_by_source(self):
        response = self.supabase.table("jobs")\
        .select("source_name", count="exect")\
        .execute()

        return response
    