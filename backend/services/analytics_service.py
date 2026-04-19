from repositories.job_repo import JobRepository
from typing import List, Dict, Any, cast
class AnalyticsService:

    def __init__(self):
        self.repo = JobRepository()

    def get_total_jobs_count(self):
        response = self.repo.get_columns(["id"])
        raw_data = cast(List[Dict[str, Any]], response.data or [])
        return len(raw_data)

    def get_total_platforms(self):
        response = self.repo.get_columns(["source_name"])
        raw_data = cast(List[Dict[str,Any]], response.data or [])

        unique_platforms = {job.get('source_name') for job in raw_data if job.get('source_name')}
        return len(unique_platforms)

    def get_market_share_stats(self):
        # transforms raw job data into source distribution for a donut chart
        response = self.repo.get_columns(["source_name"])
        # Ensure raw_jobs is a list, even if empty
        raw_jobs = cast(List[Dict[str, Any]], response.data or [])

        source_counts: Dict[str, int] = {}
        for job in raw_jobs:
            source = job.get('source_name','Unknown')
            source_counts[source] = source_counts.get(source, 0)+1
        
        # format and sort by count(descending)
        formatted_data = [
            {"name":source, "value":count}
            for source, count in source_counts.items()
        ]

        return sorted(formatted_data, key=lambda x: x['value'],reverse=True)

    # to get the seniority distribution
    def get_seniority_distribution(self):
        # 1. Fetch data
        response = self.repo.get_columns(["source_name", "job_level"])
        
        # 2. Cast to the correct type to satisfy Pylance
        raw_jobs = cast(List[Dict[str, Any]], response.data or [])

        # 3. Type hint the distribution dictionary
        # It maps a string (source) to another dictionary (levels + counts)
        distributions: Dict[str, Dict[str, Any]] = {}

        for job in raw_jobs:
            # Now Pylance knows .get() is safe
            source = job.get('source_name', 'Unknown')
            level = job.get('job_level') or "not specified"

            if source not in distributions:
                distributions[source] = {"source": source}
            
            # Increment the level count
            distributions[source][level] = distributions[source].get(level, 0) + 1

        return list(distributions.values())
    
    def get_daily_trends(self):
        response = self.repo.get_columns(["date_posted"])
        raw_data = cast(List[Dict[str,Any]], response.data or [])

        daily_counts: Dict[str,int]  ={}

        for job in raw_data:
            date=str(job.get('date_posted',''))[:10]
            if date:
                daily_counts[date]=daily_counts.get(date,0)+1
        
        formatted_trends = [
            {"date":date, "jobs":count}
            for date, count in daily_counts.items()
        ]

        return sorted(formatted_trends, key=lambda x: x['date'])
    

    def get_top_companies(self, limit=10):
        response = self.repo.get_columns(["company"])
        raw_data = cast(List[Dict[str,Any]], response.data or [])

        company_counts: Dict[str, int]={}

        for job in raw_data:
            name = job.get('company','Unknown')
            company_counts[name]=company_counts.get(name, 0)+1
        
        formatted_companies = [
            {"name":name, "count":count}
            for name, count in company_counts.items()
        ]

        top_list = sorted(formatted_companies, key=lambda x: x['count'], reverse=True)
        return top_list[:limit]

