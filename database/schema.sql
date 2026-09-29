-- =============================================================================
-- Smart Governance Platform for Administrative Operations with Citizen Service Assistance
-- Database Initialization Script
-- Database: finally_civicpulse_db
-- =============================================================================

CREATE DATABASE IF NOT EXISTS finally_civicpulse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE finally_civicpulse_db;

-- -----------------------------------------------------------------------------
-- Table: citizen
-- Description: Stores registered citizen profiles for public service assistance
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS citizen (
    citizen_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    aadhaar_number VARCHAR(12) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_citizen_aadhaar UNIQUE (aadhaar_number),
    CONSTRAINT uq_citizen_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Table: officer
-- Description: Stores administrative officers provisioned by administration
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS officer (
    officer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_officer_employee_id UNIQUE (employee_id),
    CONSTRAINT uq_officer_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_department_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS complaint (
    complaint_id VARCHAR(50) PRIMARY KEY,
    citizen_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    officer_id BIGINT NULL,
    complaint_type ENUM('NORMAL', 'EMERGENCY') NOT NULL DEFAULT 'NORMAL',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    image_url LONGTEXT,
    video_url LONGTEXT,
    latitude DOUBLE,
    longitude DOUBLE,
    location VARCHAR(255) NOT NULL,
    status ENUM('APPLIED', 'COMPLAINT_VIEWED', 'ACTION_TO_BE_TAKEN', 'COMPLAINT_RESOLVED', 'REOPENED', 'DUPLICATE', 'REJECTED') NOT NULL DEFAULT 'APPLIED',
    duplicate_status ENUM('PENDING_REVIEW', 'DUPLICATE', 'NOT_DUPLICATE') NOT NULL DEFAULT 'PENDING_REVIEW',
    action_taken TEXT,
    duplicate_of_complaint_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_complaint_citizen FOREIGN KEY (citizen_id) REFERENCES citizen(citizen_id),
    CONSTRAINT fk_complaint_department FOREIGN KEY (department_id) REFERENCES department(department_id),
    CONSTRAINT fk_complaint_officer FOREIGN KEY (officer_id) REFERENCES officer(officer_id),
    CONSTRAINT fk_complaint_duplicate_of FOREIGN KEY (duplicate_of_complaint_id) REFERENCES complaint(complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS complaint_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(50) NOT NULL,
    status ENUM('APPLIED', 'COMPLAINT_VIEWED', 'ACTION_TO_BE_TAKEN', 'COMPLAINT_RESOLVED', 'REOPENED', 'DUPLICATE') NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(50) NOT NULL,
    citizen_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    image_url LONGTEXT,
    video_url LONGTEXT,
    latitude DOUBLE,
    longitude DOUBLE,
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id),
    CONSTRAINT fk_feedback_citizen FOREIGN KEY (citizen_id) REFERENCES citizen(citizen_id),
    CONSTRAINT uk_feedback_complaint UNIQUE (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reopen_request (
    reopen_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(50) NOT NULL,
    citizen_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    image_url LONGTEXT,
    video_url LONGTEXT,
    latitude DOUBLE,
    longitude DOUBLE,
    location VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reopen_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id),
    CONSTRAINT fk_reopen_citizen FOREIGN KEY (citizen_id) REFERENCES citizen(citizen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS escalation (
    escalation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(50) NOT NULL,
    escalated_by_officer_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    escalation_level INT NOT NULL DEFAULT 1,
    remarks TEXT,
    target VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_escalation_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id),
    CONSTRAINT fk_escalation_officer FOREIGN KEY (escalated_by_officer_id) REFERENCES officer(officer_id),
    INDEX idx_escalation_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    citizen_id BIGINT NOT NULL,
    complaint_id VARCHAR(50) NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_citizen FOREIGN KEY (citizen_id) REFERENCES citizen(citizen_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id) ON DELETE SET NULL,
    INDEX idx_notifications_citizen (citizen_id),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Seed Initial Development Officer Account (BCrypt Hash for 'Officer@123')
-- Note: The Spring Boot application also automatically creates this record on startup
-- if it does not already exist.
-- -----------------------------------------------------------------------------
INSERT INTO officer (full_name, employee_id, department, email, phone_number, password, created_at)
SELECT 'Test Officer', 'EMP1001', 'Sanitation Department', 'officer@smartgov.local', '9876543210', '$2a$10$w8T0M4j6lA75fEkm2h52PecU.gq0qBqP1W8fI6Bw0vXk3Y9Z7J6l.', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM officer WHERE employee_id = 'EMP1001'
);

INSERT INTO department (name, description, created_at)
SELECT 'Sanitation Department', 'Auto seeded governance department', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Sanitation Department');

INSERT INTO department (name, description, created_at)
SELECT 'Water Supply Department', 'Auto seeded governance department', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Water Supply Department');

INSERT INTO department (name, description, created_at)
SELECT 'Road Maintenance Department', 'Auto seeded governance department', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Road Maintenance Department');

INSERT INTO department (name, description, created_at)
SELECT 'Public Health Department', 'Auto seeded governance department', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Public Health Department');

INSERT INTO department (name, description, created_at)
SELECT 'Electricity Department', 'Auto seeded governance department', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = 'Electricity Department');
