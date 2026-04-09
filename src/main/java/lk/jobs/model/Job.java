package lk.jobs.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// the data structure of the "job" in the list
public record Job (
        String title,
        String company,
        String level,//Intern, Associate, Junior, Senior
        String source, //TopJobs, ITPro, Rooster
        String link,
        LocalDateTime datePosted,  //actual date it was put online
        LocalDate scrapedDate, //today's date(when the bot found it)
        String description
){
    public String id(){
        return (company + title + source).toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    public String getTitle() {
        return this.title;
    }

    public String getCompany() {
        return this.company;
    }

    public String getLevel() {
        return this.level;
    }

    public String getSource() {
        return this.source;
    }

    public String getLink() {
        return this.link;
    }

    public LocalDateTime getDatePosted() {
        return this.datePosted;
    }

    public String getDescription() {
        return this.description;
    }
}
