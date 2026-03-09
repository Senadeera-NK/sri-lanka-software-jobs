package lk.jobs.notifier;

import lk.jobs.model.Job;
import lk.jobs.utils.Config;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TelegramNotifier {
    //to compare existingjoblists with the new job list
    public void notifyNewJobs(List<Job> currentScrappedJobs, List<Job> existingHistory) {
        Set<String> oldUrls = existingHistory.stream().map(Job::link).collect(Collectors.toSet());
        List<Job> newJobs = currentScrappedJobs.stream()
                .filter(job -> !oldUrls.contains(job.link()))
                .toList();

        if (newJobs.isEmpty()) {
            System.out.println("No new jobs to notify.");
            return;
        }

        // 1. Group jobs by level (matching your README logic)
        List<Job> interns = newJobs.stream().filter(j -> "Intern".equalsIgnoreCase(j.level())).toList();
        List<Job> associates = newJobs.stream().filter(j -> "Junior/SE".equalsIgnoreCase(j.level()) || "Associate".equalsIgnoreCase(j.level())).toList();
        List<Job> seniors = newJobs.stream().filter(j -> "Senior".equalsIgnoreCase(j.level())).toList();

        String slTime = ZonedDateTime.now(ZoneId.of("Asia/Colombo"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));

        // 2. Send categorized messages
        sendCategoryBatch("🎓 <b>Internships & Trainees</b>", interns, slTime);
        sendCategoryBatch("💻 <b>Associate & Junior Roles</b>", associates, slTime);
        sendCategoryBatch("🚀 <b>Senior & Lead Roles</b>", seniors, slTime);

        // 3. Final Footer
        sendToTelegram("📊 <b>Check the Full Dashboard:</b>\n" +
                "<a href=\"https://github.com/Senadeera-NK/sri-lanka-software-jobs\">GitHub Tracker</a>");
    }

    //categorizing messages
    private void sendCategoryBatch(String header, List<Job> jobs, String time) {
        if (jobs.isEmpty()) return;

        // Chunking within the category if there are more than 15
        int batchSize = 15;
        for (int i = 0; i < jobs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, jobs.size());
            List<Job> batch = jobs.subList(i, end);

            StringBuilder sb = new StringBuilder();
            sb.append(header).append("\n");
            sb.append("<i>").append(time).append("</i>\n\n");

            for (Job job : batch) {
                String safeTitle = job.title().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                String safeCompany = job.company().replace("&", "&amp;");

                // Format: Title first (Bold/Link), then Company (Small/Italic) on the same line or right below
                sb.append(String.format("🔹 <a href=\"%s\"><b>%s</b></a>\n", job.link(), safeTitle));
                sb.append("   └ <i>").append(safeCompany).append("</i>\n\n");
            }

            sendToTelegram(sb.toString());
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
    }

    //activate the sending the message
    private void sendToTelegram(String message) {
        try {
            String token = System.getenv("TELEGRAM_BOT_TOKEN");
            if (token == null) token = System.getProperty("TELEGRAM_BOT_TOKEN");

            String chatId = System.getenv("TELEGRAM_CHAT_ID");
            if (chatId == null) chatId = System.getProperty("TELEGRAM_CHAT_ID");

            if (token == null || chatId == null) {
                System.err.println("❌ Env variables MISSING: Check TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID");
                return;
            }

            String urlString = "https://api.telegram.org/bot" + token + "/sendMessage";

            // CRITICAL: Escape backslashes, quotes, AND newlines for JSON compatibility
            String escapedMsg = message
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n") // This is what was likely breaking your payload
                    .replace("\r", "");

            String jsonPayload = String.format(
                    "{\"chat_id\": \"%s\", \"text\": \"%s\", \"parse_mode\": \"HTML\", \"disable_web_page_preview\": true}",
                    chatId, escapedMsg
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Batch delivered successfully!");
            } else {
                // This body will now tell you if it's "Bad Request" or "Unauthorized"
                System.err.println("❌ Telegram Error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Critical failure sending Telegram: " + e.getMessage());
        }
    }
}
