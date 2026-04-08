package lk.jobs.scrapers;

import lk.jobs.model.Job;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalTime.now;

public class XpressJobsScraper implements JobScraper{
    private final String apiUrl;

    public XpressJobsScraper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();

        try{
//        using the injected url with a realistic user-agent to avoid 403s
            Document doc = Jsoup.connect(apiUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
//            expressjobs uses .job_list_bow for the main job cards
            Elements jobCards = doc.select(".job_list_box");

            for(Element card:jobCards){
                try{
                    String title = card.select(".job-title a").text().trim();
                    String company = card.select(".job_company").text().trim();
                    String link = card.select(".job-title a").attr("href");

//                    handle "x days left" logic
                    String daysLeftText = card.select("div:contains(days left)").first() != null ?
                            card.select("div:contains(days left)").first().text():"";
                    
                    LocalDate estimatedPostedDate = calculatePostedDate(daysLeftText);

                    if(!title.isEmpty() && !company.isEmpty()){
                        jobs.add(new Job(
                                title,
                                company,
                                determineLevel(title),
                                getSourceName(),
                                link,
                                estimatedPostedDate.atStartOfDay(),
                                LocalDate.now()
                        ));
                    }
                }catch(Exception e){
                    System.out.println("error parsing xpressjobs card: "+e.getMessage());
                }
            }
            
        }catch(Exception e){
            System.err.println("xpressjobs connection error: "+e.getMessage());
        }
        return jobs;
    }

    private String determineLevel(String title) {
        String t = title.toLowerCase();
        if(t.contains("intern")) return "Intern";
        if(t.contains("associate") || t.contains("trainee"))return "Associate";
        if(t.contains("senior") || t.contains("sr")||t.contains("lead"))return "Senior";
        return "Junior/SE";
    }

    private LocalDate calculatePostedDate(String daysLeftText) {
        if(daysLeftText == null ||daysLeftText.isEmpty())return LocalDate.now();

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(daysLeftText);

        if(matcher.find()){
            int daysLeft = Integer.parseInt(matcher.group(1));
//            standard 30-days window estimate
            int daysAgo = 30 - daysLeft;
            return LocalDate.now().minusDays(Math.max(0,daysAgo));
        }
        return LocalDate.now();
    }

    @Override
    public String getSourceName() {
        return "xpress.jobs";
    }
}
