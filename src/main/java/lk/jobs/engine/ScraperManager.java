package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.scrapers.ITProScraper;
import lk.jobs.scrapers.JobScraper;
import lk.jobs.utils.Config;
import lk.jobs.utils.JsonStore;

import java.util.ArrayList;
import java.util.List;

public class ScraperManager {
    private final List<JobScraper> scrapers = new ArrayList<>();
    private final JsonStore jsonStore = new JsonStore();
    // 1. You must create the cleaner instance here
    private final DataCleaner dataCleaner = new DataCleaner();

    public void run() {
        System.out.println("🚀 Starting Job Scraper Engine...");

        // 2. Fix the URL config call (remove the "14")
        String itProURL = Config.get("itpro.api.url");
        scrapers.add(new ITProScraper(itProURL));

        List<Job> allNewJobs = new ArrayList<>();

        // Execute all scrapers
        for (JobScraper scraper : scrapers) {
            System.out.println("🔍 Scraping: " + scraper.getSourceName());
            allNewJobs.addAll(scraper.scrape());
        }

        // 3. Clean the NEW data first
        List<Job> cleanedNewJobs = dataCleaner.clean(allNewJobs);

        // 4. Merge the CLEANED data with the existing store
        System.out.println("💾 Merging and saving " + cleanedNewJobs.size() + " new jobs...");
        jsonStore.saveAndMerge(cleanedNewJobs);

        // 5. Update the README using the FULL list from the store (Old + New)
        List<Job> finalJobHistory = jsonStore.load();
        new MarkdownGenerator().update(finalJobHistory);

        System.out.println("✅ Process completed successfully.");
    }

    public static void main(String[] args) {
        new ScraperManager().run();
    }
}