package lk.jobs.model;

import java.time.LocalDate;

// the data structure of the "job" in the list
public record Job (
        String title,
        String company,
        String level,//Intern, Associate, Junior, Senior
        String experience, //" - " if not mentioned, "1-2 years", "fresh graduate"
        String source, //TopJobs, ITPro, Rooster
        String link,
        LocalDate datePosted,  //actual date it was put online
        LocalDate scrapedDate //today's date(when the bot found it)
){
    public String id(){
        String raw = company+"-"+title+"-"+source+"-"+datePosted;
        return raw.toLowerCase().
                replace(" ","-")
                .replaceAll("[^a-z0-9]", "");
    }
}
