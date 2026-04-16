def get_market_share_stats(self):
    # transforms raw job data into source distribution for a donut chart
    response = self.repo.get_columns(["source_name"])
    raw_jobs = response.data

    # GROUP BY logic in python
    source_counts = {}
    for job in raw_jobs:
        source = job.get('source_name','Unknown')
        source_counts[source] = source_counts.get(source, 0)+1
    
    # format and sort by count(descending)
    formatted_data = [
        {"name":source, "value":count}
        for source, count in source_counts.items()
    ]

    return sorted(formatted_data, key=lambda x: x['value'],reverse=True)

def get_seniority_distribution(self):
    response = self.repo.get_columns(["source_name","job_level"])
    raw_jobs = response.data

    distributions = {}

    for job in raw_jobs:
        source = job.get('source_name','Unknown')
        level = job.get('job_level') or "not specified"

        if source not in distributions:
            distributions[source] = {"source":source}
        
        distributions[source][level] = distributions[source].get(level,0)+1

    return list(distributions.values())