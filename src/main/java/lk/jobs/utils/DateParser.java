package lk.jobs.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {
    public static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return LocalDateTime.now();
        String input = dateStr.toLowerCase().trim();
        System.out.println("input:"+input);

        //for the sri lankan time zone
        ZoneId slZone = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(slZone);

        // 1. Check if it's already an ISO string (from our JSON)
        if (input.contains("t") && input.length() > 10) {
            try { return LocalDateTime.parse(dateStr); } catch (Exception e) {}
        }
        // 2. Handle relative strings (X days ago)
        Pattern p = Pattern.compile("(\\d+)\\s+(minute|hour|day|week|month)s?\\s+ago");
        Matcher m = p.matcher(input);
        if (m.find()) {
            int amt = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            return switch (unit) {
                case "minute" -> now.minusMinutes(amt);
                case "hour" -> now.minusHours(amt);
                case "day" -> now.minusDays(amt).withHour(12).withMinute(0);
                case "week" -> now.minusWeeks(amt).withHour(12).withMinute(0);
                default -> now;
            };
        }

        // 3. Handle Absolute Dates (e.g., "28 Feb 2026" or "02 Mar 2026")
        try {
            // Common format for ITPro and others
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
            LocalDate parsedDate = LocalDate.parse(dateStr, formatter);

            // If it's today, return now to keep the "mins ago" fresh
            if (parsedDate.equals(LocalDate.now())) {
                return now;
            }
            // Otherwise return Noon on that day for a clean "X days ago" display
            return parsedDate.atTime(12, 0);
        } catch (Exception e) {
            // Last resort: standard ISO date yyyy-MM-dd
            try {
                return LocalDate.parse(dateStr).atTime(12, 0);
            } catch (Exception e2) {
                System.err.println("⚠️ Could not parse date: [" + dateStr + "]. Defaulting to now.");
                return now;
            }
        }

    }
}