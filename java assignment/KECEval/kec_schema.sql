-- ============================================================================
-- Kongu Engineering College (Autonomous), Perundurai, Erode - 638060
-- Regulation 2024 (R2024) - Student Academic & Continuous Evaluation System
-- Database Schema Script: kec_schema.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS kec_eval;
USE kec_eval;

-- 1. Students Table
DROP TABLE IF EXISTS assessment_marks;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    roll_number VARCHAR(15) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    current_semester INT NOT NULL CHECK (current_semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Courses Table
CREATE TABLE courses (
    course_code VARCHAR(15) PRIMARY KEY,
    course_title VARCHAR(150) NOT NULL,
    credits INT NOT NULL CHECK (credits > 0),
    course_type ENUM('THEORY', 'PRACTICAL', 'EMBEDDED') NOT NULL,
    department VARCHAR(20) DEFAULT 'ALL',
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Enrollments & Attendance Tracking Table
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(15) NOT NULL,
    course_code VARCHAR(15) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    academic_year VARCHAR(15) DEFAULT '2024-2025',
    classes_held INT DEFAULT 0 CHECK (classes_held >= 0),
    classes_attended INT DEFAULT 0 CHECK (classes_attended >= 0),
    medical_condonation BOOLEAN DEFAULT FALSE,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_enrollment UNIQUE (roll_number, course_code, semester),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Incremental Assessment Marks Table
-- Phase 1: cia_test1 (0-100), cia_test2 (0-100)
-- Phase 2: assignment_marks (0-100)
-- Phase 3: ese_marks (0-100), is_absent_ese (BOOLEAN)
CREATE TABLE assessment_marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    cia_test1 DECIMAL(5,2) DEFAULT NULL CHECK (cia_test1 IS NULL OR (cia_test1 >= 0 AND cia_test1 <= 100)),
    cia_test2 DECIMAL(5,2) DEFAULT NULL CHECK (cia_test2 IS NULL OR (cia_test2 >= 0 AND cia_test2 <= 100)),
    assignment_marks DECIMAL(5,2) DEFAULT NULL CHECK (assignment_marks IS NULL OR (assignment_marks >= 0 AND assignment_marks <= 100)),
    ese_marks DECIMAL(5,2) DEFAULT NULL CHECK (ese_marks IS NULL OR (ese_marks >= 0 AND ese_marks <= 100)),
    is_absent_ese BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mark_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Sample Seed Data for Demonstration & Testing
-- ============================================================================

-- Insert Sample Students
INSERT INTO students (roll_number, name, department, current_semester) VALUES
('24ITR001', 'Aravind K', 'IT', 3),
('24ITR002', 'Bhavana S', 'IT', 3),
('24ITR003', 'Dinesh Kumar M', 'IT', 3),
('24ITR004', 'Gowtham V', 'IT', 3),
('24ITR005', 'Harini R', 'IT', 3),
('24CSR001', 'Kavya P', 'CSE', 3),
('24CSR002', 'Manoj S', 'CSE', 3);

-- Insert Sample Courses (THEORY, PRACTICAL, EMBEDDED)
INSERT INTO courses (course_code, course_title, credits, course_type, department, semester) VALUES
('24ITT31', 'Database Management Systems', 3, 'THEORY', 'IT', 3),
('24ITR31', 'Data Structures & Algorithms', 4, 'EMBEDDED', 'IT', 3),
('24ITP31', 'Advanced Java Programming Lab', 2, 'PRACTICAL', 'IT', 3),
('24CST32', 'Operating Systems', 3, 'THEORY', 'CSE', 3);

-- Enrollments with varied attendance scenarios:
-- 24ITR001: Excellent attendance (>= 75%)
-- 24ITR002: Normal attendance (>= 75%)
-- 24ITR003: 65% - 74.9% conditional attendance with Medical Condonation APPROVED
-- 24ITR004: < 65% Shortage of Attendance (SA defaulter)
-- 24ITR005: Eligible attendance, but Absent in ESE (AB)
INSERT INTO enrollments (roll_number, course_code, semester, academic_year, classes_held, classes_attended, medical_condonation) VALUES
-- Aravind K
('24ITR001', '24ITT31', 3, '2024-2025', 50, 48, FALSE), -- 96%
('24ITR001', '24ITR31', 3, '2024-2025', 60, 56, FALSE), -- 93.3%
('24ITR001', '24ITP31', 3, '2024-2025', 30, 28, FALSE), -- 93.3%

-- Bhavana S
('24ITR002', '24ITT31', 3, '2024-2025', 50, 42, FALSE), -- 84%
('24ITR002', '24ITR31', 3, '2024-2025', 60, 50, FALSE), -- 83.3%
('24ITR002', '24ITP31', 3, '2024-2025', 30, 25, FALSE), -- 83.3%

-- Dinesh Kumar M (Condonation case: 70% attendance, approved)
('24ITR003', '24ITT31', 3, '2024-2025', 50, 35, TRUE),  -- 70% with Condonation
('24ITR003', '24ITR31', 3, '2024-2025', 60, 43, TRUE),  -- 71.6% with Condonation
('24ITR003', '24ITP31', 3, '2024-2025', 30, 22, TRUE),  -- 73.3% with Condonation

-- Gowtham V (Defaulter case: 58% attendance < 65% -> SA)
('24ITR004', '24ITT31', 3, '2024-2025', 50, 29, FALSE), -- 58% -> SA
('24ITR004', '24ITR31', 3, '2024-2025', 60, 34, FALSE), -- 56.6% -> SA
('24ITR004', '24ITP31', 3, '2024-2025', 30, 16, FALSE), -- 53.3% -> SA

-- Harini R (Eligible attendance, but Absent in ESE)
('24ITR005', '24ITT31', 3, '2024-2025', 50, 45, FALSE), -- 90%
('24ITR005', '24ITR31', 3, '2024-2025', 60, 52, FALSE), -- 86.6%
('24ITR005', '24ITP31', 3, '2024-2025', 30, 27, FALSE), -- 90%

-- CSE Students
('24CSR001', '24CST32', 3, '2024-2025', 50, 47, FALSE),
('24CSR002', '24CST32', 3, '2024-2025', 50, 44, FALSE);

-- Assessment Marks (Populating Phases 1, 2, 3)
-- 24ITR001: High Achiever (Grade O and A+)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(1, 95.0, 92.0, 96.0, 94.0, FALSE),
(2, 90.0, 88.0, 92.0, 89.0, FALSE),
(3, 98.0, 95.0, 97.0, 95.0, FALSE);

-- 24ITR002: Regular Pass (Grade A and B+)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(4, 78.0, 75.0, 80.0, 72.0, FALSE),
(5, 70.0, 72.0, 75.0, 68.0, FALSE),
(6, 85.0, 82.0, 88.0, 80.0, FALSE);

-- 24ITR003: Condoned student who appeared and passed (Grade B / B+)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(7, 65.0, 60.0, 70.0, 58.0, FALSE),
(8, 62.0, 64.0, 68.0, 60.0, FALSE),
(9, 75.0, 70.0, 78.0, 72.0, FALSE);

-- 24ITR004: Attendance Defaulter (<65% -> Barred from ESE, SA grade)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(10, 55.0, 50.0, 60.0, NULL, FALSE),
(11, 48.0, 52.0, 55.0, NULL, FALSE),
(12, 60.0, 58.0, 65.0, NULL, FALSE);

-- 24ITR005: Absent in ESE (AB grade)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(13, 82.0, 80.0, 85.0, 0.0, TRUE),
(14, 78.0, 81.0, 80.0, 0.0, TRUE),
(15, 85.0, 88.0, 90.0, 0.0, TRUE);

-- CSE Students
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese) VALUES
(16, 88.0, 85.0, 90.0, 86.0, FALSE),
(17, 72.0, 75.0, 78.0, 70.0, FALSE);

