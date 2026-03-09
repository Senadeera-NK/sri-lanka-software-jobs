package lk.jobs.engine;

import lk.jobs.model.Job;
import lk.jobs.scrapers.ITProScraper;
import lk.jobs.scrapers.JobScraper;
import lk.jobs.utils.Config;
import lk.jobs.utils.JsonStore;
import lk.jobs.scrapers.TopJobsScraper;
import java.util.ArrayList;
import java.util.List;

public class ScraperManager {
    private final List<JobScraper> scrapers = new ArrayList<>();
    private final JsonStore jsonStore = new JsonStore();
    //creating the cleaner instance
    private final DataCleaner dataCleaner = new DataCleaner();

    public void run() {
        System.out.println("🚀 Starting Job Scraper Engine...");

        //fixing the URL config call (remove the "14")
        String itProURL = Config.get("itpro.api.url");
        scrapers.add(new ITProScraper(itProURL));

        String TopjobsURL = Config.get("topjobs.url");
        scrapers.add(new TopJobsScraper(TopjobsURL));

        List<Job> allNewJobs = new ArrayList<>();

        // Execute all scrapers
        for (JobScraper scraper : scrapers) {
            System.out.println("🔍 Scraping: " + scraper.getSourceName());
            allNewJobs.addAll(scraper.scrape());
        }
        //loading history data from json
        List<Job> existingHistory = jsonStore.load();

        //combining all into one master list
        List<Job> masterList = new ArrayList<>(existingHistory);
        masterList.addAll(allNewJobs);

        //Clean the NEW data first
        List<Job> finalCleanedList = dataCleaner.clean(masterList);

        //Merge the CLEANED data with the existing store
        System.out.println("Merging and saving " + finalCleanedList.size() + " update jobs to history..");
        jsonStore.save(finalCleanedList);

        // Update the README using the FULL list from the store (Old + New)
        new MarkdownGenerator().update(finalCleanedList);

        System.out.println("Process completed successfully.");
    }

    public static void main(String[] args) {
        new ScraperManager().run();
    }
}