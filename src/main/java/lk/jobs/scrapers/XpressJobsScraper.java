package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import lk.jobs.utils.DateParser;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class XpressJobsScraper implements JobScraper {
    private final String listApiUrl;
    private final String detailApiBase = "https://xpress.jobs/api/jobs/publishedJob?jobId=";
    private final ObjectMapper mapper = new ObjectMapper();

    public XpressJobsScraper(String listApiUrl) {
        this.listApiUrl = listApiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            // Step 1: Fetch the main job list
            String listJson = fetchJson(listApiUrl);
            JsonNode rootArray = mapper.readTree(listJson);

            if (rootArray.isArray()) {
                System.out.println("DEBUG [XpressJobs]: Found " + rootArray.size() + " total jobs. Filtering...");

                for (JsonNode node : rootArray) {
                    String title = node.path("jobTitle").asText("").trim();

                    // Filter early so we don't waste API calls on irrelevant jobs
                    if (!isRelevant(title)) continue;

                    int jobId = node.path("jobId").asInt();

                    // Step 2: Fetch the Full Detail for this specific Job
                    // This is where we get the 'jobInfo' field you need.
                    try {
                        String detailJson = fetchJson(detailApiBase + jobId);
                        JsonNode detailNode = mapper.readTree(detailJson);

                        // Extract from Detail Node
                        JsonNode jobItem = detailNode.path("jobItem");
                        String company = jobItem.path("organizationName").asText("Unknown").trim();
                        String rawHtml = detailNode.path("jobInfo").asText("");
                        String cleanDescription = Jsoup.parse(rawHtml).text().trim();

                        // Date Parsing
                        String dateStr = detailNode.path("createdDate").asText();
                        LocalDateTime postedDate = DateParser.parseDate(dateStr);

                        LocalDate cutoff = LocalDate.now().minusDays(Config.getInt("max.days.old", 14));

                        if (!postedDate.toLocalDate().isBefore(cutoff)) {
                            jobs.add(new Job(
                                    title,
                                    company,
                                    determineLevel(title),
                                    getSourceName(),
                                    "https://xpress.jobs/jobs/view/" + jobId,
                                    postedDate,
                                    LocalDate.now(),
                                    cleanDescription.isEmpty() ? title : cleanDescription
                            ));
                        }

                        // Small delay to be polite to the server
                        Thread.sleep(200);

                    } catch (Exception detailEx) {
                        System.err.println("Failed to fetch detail for job " + jobId + ": " + detailEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("XpressJobs Scraper Error: " + e.getMessage());
        }
        return jobs;
    }

    private String fetchJson(String url) throws Exception {
        return Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .execute()
                .body();
    }

    private boolean isRelevant(String title) {
        String lowerTitle = title.toLowerCase();
        boolean isBlocked = Config.getBlockedKeywords().stream().anyMatch(lowerTitle::contains);
        if (isBlocked) return false;

        boolean hasTech = Config.getTechKeywords().stream().anyMatch(lowerTitle::contains);
        boolean hasRole = Config.getTechJobKeywords().stream().anyMatch(lowerTitle::contains);
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