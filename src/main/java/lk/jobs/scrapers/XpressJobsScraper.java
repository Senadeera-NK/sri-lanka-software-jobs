package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class XpressJobsScraper implements JobScraper {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public XpressJobsScraper(String apiUrl) {
        // We will use the API URL you found
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            // Fetch the JSON string directly from the API
            String jsonResponse = Jsoup.connect(apiUrl)
                    .ignoreContentType(true) // Crucial for non-HTML responses
                    .userAgent("Mozilla/5.0")
                    .execute()
                    .body();

            // Parse the JSON array
            JsonNode rootArray = mapper.readTree(jsonResponse);

            if (rootArray.isArray()) {
                System.out.println("DEBUG [XpressJobs]: Processing " + rootArray.size() + " jobs from API.");

                for (JsonNode node : rootArray) {
                    String title = node.get("jobTitle").asText();
                    String company = node.get("organizationName").asText();
                    int jobId = node.get("jobId").asInt();
                    if (!isRelevant(title)) {
                        // ADD THIS LINE TEMPORARILY:
                        System.out.println("SKIPPED (Keyword Mismatch): " + title);
                        continue;
                    }
                    // Filter based on your tech.keywords
                    if (isRelevant(title)) {

                        // Date Calculation: expireDayCountDown usually implies a 30-day post.
                        // If it expires in 16 days, it was posted ~14 days ago.
                        int daysLeft = node.get("expireDayCountDown").asInt();
                        int daysAgo = 30 - daysLeft;
                        LocalDateTime postedDate = LocalDateTime.now().minusDays(Math.max(0, daysAgo));

                        // Freshness check from config
                        LocalDate cutoff = LocalDate.now().minusDays(Config.getInt("max.days.old", 14));

                        if (!postedDate.toLocalDate().isBefore(cutoff)) {
                            jobs.add(new Job(
                                    title,
                                    company,
                                    determineLevel(title),
                                    getSourceName(),
                                    "https://xpress.jobs/jobs/view/" + jobId,
                                    postedDate,
                                    LocalDate.now()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("XpressJobs API Error: " + e.getMessage());
        }
        return jobs;
    }

    private boolean isRelevant(String title) {
        String lowerTitle = title.toLowerCase();

        // Check Blocked Keywords first (Highest Priority)
        boolean isBlocked = Config.getBlockedKeywords().stream()
                .anyMatch(lowerTitle::contains);
        if (isBlocked) return false;

        // Check Tech Keywords
        boolean hasTech = Config.getTechKeywords().stream()
                .anyMatch(lowerTitle::contains);

        // Check Job Level/Role Keywords (e.g., Trainee, Intern, Engineer)
        boolean hasRole = Config.getTechJobKeywords().stream()
                .anyMatch(lowerTitle::contains);

        // If it mentions a tech stack OR a relevant engineering role, we want it
        return hasTech || hasRole;
    }

    private String determineLevel(String title) {
        String t = title.toLowerCase();
        if (t.contains("intern")) return "Intern";
        if (t.contains("associate") || t.contains("trainee")) return "Associate";
        if (t.contains("senior") || t.contains("sr") || t.contains("lead")) return "Senior";
        return "Junior/SE";
    }

    @Override
    public String getSourceName() {
        return "XpressJobs";
    }
}