package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataCleaner {

    public List<Job> clean(List<Job> allJobs) {
        if (allJobs == null || allJobs.isEmpty()) return new ArrayList<>();

        List<String> domains = Config.getTechKeywords();
        //List<String> roles = Config.getJobRoleKeywords();

        int maxDays = Integer.parseInt(Config.get("max.days.old"));
        LocalDate cutoffDate = LocalDate.now().minusDays(maxDays);

        return allJobs.stream()
                // 1. Tech Filter: Remove non-tech roles immediately
                .filter(job -> isTechJob(job.title(), domains))

                // 2. Date Filter: Remove jobs older than 14 days
                .filter(job -> !job.datePosted().isBefore(cutoffDate.atStartOfDay()))

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

    private boolean isTechJob(String title, List<String> techKeywords) {
        if (title == null) return false;
        String t = title.toLowerCase();

        // 1. Match against your 'tech.keywords' (software, dev, qa, react, engineer, etc.)
        boolean matchesTech = techKeywords.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(t::contains);

        // 2. Extra Safety: Hard-reject known non-tech words even if they hit a keyword
        // Example: "HR Manager for Software Company" would match 'software' but get rejected here.
        boolean isTrash = t.contains("human resource") || t.contains("marketing") || t.contains("recruiter");

        return matchesTech && !isTrash;
    }
}