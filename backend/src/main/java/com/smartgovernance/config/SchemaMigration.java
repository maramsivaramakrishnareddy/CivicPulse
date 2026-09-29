package com.smartgovernance.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles database schema migrations on application startup.
 * Converts VARCHAR(500) columns to LONGTEXT for media storage,
 * and adds new columns in a version-safe, non-blocking way.
 */
@Component
public class SchemaMigration {

    private static final Logger logger = LoggerFactory.getLogger(SchemaMigration.class);

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateSchema() {
        logger.info("===== STARTING DATABASE SCHEMA MIGRATION =====");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // ---------- complaint table: MODIFY COLUMN statements ----------
            logger.info("Altering complaint table columns...");
            runStatement(stmt, "ALTER TABLE complaint MODIFY COLUMN image_url LONGTEXT",
                    "complaint.image_url altered to LONGTEXT");
            runStatement(stmt, "ALTER TABLE complaint MODIFY COLUMN video_url LONGTEXT",
                    "complaint.video_url altered to LONGTEXT");
            runStatement(stmt,
                    "ALTER TABLE complaint MODIFY COLUMN status " +
                            "ENUM('APPLIED', 'COMPLAINT_VIEWED', 'ACTION_TO_BE_TAKEN', 'COMPLAINT_RESOLVED', " +
                            "'REOPENED', 'DUPLICATE', 'REJECTED') NOT NULL DEFAULT 'APPLIED'",
                    "complaint.status updated with DUPLICATE/REJECTED states");
            runStatement(stmt,
                    "ALTER TABLE complaint_history MODIFY COLUMN status " +
                            "ENUM('APPLIED', 'COMPLAINT_VIEWED', 'ACTION_TO_BE_TAKEN', 'COMPLAINT_RESOLVED', " +
                            "'REOPENED', 'DUPLICATE', 'REJECTED') NOT NULL",
                    "complaint_history.status updated with DUPLICATE/REJECTED states");

            // ---------- complaint table: ADD COLUMN (version-safe) ----------
            logger.info("Ensuring duplicate-link column exists...");
            addColumnIfMissing(conn, stmt, "complaint", "duplicate_of_complaint_id",
                    "VARCHAR(50) NULL");

            // Officer Module: additive, backward-compatible column for per-history officer attribution
            logger.info("Ensuring complaint_history.officer_id column exists...");
            addColumnIfMissing(conn, stmt, "complaint_history", "officer_id",
                    "BIGINT NULL");

            // AI Module (Complaint Registration): additive columns for AI-assisted priority
            // detection and photo-vs-description mismatch flagging. Advisory only - does not
            // change complaintType or any existing status/duplicate logic.
            logger.info("Ensuring AI-assist columns exist on complaint...");
            addColumnIfMissing(conn, stmt, "complaint", "ai_priority", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, stmt, "complaint", "ai_photo_mismatch_suspected", "BOOLEAN NULL DEFAULT FALSE");
            addColumnIfMissing(conn, stmt, "complaint", "ai_photo_mismatch_reason", "VARCHAR(500) NULL");

            // ---------- feedback table: MODIFY COLUMN statements ----------
            logger.info("Altering feedback table columns...");
            runStatement(stmt, "ALTER TABLE feedback MODIFY COLUMN image_url LONGTEXT",
                    "feedback.image_url altered to LONGTEXT");
            runStatement(stmt, "ALTER TABLE feedback MODIFY COLUMN video_url LONGTEXT",
                    "feedback.video_url altered to LONGTEXT");

            // ---------- reopen_request table: MODIFY COLUMN statements ----------
            logger.info("Altering reopen_request table columns...");
            runStatement(stmt, "ALTER TABLE reopen_request MODIFY COLUMN image_url LONGTEXT",
                    "reopen_request.image_url altered to LONGTEXT");
            runStatement(stmt, "ALTER TABLE reopen_request MODIFY COLUMN video_url LONGTEXT",
                    "reopen_request.video_url altered to LONGTEXT");

            // ---------- escalation table: ADD COLUMN (version-safe) ----------
            logger.info("Ensuring escalation detail columns exist...");
            addColumnIfMissing(conn, stmt, "escalation", "remarks", "TEXT NULL");
            addColumnIfMissing(conn, stmt, "escalation", "target", "VARCHAR(150) NULL");
            addColumnIfMissing(conn, stmt, "escalation", "status", "VARCHAR(30) NOT NULL DEFAULT 'PENDING'");

            // ---------- notifications table: CREATE TABLE IF NOT EXISTS ----------
            logger.info("Ensuring notifications table exists...");
            runStatement(stmt,
                    "CREATE TABLE IF NOT EXISTS notifications (" +
                            "notification_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "citizen_id BIGINT NOT NULL, " +
                            "complaint_id VARCHAR(50) NULL, " +
                            "title VARCHAR(200) NOT NULL, " +
                            "message TEXT NOT NULL, " +
                            "type VARCHAR(50) NOT NULL, " +
                            "is_read BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "CONSTRAINT fk_notifications_citizen FOREIGN KEY (citizen_id) REFERENCES citizen(citizen_id) ON DELETE CASCADE, " +
                            "CONSTRAINT fk_notifications_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id) ON DELETE SET NULL, " +
                            "INDEX idx_notifications_citizen (citizen_id), " +
                            "INDEX idx_notifications_is_read (is_read), " +
                            "INDEX idx_notifications_created_at (created_at)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
                    "notifications table created or already present");

            logger.info("===== DATABASE SCHEMA MIGRATION COMPLETED =====");

        } catch (Exception e) {
            // Only reached if getting the connection itself fails
            logger.error("Schema migration could not start: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs a single ALTER/MODIFY statement. Logs success or failure
     * without stopping the rest of the migration.
     */
    private void runStatement(Statement stmt, String sql, String successMessage) {
        try {
            stmt.execute(sql);
            logger.info("\u2713 {}", successMessage);
        } catch (Exception e) {
            logger.warn("Skipped [{}]: {}", successMessage, e.getMessage());
        }
    }

    /**
     * Adds a column only if it does not already exist, using JDBC
     * DatabaseMetaData instead of "IF NOT EXISTS" syntax (which needs
     * MySQL 8.0.29+ and fails on older servers). Safe on any MySQL version.
     */
    private void addColumnIfMissing(Connection conn, Statement stmt,
                                    String table, String column, String definition) {
        try {
            if (columnExists(conn, table, column)) {
                logger.info("\u2713 {}.{} already exists, skipping", table, column);
                return;
            }
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            logger.info("\u2713 {}.{} added", table, column);
        } catch (Exception e) {
            logger.warn("Skipped adding {}.{}: {}", table, column, e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
}
