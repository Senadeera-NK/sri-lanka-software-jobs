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

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

public class TelegramNotifier {
    //to compare existingjoblists with the new job list
    public void notifyNewJobs(List<Job> currentScrappedJobs, List<Job> existingHistory){
        //identifying only brand new jobs
        Set<String> oldUrls = existingHistory.stream().map(Job::link).collect(Collectors.toSet());
        List<Job> newJobs = currentScrappedJobs.stream()
                .filter(job-> !oldUrls.contains(job.link()))
                .toList();

        if(newJobs.isEmpty()){
            System.out.println("No new jobs to notify.");
            return;
        }

        //formating the message
        //getting the SL time
        String slTime = ZonedDateTime.now(ZoneId.of("Asia/Colombo"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        StringBuilder sb = new StringBuilder("🔥 *New Jobs Found!* (" + slTime + ")\n\n");

        for (Job job : newJobs) {
            sb.append(String.format("📍 [%s](%s)\n", job.title(), job.link()));
        }
        //footer - for the github link
        // Add the "Checkout for more" footer
        sb.append("📊 *Check out more jobs & details here:* \n");
        sb.append("[GitHub Dashboard](https://github.com/Senadeera-NK/sri-lanka-software-jobs)");
        sendToTelegram(sb.toString());
    }

    //activate the sending the message
    private void sendToTelegram(String message) {
        try {
            String token = System.getenv("TELEGRAM_BOT_TOKEN");
            String chatId = System.getenv("TELEGRAM_CHAT_ID");

            //getting the telegram url
            String baseUrl = Config.get("telegram.api.url");
            String urlString = String.format(baseUrl, token, chatId, URLEncoder.encode(message, StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Telegram notification sent!");
        } catch (Exception e) {
            System.err.println("Failed to send Telegram: " + e.getMessage());
        }
    }
}
