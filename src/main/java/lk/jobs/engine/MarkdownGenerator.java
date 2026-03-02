package lk.jobs.engine;

import lk.jobs.model.Job;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MarkdownGenerator {

    private static final String README_PATH = "README.md";
    private static final String START_MARKER = "";
    private static final String END_MARKER = "";

    public void update(List<Job> jobs) {
        try {
            String content = Files.readString(Paths.get(README_PATH));

            // 1. Generate the new table string
            String newTable = generateTable(jobs);

            // 2. Add the "Last Updated" metadata
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            String metaInfo = "> 🟢 **Last Updated:** " + timestamp + " | **Total Jobs Found:** " + jobs.size() + "\n\n";

            // 3. Regex replace everything between the markers
            String pattern = "(?s)" + START_MARKER + ".*?" + END_MARKER;
            String replacement = START_MARKER + "\n" + metaInfo + newTable + "\n" + END_MARKER;

            String updatedContent = content.replaceAll(pattern, replacement);

            Files.writeString(Paths.get(README_PATH), updatedContent);
            System.out.println("📝 README.md updated successfully!");

        } catch (IOException e) {
            System.err.println("❌ Error updating README: " + e.getMessage());
        }
    }

    private String generateTable(List<Job> jobs) {
        StringBuilder sb = new StringBuilder();
        // Table Header (Now includes Experience!)
        sb.append("| Title | Company | Level | Exp | Source | Posted | Link |\n");
        sb.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");

        for (Job job : jobs) {
            sb.append(String.format("| %s | %s | %s | %s | %s | %s | [View](%s) |\n",
                    job.title(),
                    job.company(),
                    job.level(),
                    job.experience(), // Added Experience level here
                    job.source(),
                    job.datePosted(),
                    job.link()
            ));
        }
        return sb.toString();
    }
}