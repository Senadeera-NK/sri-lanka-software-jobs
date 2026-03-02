package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataCleaner {

    public List<Job> clean(List<Job> allJobs) {
        if (allJobs == null || allJobs.isEmpty()) return new ArrayList<>();

        int maxDays = Integer.parseInt(Config.get("max.days.old"));
        LocalDate cutoffDate = LocalDate.now().minusDays(maxDays);

        // Get keywords once before the stream starts for better performance
        List<String> keywords = Config.getList("tech.keywords");

        return allJobs.stream()
                // 1. Tech Filter: Remove non-tech roles immediately
                .filter(job -> isTechJob(job.title(), keywords))

                // 2. Date Filter: Remove jobs older than 14 days
                .filter(job -> !job.datePosted().isBefore(cutoffDate))

                // 3. Deduplicate: Group by ID (company-title-source-date)
                .collect(Collectors.toMap(
                        Job::id,
                        job -> job,
                        (existing, replacement) -> existing, // Keep the first one found
                        LinkedHashMap::new
                ))
                .values()
                .stream()

                // 4. Sort: Newest posts at the top
                .sorted(Comparator.comparing(Job::datePosted).reversed())
                .collect(Collectors.toList());
    }

    private boolean isTechJob(String title, List<String> keywords) {
        if (title == null) return false;
        String t = title.toLowerCase();
        return keywords.stream().anyMatch(t::contains);
    }
}