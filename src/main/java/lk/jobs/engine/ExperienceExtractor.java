package lk.jobs.engine; // Or lk.jobs.utils

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperienceExtractor {

    // Regex explained: Matches digits (1-2) followed by "year", "years", or "yrs"
    // Handles variations like "2+ years", "1-3 yrs", "5 years"
    private static final String EXP_PATTERN = "(\\d+)\\s*(\\+|-|to)?\\s*(\\d+)?\\s*(years?|yrs?)";

    public static String extract(String description) {
        if (description == null || description.isEmpty()) return "Not Specified";

        String text = description.toLowerCase();

        // 1. Check for entry-level keywords first
        if (text.contains("fresher") || text.contains("no experience") || text.contains("trainee")) {
            return "0 - 1 Year";
        }

        // 2. Use Regex to hunt for year patterns
        Pattern pattern = Pattern.compile(EXP_PATTERN);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // Returns the first match it finds, e.g., "2+ years"
            String result = matcher.group(0);
            return capitalize(result);
        }

        return "Check Desc.";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}