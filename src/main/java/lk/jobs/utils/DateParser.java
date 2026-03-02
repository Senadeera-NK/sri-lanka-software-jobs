package lk.jobs.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }

        String input = dateStr.toLowerCase().trim();

        // 1. Handle "Today" or "Just now"
        if (input.contains("today") || input.contains("now")) {
            return LocalDate.now();
        }

        // 2. Handle "Yesterday"
        if (input.contains("yesterday")) {
            return LocalDate.now().minusDays(1);
        }

        // 3. Handle relative "X days ago" or "X weeks ago"
        // Regex looks for a number followed by 'day' or 'week'
        Pattern relativePattern = Pattern.compile("(\\d+)\\s+(day|week|month)s?\\s+ago");
        Matcher matcher = relativePattern.matcher(input);

        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            return switch (unit) {
                case "day" -> LocalDate.now().minusDays(amount);
                case "week" -> LocalDate.now().minusWeeks(amount);
                case "month" -> LocalDate.now().minusMonths(amount);
                default -> LocalDate.now();
            };
        }

        // 4. Fallback to Absolute Date Formats
        try {
            return LocalDate.parse(dateStr); // ISO YYYY-MM-DD
        } catch (Exception e) {
            try {
                // Handle "01 Mar 2026"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e2) {
                // If all else fails, assume today so the job isn't lost
                return LocalDate.now();
            }
        }
    }
}