package lk.jobs.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// converting "3 days ago" into a date
public class DateParser {
    public static LocalDate parseDate(String dateStr){
        if(dateStr == null || dateStr.isEmpty()){
            return LocalDate.now();
        }

        try{
            //try ISO format (YYYY-MM-DD)
            return LocalDate.parse(dateStr);
        }catch(Exception e){
            try{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
                return LocalDate.parse(dateStr, formatter);
            }catch(Exception e2){
                return LocalDate.now();
            }
        }
    }
}
