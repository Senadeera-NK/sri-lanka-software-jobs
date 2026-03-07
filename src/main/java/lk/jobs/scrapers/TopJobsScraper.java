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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopJobsScraper implements JobScraper {

    // The Software Dev & QA category URL
    private static final String TARGET_URL = "https://www.topjobs.lk/applicant/vacancybyfunctionalarea.jsp?FA=SDQ&jst=OPEN";

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // Select all rows that have an 'onclick' starting with 'createAlert'
            Elements rows = doc.select("tr[onclick^=createAlert]");

            for (Element row : rows) {
                try {
                    // 1. Extract IDs from the onclick attribute for the URL
                    // Example: createAlert('13','0000000484','0001475899','0000000651',...)
                    String onClick = row.attr("onclick");
                    String directLink = buildDirectLink(row);

                    // 2. Extract Text Data
                    String title = row.select("h2").text().trim()
                            .replaceAll("\\s"," ")//replacing multiple spaces/newlines
                            .replaceAll("\\?","- ");//fixing weird marks
                    String company = row.select("h1").text().trim();

                    // The 5th column (index 4) contains the date: "Sat Mar 07 2026"
                    String dateStr = row.select("td").get(4).text().trim();

                    if (!title.isEmpty() && !company.isEmpty()) {
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title),
                                getSourceName(),
                                directLink,
                                DateParser.parseDate(dateStr),
                                LocalDate.now()
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
        // Inside your TopJobsScraper row loop
        String jc = row.select("span[id^=hdnJC]").text().trim();
        String ec = row.select("span[id^=hdnEC]").text().trim();
        String ac = row.select("span[id^=hdnAC]").text().trim();

// Construct the URL
        String directLink = String.format(
                "https://www.topjobs.lk/employer/JobAdvertismentServlet?ac=%s&jc=%s&ec=%s&pg=applicant/vacancybyfunctionalarea.jsp",
                ac, jc, ec
        );
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