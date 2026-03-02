package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.scrapers.ITProScraper;
import lk.jobs.scrapers.JobScraper;
import lk.jobs.utils.Config;
import lk.jobs.utils.JsonStore;

import java.util.ArrayList;
import java.util.List;

//runs all scrapers
public class ScraperManager {
    private final List<JobScraper> scrapers = new ArrayList<>();
    private final JsonStore jsonStore = new JsonStore();

    public void run() {
        System.out.println("🚀 Starting Job Scraper Engine...");

        // 1. Initialize Scrapers (We'll load URLs from config later)
        // For now, let's hardcode the ITPro URL to test
        String itProURL = Config.get("itpro.api.url", "14");
        scrapers.add(new ITProScraper(itProURL));

        List<Job> allNewJobs = new ArrayList<>();

        // 2. Execute all scrapers
        for (JobScraper scraper : scrapers) {
            System.out.println("🔍 Scraping: " + scraper.getSourceName());
            allNewJobs.addAll(scraper.scrape());
        }

        // 3. Merge with existing data, Deduplicate, and Save
        System.out.println("💾 Merging and saving " + allNewJobs.size() + " new jobs...");
        jsonStore.saveAndMerge(allNewJobs);

        // 4. Update the README (We will build this next!)
        // new ReadmeWriter().update(jsonStore.load());

        System.out.println("✅ Process completed successfully.");
    }

    public static void main(String[] args) {
        new ScraperManager().run();
    }
}