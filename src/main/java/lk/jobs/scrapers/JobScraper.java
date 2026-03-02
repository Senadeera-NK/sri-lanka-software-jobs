package lk.jobs.scrapers;

import lk.jobs.model.Job;

import java.util.List;

//the "rules" of the jobs scrapping common for all 'job sites'
public interface JobScraper {
    /*
    * connects to the source, extracts jobs, and maps them to the job record
    * @return A list of job objects found */
    List<Job> scrape();

    /*
    * identifies which site this scraper belongs to, useful for logging and the "source"
    * @return the name of the site("topJobs.lk")*/
    String getSourceName();

    /*checks if scraper is healthy, useful if a website changes its HTML and breaks the scraper*/
    default boolean isHealthy(){
        return true;
    }
}
