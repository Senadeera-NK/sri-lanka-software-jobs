package lk.jobs.repository;

import lk.jobs.model.Job;
import lk.jobs.db.DatabaseConnection;

import java.sql.*;

public class JobRepository {
    private static final String UPSERT_SQL =
            "INSERT INTO jobs (title, company, job_level, source_name, job_link, date_posted, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (job_link) DO UPDATE SET " +
                    "title = EXCLUDED.title, " +
                    "description = EXCLUDED.description, " +
                    "scraped_at = CURRENT_TIMESTAMP";

    public void save(Job job) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPSERT_SQL)) {

            pstmt.setString(1, job.getTitle());
            pstmt.setString(2, job.getCompany());
            pstmt.setString(3, job.getLevel());
            pstmt.setString(4, job.getSource());
            pstmt.setString(5, job.getLink());
            pstmt.setTimestamp(6, Timestamp.valueOf(job.getDatePosted()));
            pstmt.setString(7, job.getDescription());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database Save Error: " + e.getMessage());
        }
    }
}