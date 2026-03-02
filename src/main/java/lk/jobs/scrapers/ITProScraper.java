package lk.jobs.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.jobs.model.Job;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static lk.jobs.utils.DateParser.parseDate;

// specific rules/logics related to "ITPro" site scrapping
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
                    .uri(URI.create(apiUrl+"?action=getJobs&response=json&count=50"))
                    .header("Accept","application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            //ITPro usually returns an array of jobs
            for(JsonNode node:root){
                String title = node.get("title").asText();
                String company = node.get("company").asText();

                //to determine level/experience if API doesnt provide it
                String level = determineLevel(title);
                String exp = node.has("experience")?node.get("experience").asText():"Not specified";

                jobs.add(new Job(
                        title,
                        company,
                        level,
                        exp,
                        "ITPro.lk",
                        node.get("url").asText(),
                        parseDate(node.get("posted_date").asText()), // Use your DateUtils here
                        LocalDate.now()
                ));
            }
        }catch(Exception e){
            System.out.println("Error scraping ITPro: "+e.getMessage());
        }
        return jobs;
    }

    private String determineLevel(String title){
        String t = title.toLowerCase();
        if(t.contains("interns")) return "Intern";
        if(t.contains("associate")) return "Associate";
        if(t.contains("senior")|| t.contains("sr")) return "Senior";
        return "Junior/SE";
    }

    @Override
    public String getSourceName() {
        return "ITPro.lk";
    }
}
