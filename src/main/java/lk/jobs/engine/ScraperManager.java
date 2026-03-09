package lk.jobs.engine;

import io.github.cdimascio.dotenv.Dotenv;
import lk.jobs.model.Job;
import lk.jobs.scrapers.ITProScraper;
import lk.jobs.scrapers.JobScraper;
import lk.jobs.utils.Config;
import lk.jobs.utils.JsonStore;
import lk.jobs.scrapers.TopJobsScraper;
import lk.jobs.notifier.TelegramNotifier;
import java.util.ArrayList;
import java.util.List;


public class ScraperManager {
    private final List<JobScraper> scrapers = new ArrayList<>();
    private final JsonStore jsonStore = new JsonStore();
    //creating the cleaner instance
    private final DataCleaner dataCleaner = new DataCleaner();

    //creating a notifer instance
    private final TelegramNotifier telegramNotifier = new TelegramNotifier();

    public void run() {
        System.out.println("Starting Job Scraper Engine...");

        //fixing the URL config call (remove the "14")
        String itProURL = Config.get("itpro.api.url");
        scrapers.add(new ITProScraper(itProURL));

        String TopjobsURL = Config.get("topjobs.url");
        scrapers.add(new TopJobsScraper(TopjobsURL));

        List<Job> allNewJobs = new ArrayList<>();

        // Execute all scrapers
        for (JobScraper scraper : scrapers) {
            try{
                System.out.println("Scraping: " + scraper.getSourceName());
                List<Job> scrapped = scraper.scrape();
                System.out.println("Found"+scrapped.size()+" jobs from "+scraper.getSourceName());
                allNewJobs.addAll(scrapped);
            }catch(Exception e){
                System.err.println("CRITICAL ERROR on "+scraper.getSourceName()+": "+e.getMessage());
            }

        }



        //loading history data from json
        List<Job> existingHistory = jsonStore.load();

        //cleaning the new jobs first
        List<Job> cleanedNewJobs = dataCleaner.clean(allNewJobs);

        //NOTIFY FIRST - the cleaned new jobs
        telegramNotifier.notifyNewJobs(cleanedNewJobs, existingHistory);

        //combining and deduplicating for the master list
        List<Job> masterList = new ArrayList<>(existingHistory);
        masterList.addAll(cleanedNewJobs);

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
        // 1. LOAD THE ENV FIRST
        try {
            Dotenv dotenv = Dotenv.configure().load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
            System.out.println("✅ Environment variables loaded.");
        } catch (Exception e) {
            System.err.println("⚠️ Could not find .env file, falling back to System Env.");
        }

        // 2. NOW RUN THE SCRAPER
        new ScraperManager().run();
    }
}