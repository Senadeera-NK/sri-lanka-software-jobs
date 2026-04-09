package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import lk.jobs.utils.DateParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoosterJobsScraper implements JobScraper {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public RoosterJobsScraper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            // Searching for "software" to get a broad list of relevant engineering roles
            String requestBody = "{\"query\":[\"software\"],\"limit\":100,\"page\":1,\"filters\":{}}";

            Connection.Response response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Origin", "https://rooster.jobs")
                    .header("Referer", "https://rooster.jobs/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .requestBody(requestBody)
                    .ignoreHttpErrors(true)
                    .execute();

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                System.err.println("Rooster API Failed. Status: " + response.statusCode());
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

                    if (title.isEmpty() || !isRelevant(title)) continue;

                    // 1. GET & CLEAN RAW DESCRIPTION
                    String rawHtml = node.path("description").asText("");
                    if (rawHtml.isEmpty()) {
                        rawHtml = node.path("body").asText("");
                    }
                    String cleanDescription = Jsoup.parse(rawHtml).text();

                    // 2. USE YOUR UPDATED DATEPARSER
                    String createdAtStr = node.path("created_at").asText();
                    LocalDateTime postedDate = DateParser.parseDate(createdAtStr);

                    // 3. LIGHTWEIGHT SKILLS EXTRACTION
                    List<String> extractedSkills = extractSkills(title + " " + cleanDescription);

                    LocalDate cutoff = LocalDate.now().minusDays(Config.getInt("max.days.old", 14));

                    if (!postedDate.toLocalDate().isBefore(cutoff)) {
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title, node.path("job_type").asText("")),
                                getSourceName(),
                                "https://rooster.jobs/jobs/" + jobId,
                                postedDate,
                                LocalDate.now(),
                                cleanDescription // NEW: Full description for PostgreSQL
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Rooster Scraper Exception: " + e.getMessage());
        }
        return jobs;
    }

    private List<String> extractSkills(String text) {
        if (text == null || text.isEmpty()) return List.of();
        String lowerText = text.toLowerCase();

        return Config.getTechKeywords().stream()
                .filter(skill -> lowerText.contains(skill.toLowerCase()))
                .distinct()
                .toList();
    }

    private boolean isRelevant(String title) {
        String lowerTitle = title.toLowerCase();
        boolean isBlocked = Config.getBlockedKeywords().stream().anyMatch(lowerTitle::contains);
        if (isBlocked) return false;

        boolean hasTech = Config.getTechKeywords().stream().anyMatch(lowerTitle::contains);
        boolean hasRole = Config.getTechJobKeywords().stream().anyMatch(lowerTitle::contains);

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