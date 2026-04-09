package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.Config;
import lk.jobs.utils.DateParser;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ITProScraper implements JobScraper {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public ITProScraper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        int[] techCategoryIds = {21, 35, 38, 39, 42};
        int daysBehind = 15;

        for (int catId : techCategoryIds) {
            try {
                String listUrl = apiUrl + "?action=getJobs&category=" + catId + "&days_behind=" + daysBehind + "&response=json";
                JsonNode root = fetchJson(listUrl);

                if (root != null && root.isArray()) {
                    for (JsonNode node : root) {
                        String id = node.path("id").asText();
                        String title = node.path("title").asText();
                        String company = node.path("company").asText("Unknown");
                        String createdOn = node.path("mysql_date").asText();

                        if (!isRelevant(title)) continue;

                        // 1. FETCH RAW DESCRIPTION
                        String fullDescription = fetchFullDescription(id);

                        // 2. LIGHTWEIGHT SKILL EXTRACTION
                        List<String> skills = extractSkills(title + " " + fullDescription);

                        String slug = (title + " at " + company).toLowerCase()
                                .replaceAll("[^a-z0-9\\s]", "")
                                .replaceAll("\\s+", "-");
                        String jobUrl = "https://itpro.lk/job/" + id + "/" + slug + "/";

                        // 3. CONSTRUCT JOB
                        // Use parseDate here. It already handles the MySQL format yyyy-MM-dd HH:mm:ss
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title),
                                getSourceName(),
                                jobUrl,
                                DateParser.parseDate(createdOn),
                                LocalDate.now(),
                                fullDescription
                        ));

                        Thread.sleep(300);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error on ITPro Cat " + catId + ": " + e.getMessage());
            }
        }
        return jobs;
    }

    private String fetchFullDescription(String jobId) {
        try {
            String detailUrl = "https://itpro.lk/api/v1/jobs/" + jobId;
            JsonNode detailNode = fetchJson(detailUrl);

            if (detailNode != null) {
                String rawHtml = "";
                if (detailNode.has("description")) {
                    rawHtml = detailNode.get("description").asText("");
                } else if (detailNode.has("body")) {
                    rawHtml = detailNode.get("body").asText("");
                }
                return Jsoup.parse(rawHtml).text();
            }
        } catch (Exception e) {
            System.err.println("Could not fetch details for ITPro Job " + jobId);
        }
        return "No description available";
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

    private JsonNode fetchJson(String urlString) throws Exception {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "application/json");

        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) content.append(line);
        in.close();

        String body = content.toString().trim();
        if (body.contains("[")) {
            body = body.substring(body.indexOf("["), body.lastIndexOf("]") + 1);
        }
        return body.startsWith("<") ? null : mapper.readTree(body);
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
        return "ITPro.lk";
    }
}