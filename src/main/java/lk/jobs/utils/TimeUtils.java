package lk.jobs.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    public static String getRelativeTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        if (minutes < 60) {
            return (minutes <= 1) ? "Just now" : minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "Yesterday";
        } else {
            return days + " days ago";
        }
    }
}