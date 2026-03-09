package lk.jobs.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {
    public static LocalDateTime parseDate(String dateStr) {
        ZoneId slZone = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(slZone);

        if (dateStr == null || dateStr.trim().isEmpty()) return now;

        // 1. Clean data and determine "parts" for absolute dates
        String cleanData = dateStr.trim().replaceAll("\\s+", " ");
        String inputLower = cleanData.toLowerCase();
        String[] parts = cleanData.split(" ");

        // 2. Handle Relative Strings (X days ago) - Stay with lowercase 'inputLower'
        Pattern p = Pattern.compile("(\\d+)\\s+(minute|hour|day|week|month)s?\\s+ago");
        Matcher m = p.matcher(inputLower);
        if (m.find()) {
            int amt = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            return switch (unit) {
                case "minute" -> now.minusMinutes(amt);
                case "hour" -> now.minusHours(amt);
                case "day", "week" -> {
                    LocalDateTime dt = unit.equals("day") ? now.minusDays(amt) : now.minusWeeks(amt);
                    yield dt.withHour(12).withMinute(0);
                }
                default -> now;
            };
        }

        // 3. Handle Absolute Formats
        try {
            // MySQL Format (ITPro)
            if (cleanData.contains("-") && cleanData.contains(":")) {
                return LocalDateTime.parse(cleanData, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            // TopJobs (EEE MMM dd yyyy)
            if (parts.length == 4) {
                DateTimeFormatter topJobsFormatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive().appendPattern("EEE MMM dd yyyy").toFormatter(Locale.ENGLISH);
                return handleLocalDate(LocalDate.parse(cleanData, topJobsFormatter), slZone);
            }

            // General (d MMM yyyy)
            if (parts.length == 3) {
                DateTimeFormatter generalFormatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive().appendPattern("d MMM yyyy").toFormatter(Locale.ENGLISH);
                return handleLocalDate(LocalDate.parse(cleanData, generalFormatter), slZone);
            }

            // 4. LAST RESORT: Try standard ISO (only if nothing else matched)
            return LocalDateTime.parse(dateStr);

        } catch (Exception e) {
            // Only print if we are totally lost
            try {
                return LocalDate.parse(cleanData).atTime(12, 0);
            } catch (Exception e2) {
                return now;
            }
        }

    }
    private static LocalDateTime handleLocalDate(LocalDate parsedDate, ZoneId slZone) {
        LocalDate today = LocalDate.now(slZone);
        if (parsedDate.equals(today)) {
            return parsedDate.atStartOfDay();
        }
        return parsedDate.atTime(12, 0);
    }
}