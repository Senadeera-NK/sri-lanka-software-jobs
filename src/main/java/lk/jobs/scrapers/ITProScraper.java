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
        try(HttpClient client = HttpClient.newHttpClient()){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?action=getJobs&response=json&count=50"))
                    .header("Accept","application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            // Check if root is an array before looping
            if (root.isArray()) {
                for(JsonNode node : root){
                    //System.out.println("JSON Node: " + node.toString());
                    // .path() is safer than .get()
                    String id = node.path("id").asText();
                    String title = node.path("title").asText("Unknown Title");
                    String company = node.path("company").asText("Unknown Company");

                    // 1. Create a URL-friendly slug (lowercase, replace spaces/special chars with hyphens)
                    // We combine title + " at " + company to match ITPro's format
                    String combined = title + " at " + company;
                    String slug = combined.toLowerCase()
                            .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                            .replaceAll("\\s+", "-");        // Replace spaces with hyphens

                    // 2. Construct the full URL
                    String jobUrl = "https://itpro.lk/job/" + id + "/" + slug + "/";

                    // Fixed the date logic
                    String rawDate = node.has("posted_date")
                            ? node.path("posted_date").asText()
                            : LocalDate.now().toString();

                    String level = determineLevel(title);

                    jobs.add(new Job(
                            title,
                            company,
                            level,
                            getSourceName(),
                            jobUrl,
                            DateParser.parseDate(rawDate),
                            LocalDate.now()
                    ));
                }
            }
        } catch(Exception e) {
            // This will now give us more detail if it fails
            e.printStackTrace();
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