package lk.jobs.engine;

import lk.jobs.model.Job;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownGenerator {

    private static final String README_PATH = "README.md";
    // 1. Give these actual values!
    private static final String START_MARKER = "";
    private static final String END_MARKER = "";

    public void update(List<Job> jobs) {
        try {
            Path path = Paths.get(README_PATH);
            String content = Files.readString(path, StandardCharsets.UTF_8);

            String newTable = generateTable(jobs);
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            String metaInfo = "> 🟢 **Last Updated:** " + timestamp + " | **Total Jobs Found:** " + jobs.size() + "\n\n";

            // 2. Prepare the replacement block
            String replacementBlock = START_MARKER + "\n" + metaInfo + newTable + "\n" + END_MARKER;

            // 3. Use Pattern.quote to handle the markers safely
            String patternString = "(?s)" + Pattern.quote(START_MARKER) + ".*?" + Pattern.quote(END_MARKER);

            // 4. Use Matcher.quoteReplacement to avoid errors with special characters in job titles
            String updatedContent = content.replaceFirst(patternString, Matcher.quoteReplacement(replacementBlock));

            Files.writeString(path, updatedContent, StandardCharsets.UTF_8);
            System.out.println("📝 README.md updated successfully!");

        } catch (IOException e) {
            System.err.println("❌ Error updating README: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateTable(List<Job> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("| Title | Company | Level | Exp | Source | Posted | Link |\n");
        sb.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");

        for (Job job : jobs) {
            sb.append(String.format("| %s | %s | %s | %s | %s | %s | [View](%s) |\n",
                    job.title(), job.company(), job.level(), job.experience(),
                    job.source(), job.datePosted(), job.link()
            ));
        }
        return sb.toString();
    }
}