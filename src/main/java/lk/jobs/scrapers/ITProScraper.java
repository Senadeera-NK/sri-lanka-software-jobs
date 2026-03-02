package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;
import lk.jobs.utils.DateParser;
import lk.jobs.utils.DateParser; // Updated to match our utility name

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ITProScraper implements JobScraper {
    private final String apiUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public ITProScraper(String apiUrl){
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        int[] techCategoryIds = {21, 35, 38, 39, 42};
        int daysBehind = 15;

        for (int catId : techCategoryIds) {
            try {
                String finalUrl = apiUrl.trim() + "?action=getJobs&category=" + catId +
                        "&days_behind=" + daysBehind + "&random=0&type=0&response=json";

                java.net.URL url = new java.net.URL(finalUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

                // Set headers manually to mimic a high-authority browser request
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
                conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
                conn.setRequestProperty("Referer", "https://itpro.lk/");
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                String rawBody = content.toString().trim();

                // DETOX: Extract the JSON array inside var jobs = [...]
                String jsonOnly = rawBody;
                if (jsonOnly.contains("[")) {
                    jsonOnly = jsonOnly.substring(jsonOnly.indexOf("["), jsonOnly.lastIndexOf("]") + 1);
                }

                if (jsonOnly.startsWith("<")) {
                    System.err.println("Cat " + catId + " still blocked by HTML documentation.");
                    continue;
                }

                JsonNode root = mapper.readTree(jsonOnly);
                if (root.isArray()) {
                    for (JsonNode node : root) {
                        String title = node.path("title").asText();
                        String company = node.path("company").asText();
                        String id = node.path("id").asText();
                        String createdOn = node.path("created_on").asText();
                        System.out.println("create on:"+createdOn);

                        String slug = (title + " at " + company).toLowerCase()
                                .replaceAll("[^a-z0-9\\s]", "")
                                .replaceAll("\\s+", "-");
                        String jobUrl = "https://itpro.lk/job/" + id + "/" + slug + "/";

                        jobs.add(new Job(
                                title, company, determineLevel(title),
                                getSourceName(), jobUrl,
                                DateParser.parseDate(createdOn),
                                LocalDate.now()
                        ));
                    }
                    System.out.println("Cat " + catId + " Success: Found " + root.size() + " jobs.");
                }
            } catch (Exception e) {
                System.err.println("Error on Cat " + catId + ": " + e.getMessage());
            }
        }
        return jobs;
    }

    private String determineLevel(String title){
        String t = title.toLowerCase();
        if(t.contains("intern")) return "Intern"; // Removed 's' to catch both Intern and Interns
        if(t.contains("associate") || t.contains("trainee")) return "Associate";
        if(t.contains("senior") || t.contains("sr") || t.contains("lead")) return "Senior";
        return "Junior/SE";
    }

    @Override
    public String getSourceName() {
        return "ITPro.lk";
    }
}