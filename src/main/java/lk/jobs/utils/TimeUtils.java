package lk.jobs.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    private static final ZoneId SL_ZONE = ZoneId.of("Asia/Colombo");
    public static String getRelativeTime(java.time.LocalDateTime dateTime) {
        //converting the stored localDatetime to a zoned instance
        ZonedDateTime jobTime = dateTime.atZone(SL_ZONE);

        //get the actual current time in Sri Lanka
        ZonedDateTime now = ZonedDateTime.now(SL_ZONE);

        long seconds = ChronoUnit.SECONDS.between(dateTime, now);
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        //(due to slight clock drift = mins<60 ->min 0
        if(seconds<60){
            return (seconds<=10)?"Just now":seconds+" secs ago";
        }
        else if (minutes < 60) {
            return  minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "Yesterday";
        } else {
            return days + " days ago";
        }
    }
}