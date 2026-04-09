package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RoosterJobsScraper implements JobScraper {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    // Rooster date format: 2026-03-23 12:20:35
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RoosterJobsScraper(String apiUrl) {
        // Use: https://api.rooster.jobs/jobSearch/jobs/search
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            // FIXED: query is now an array to match the API's new requirements
            String requestBody = "{\"query\":[\"software\"],\"limit\":100,\"page\":1,\"filters\":{}}";

            Connection.Response response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Origin", "https://rooster.jobs")
                    .header("Referer", "https://rooster.jobs/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                    .requestBody(requestBody)
                    .ignoreHttpErrors(true)
                    .execute();

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                System.err.println("Rooster API Failed. Status: " + response.statusCode());
                System.err.println("Error Body: " + response.body());
                return jobs;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode dataArray = root.path("body").path("data");

            if (dataArray.isArray()) {
                System.out.println("DEBUG [Rooster]: Successfully fetched " + dataArray.size() + " jobs.");

                for (JsonNode node : dataArray) {
                    String title = node.path("title").asText("");
                    String company = node.path("company_name").asText("Unknown");
                    int jobId = node.path("id").asInt();

                    // Skip if title is empty or not relevant to your tech stack
                    if (title.isEmpty() || !isRelevant(title)) continue;

                    String createdAtStr = node.path("created_at").asText();
                    LocalDateTime postedDate;
                    try {
                        postedDate = LocalDateTime.parse(createdAtStr, formatter);
                    } catch (Exception e) {
                        postedDate = LocalDateTime.now();
                    }

                    LocalDate cutoff = LocalDate.now().minusDays(Config.getInt("max.days.old", 14));

                    if (!postedDate.toLocalDate().isBefore(cutoff)) {
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title, node.path("job_type").asText("")),
                                getSourceName(),
                                "https://rooster.jobs/jobs/" + jobId,
                                postedDate,
                                LocalDate.now()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Rooster Scraper Exception: " + e.getMessage());
        }
        return jobs;
    }

    private boolean isRelevant(String title) {
        String lowerTitle = title.toLowerCase();

        boolean isBlocked = Config.getBlockedKeywords().stream()
                .anyMatch(lowerTitle::contains);
        if (isBlocked) return false;

        boolean hasTech = Config.getTechKeywords().stream()
                .anyMatch(lowerTitle::contains);
        boolean hasRole = Config.getTechJobKeywords().stream()
                .anyMatch(lowerTitle::contains);

        return hasTech || hasRole;
    }

    private String determineLevel(String title, String jobType) {
        String t = title.toLowerCase();
        if (jobType.equalsIgnoreCase("internship") || t.contains("intern")) return "Intern";
        if (t.contains("associate") || t.contains("trainee")) return "Associate";
        if (t.contains("senior") || t.contains("sr") || t.contains("lead")) return "Senior";
        return "Junior/SE";
    }

    @Override
    public String getSourceName() {
        return "Rooster.jobs";
    }
}