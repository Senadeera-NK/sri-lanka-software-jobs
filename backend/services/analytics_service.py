from repositories.job_repo import JobRepository
from typing import List, Dict, Any, cast
class AnalyticsService:

    def __init__(self):
        self.repo = JobRepository()

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

