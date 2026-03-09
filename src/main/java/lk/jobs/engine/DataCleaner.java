package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataCleaner {

    public List<Job> clean(List<Job> allJobs) {
        if (allJobs == null || allJobs.isEmpty()) return new ArrayList<>();

        List<String> techKeyWords = Arrays.asList(Config.get("tech.keywords").split(","));
        List<String> blockedKeyWords = Arrays.asList(Config.get("blocked.keywords").split(","));

        int maxDays = Integer.parseInt(Config.get("max.days.old"));
        LocalDate cutoffDate = LocalDate.now().minusDays(maxDays);

        return allJobs.stream()
                // 1. Tech Filter: Remove non-tech roles immediately
                .filter(job -> isTechJob(job.title(), techKeyWords,blockedKeyWords))

                // 2. Date Filter: Remove jobs older than 14 days
                .filter(job -> !job.datePosted().isBefore(cutoffDate.atStartOfDay()))

                // 3. Deduplicate: Group by ID (company-title-source-date)
                .collect(Collectors.toMap(
                        Job::id,
                        job -> job,
                        (existing, replacement) -> replacement, // Keep the first one found
                        LinkedHashMap::new
                ))
                .values()
                .stream()

                // 4. Sort: Newest posts at the top
                .sorted(Comparator.comparing(Job::datePosted).reversed())
                .collect(Collectors.toList());
    }

    private boolean isTechJob(String title, List<String> techKeywords, List<String> blockedKeyWords) {
        if (title == null) return false;
        String t = title.toLowerCase();

        // 1. Match against your 'tech.keywords' (software, dev, qa, react, engineer, etc.)
        boolean matchesTech = techKeywords.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(keyword -> {
                    // If the keyword is short (like 'qa'), check for whole word boundaries
                    if (keyword.length() <= 2) {
                        return t.matches(".*\\b" + keyword + "\\b.*");
                    }
                    return t.contains(keyword);
                });

        //MUST NOT match any blocked keywords
        boolean hasBlockedMatch = blockedKeyWords.stream()
                .map(String::trim).map(String::toLowerCase)
                .anyMatch(t::contains);

        return matchesTech && !hasBlockedMatch;
    }
}