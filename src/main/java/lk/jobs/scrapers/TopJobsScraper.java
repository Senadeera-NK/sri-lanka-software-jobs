package lk.jobs.scrapers;

import lk.jobs.model.Job;
import lk.jobs.utils.DateParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TopJobsScraper implements JobScraper {
    private final String apiUrl;

    public TopJobsScraper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(apiUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Elements rows = doc.select("tr[onclick^=createAlert]");

            for (Element row : rows) {
                try {
                    String title = row.select("h2").text().trim()
                            .replaceAll("\\s", " ")
                            .replaceAll("\\?", "- ");
                    String company = row.select("h1").text().trim();
                    String directLink = buildDirectLink(row);
                    String dateStr = row.select("td").get(4).text().trim();

                    if (!title.isEmpty() && !company.isEmpty()) {
                        // We skip the fetchTopJobsDescription call entirely.
                        // It saves time and prevents unnecessary 404s/image-only pages.
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title),
                                getSourceName(),
                                directLink,
                                DateParser.parseDate(dateStr),
                                LocalDate.now(),
                                title // Using Title as the description for TopJobs
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing TopJobs row: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("TopJobs Connection Error: " + e.getMessage());
        }
        return jobs;
    }

    private String buildDirectLink(Element row) {
        String jc = row.select("span[id^=hdnJC]").text().trim();
        String ec = row.select("span[id^=hdnEC]").text().trim();
        String ac = row.select("span[id^=hdnAC]").text().trim();

        return String.format("https://www.topjobs.lk/employer/JobAdvertismentServlet?ac=%s&jc=%s&ec=%s", ac, jc, ec);
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
        return "TopJobs.lk";
    }
}