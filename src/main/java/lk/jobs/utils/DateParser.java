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
        //for the sri lankan time zone
        ZoneId slZone = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(slZone);

        if (dateStr == null || dateStr.isEmpty()) return now;

        String input = dateStr.toLowerCase().trim();
        System.out.println("input:"+input);

        // 1. Check if it's already an ISO string (from our JSON)
        if (input.contains("t") && input.length() > 10) {
            try { return LocalDateTime.parse(dateStr); } catch (Exception e) {
                System.out.println("Error:parsing ISO: "+e);
            }
        }
        // Handle relative strings (X days ago)
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

        // Handle Absolute Dates (e.g., "28 Feb 2026" or "02 Mar 2026")
        try {
            //for ITPro.lk
            DateTimeFormatter mySqlformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try{
                return LocalDateTime.parse(dateStr, mySqlformatter);
            }catch(Exception e){
                System.out.println("failed to parse dateTime:"+ e);
            }

            //TopJobs Format: "Sat Mar 07 2026"

                DateTimeFormatter topJobsFormatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("EEE MMM dd yyyy")
                        .toFormatter(Locale.ENGLISH);

                DateTimeFormatter generalFormatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("d MMM yyyy")
                        .toFormatter(Locale.ENGLISH);

                try{
                    if(input.split(" ").length==4){
                        LocalDate parsedDate = LocalDate.parse(input, topJobsFormatter);
                        return handleLocalDate(parsedDate,slZone);
                    }
                    //07 mar 2026
                    LocalDate parsedGeneralDate = LocalDate.parse(input, generalFormatter);
                    return handleLocalDate(parsedGeneralDate, slZone);
                }catch (Exception e){

                }

            // fallback - date only
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
            LocalDate parsedDate = LocalDate.parse(dateStr, formatter);
            LocalDate today = LocalDate.now(slZone);

            // If it's today, return now to keep the "mins ago" fresh
            if (parsedDate.equals(today)) {
                return parsedDate.atStartOfDay();
            }
            // Otherwise return Noon on that day for a clean "X days ago" display
            return parsedDate.atTime(12, 0);
        } catch (Exception e) {

            // Last resort: standard ISO date yyyy-MM-dd
            try {
                return LocalDate.parse(dateStr).atTime(12, 0);
            } catch (Exception e2) {
                System.err.println("Could not parse date: [" + dateStr + "]. Defaulting to now.");
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