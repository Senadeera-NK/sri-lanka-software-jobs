package lk.jobs.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {
    public static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now().atStartOfDay();
        }

        String input = dateStr.toLowerCase().trim();
        LocalDateTime now = LocalDateTime.now();

        //handle hours ago
        Pattern hourPattern = Pattern.compile("(\\d+)\\s+hours?\\s+ago");
        Matcher hourMatcher = hourPattern.matcher(input);
        if(hourMatcher.find()){
            return now.minusHours(Integer.parseInt(hourMatcher.group(1)));
        }
        // 1. Handle "Today" or "Just now"
        if (input.contains("today") || input.contains("now")) {
            return now;
        }

        // 2. Handle "Yesterday"
        if (input.contains("yesterday")) {
            return now.minusDays(1);
        }

        // 3. Handle relative "X days ago" or "X weeks ago"
        // Regex looks for a number followed by 'day' or 'week'
        Pattern relativePattern = Pattern.compile("(\\d+)\\s+(minute|hour|day|week|month)s?\\s+ago");
        Matcher matcher = relativePattern.matcher(input);

        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            return switch (unit) {
                case "day" -> now.minusDays(amount);
                case "week" -> now.minusWeeks(amount);
                case "month" -> now.minusMonths(amount);
                default -> now;
            };
        }

        // 4. Fallback to Absolute Date Formats
        // In DateParser.java fallback (Step 4)
        try {
            DateTimeFormatter itproFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateStr, itproFormatter);
        } catch (Exception e) {
            // If it's just a date without time
            try { return LocalDate.parse(dateStr).atStartOfDay(); }
            catch (Exception e2) { return now; }
        }
    }
}