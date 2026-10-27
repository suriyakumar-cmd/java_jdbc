# Kongu Engineering College (Autonomous), Perundurai - 638060
## Student Academic & Continuous Evaluation System (Regulation 2024 - R2024)
### Database-Driven Java JDBC Application

---

## 📌 Project Overview
This project implements a complete, database-driven Java application utilizing **Java Database Connectivity (JDBC)** to manage student records, execute **incremental marks entry ("Then and Now")**, enforce **attendance eligibility rules**, and generate formal academic performance reports strictly conforming to **Kongu Engineering College (Autonomous) - Regulation 2024 (KEC R2024)**.

---

## 🏛️ Regulation 2024 (KEC R2024) Rules & Weightage Engine

### 1. Course Type Weightage & Passing Criteria
| Course Type | CIA Weightage | ESE Weightage | ESE Minimum Threshold | Overall Pass Minimum |
| :--- | :---: | :---: | :---: | :---: |
| **THEORY** | **40%** | **60%** | **45% (27 / 60 scaled)** | **50 Marks** |
| **PRACTICAL** | **60%** | **40%** | **50% (20 / 40 scaled)** | **50 Marks** |
| **EMBEDDED** | **50%** | **50%** | **45% (22.5 / 50 scaled)**| **50 Marks** |

### 2. Continuous Internal Assessment (CIA) Formula
Continuous Internal Assessment (out of 100) comprises:
$$\text{CIA}_{100} = (\text{CIA Test 1} \times 0.40) + (\text{CIA Test 2} \times 0.40) + (\text{Assignment / Quiz / Lab} \times 0.20)$$

Scaled according to course type:
- **THEORY**: $\text{CIA}_{\text{scaled}} = (\text{CIA}_{100} / 100) \times 40$
- **PRACTICAL**: $\text{CIA}_{\text{scaled}} = (\text{CIA}_{100} / 100) \times 60$
- **EMBEDDED**: $\text{CIA}_{\text{scaled}} = (\text{CIA}_{100} / 100) \times 50$

### 3. Attendance Eligibility Gate
$$\text{Attendance \%} = \left(\frac{\text{Classes Attended}}{\text{Classes Held}}\right) \times 100$$
- **$\ge$ 75.0%**: Eligible to appear in End Semester Examinations (ESE).
- **65.0% to 74.9%**: Conditionally eligible. Requires **Medical Condonation** approval. If not approved, student is barred from ESE and awarded grade **SA**.
- **< 65.0%**: Shortage of Attendance. Barred from ESE. Automatically awarded grade **SA** (0 Grade Points).

### 4. 10-Point Letter Grading System
| Total Marks Range | Letter Grade | Grade Points | Description |
| :---: | :---: | :---: | :--- |
| **91 – 100** | **O** | **10** | Outstanding |
| **81 – 90** | **A+** | **9** | Excellent |
| **71 – 80** | **A** | **8** | Very Good |
| **61 – 70** | **B+** | **7** | Good |
| **50 – 60** | **B** | **6** | Above Average |
| **< 50 or Failed ESE Min** | **RA** | **0** | Re-Appearance / Arrear |
| **Attendance < 65%** | **SA** | **0** | Shortage of Attendance |
| **Absent in ESE** | **AB** | **0** | Absent in End Semester Examination |

### 5. SGPA & Percentage Formulas
$$\text{SGPA} = \frac{\sum (\text{Course Credits} \times \text{Grade Points Earned})}{\sum \text{Total Enrolled Credits}}$$

$$\text{Equivalent Percentage} = \text{SGPA} \times 10$$

---

## 🗄️ Database Schema (`kec_schema.sql`)

```
+------------------------------------+          +------------------------------------+
|             students               |          |              courses               |
+------------------------------------+          +------------------------------------+
| roll_number (PK, VARCHAR)          |          | course_code (PK, VARCHAR)          |
| name (VARCHAR)                     |          | course_title (VARCHAR)             |
| department (VARCHAR)               |          | credits (INT)                      |
| current_semester (INT)             |          | course_type (ENUM THEORY/PRAC/EMB) |
| created_at (TIMESTAMP)             |          | semester (INT)                     |
+------------------------------------+          +------------------------------------+
                  |                                               |
                  +-----------------------+   +-------------------+
                                          |   |
                                          v   v
                                +------------------------------------+
                                |            enrollments             |
                                +------------------------------------+
                                | enrollment_id (PK, AUTO_INCREMENT) |
                                | roll_number (FK -> students)       |
                                | course_code (FK -> courses)        |
                                | semester (INT)                     |
                                | classes_held (INT)                 |
                                | classes_attended (INT)             |
                                | medical_condonation (BOOLEAN)      |
                                +------------------------------------+
                                                  |
                                                  v
                                +------------------------------------+
                                |          assessment_marks          |
                                +------------------------------------+
                                | mark_id (PK, AUTO_INCREMENT)       |
                                | enrollment_id (FK -> enrollments)  |
                                | cia_test1 (DECIMAL 0-100) [Phase 1]|
                                | cia_test2 (DECIMAL 0-100) [Phase 1]|
                                | assignment_marks (DECIMAL) [Phase2]|
                                | ese_marks (DECIMAL 0-100) [Phase 3]|
                                | is_absent_ese (BOOLEAN)   [Phase 3]|
                                +------------------------------------+
```

---

## 📁 Project Directory Structure
```
c:\Users\Suriya\Downloads\KECEval\
├── lib\
│   └── mysql-connector-j-8.3.0.jar        # Official MySQL JDBC Driver
├── kec_schema.sql                          # MySQL Database DDL & Seed Data
├── db.properties                           # Database Connection Configuration
├── DBConnection.java                       # JDBC Connection Factory & Manager
├── Student.java                            # Student Domain Entity
├── Course.java                             # Course Domain Entity
├── Enrollment.java                         # Enrollment & Attendance Entity
├── AssessmentMark.java                     # Incremental Marks Entity
├── EvaluationResult.java                   # KEC R2024 Evaluation Engine
├── StudentDAO.java                         # Student, Course & Enrollment DAO
├── AcademicDAO.java                        # Phased Assessment & Reporting DAO
├── MainApp.java                            # Menu-Driven Console Interface
├── SystemVerificationTest.java             # 25-Point Comprehensive Test Suite
├── EXECUTION_DOCUMENTATION.md             # Execution Proofs and Reports
└── README.md                               # Project Guide & Documentation
```

---

## 🚀 How to Run the Application

### Prerequisites
- **JDK 17 or 21+** installed (`javac -version`, `java -version`)
- **MySQL / MariaDB** running on `localhost:3306` (XAMPP MySQL is supported natively)

### Step 1: Initialize Database
```powershell
# In PowerShell:
Get-Content kec_schema.sql | C:\xampp\mysql\bin\mysql.exe -u root kec_eval
# Or in Windows CMD:
mysql -u root -e "CREATE DATABASE IF NOT EXISTS kec_eval;"
mysql -u root kec_eval < kec_schema.sql
```

### Step 2: Compile All Java Files
```powershell
javac -cp ".;lib/*" *.java
```

### Step 3: Run Interactive Application
```powershell
java -cp ".;lib/*" MainApp
```

### Step 4: Run Automated Demonstration & Verification
```powershell
# Run the complete end-to-end automated demonstration
java -cp ".;lib/*" MainApp --demo

# Run the 25-point comprehensive verification test suite
java -cp ".;lib/*" SystemVerificationTest

# Generate specific reports from CLI:
java -cp ".;lib/*" MainApp --marksheet 24ITR001 3
java -cp ".;lib/*" MainApp --defaulters 3 IT
java -cp ".;lib/*" MainApp --summary IT 3
```

---

## 📊 Sample Report Outputs

### 1. Student Semester Mark Sheet (High Achiever - Grade O, A+)
```
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS                                      
                                STUDENT ACADEMIC & CONTINUOUS EVALUATION GRADE SHEET                                
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Roll Number   : 24ITR001                  Student Name : Aravind K                     
 Department    : IT                        Semester     : 3
 Academic Year : 2024-2025                 Regulation   : R2024
--------------------------------------------------------------------------------------------------------------------
 Code     Course Title                     Type      Cred Attd%  CIA1  CIA2  Assgn  CIA(Scl) ESE(Raw) ESE(Scl) Total  Grade GP  Status      
--------------------------------------------------------------------------------------------------------------------
 24ITP31  Advanced Java Programming Lab    PRACTICAL 2     93.3% 98.0  95.0  97.0    58.0/60 95.0     38.0      96.0  O     10  PASS        
 24ITR31  Data Structures & Algorithms     EMBEDDED  4     93.3% 90.0  88.0  92.0    44.8/50 89.0     44.5      89.3  A+    9   PASS        
 24ITT31  Database Management Systems      THEORY    3     96.0% 95.0  92.0  96.0    37.6/40 94.0     56.4      94.0  O     10  PASS        
--------------------------------------------------------------------------------------------------------------------
 Total Enrolled Credits : 9               Total Credits Earned  : 9              
 SGPA Earned            : 9.56            Equivalent Percentage: 95.56%
 Overall Result Status  : FIRST CLASS WITH DISTINCTION
====================================================================================================================
```

### 2. Attendance Defaulter Report (Grade SA < 65%)
```
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                         ATTENDANCE DEFAULTER REPORT (< 65%)                                        
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Filter: Department = IT              | Semester = 3
--------------------------------------------------------------------------------------------------------------------
 Roll No    Student Name              Dept   Course   Held  Attd  Attd%   Eligibility / Reason           Awarded Grade
--------------------------------------------------------------------------------------------------------------------
 24ITR004   Gowtham V                 IT     24ITP31  30    16     53.3% SHORTAGE (<65%) - BARRED       SA (0 GP)   
 24ITR004   Gowtham V                 IT     24ITR31  60    34     56.7% SHORTAGE (<65%) - BARRED       SA (0 GP)   
 24ITR004   Gowtham V                 IT     24ITT31  50    29     58.0% SHORTAGE (<65%) - BARRED       SA (0 GP)   
--------------------------------------------------------------------------------------------------------------------
 Total Defaulter Courses Flagged: 3 (All barred from taking ESE, Grade awarded: SA)
====================================================================================================================
```

### 3. Class Performance & Result Summary
```
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                         CLASS PERFORMANCE & RESULT SUMMARY                                         
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Department : IT                        Semester : 3
 Total Students Evaluated : 6               Total Passed / All Clear : 4              
 Total Failed / Arrears   : 2               Overall Pass Percentage  : 66.67%
 Breakup: RA = 0 | SA = 1 | AB = 1       Class Average SGPA      : 5.54 (55.37%)
--------------------------------------------------------------------------------------------------------------------
 Roll No    Student Name              Dept     SGPA     Percentage Result Status
--------------------------------------------------------------------------------------------------------------------
 24ITR001   Aravind K                 IT       9.56     95.56     % ALL CLEAR   
 24ITR002   Bhavana S                 IT       7.78     77.78     % ALL CLEAR   
 24ITR003   Dinesh Kumar M            IT       6.89     68.89     % ALL CLEAR   
 24ITR004   Gowtham V                 IT       0.00     0.00      % FAIL (SA)   
 24ITR005   Harini R                  IT       0.00     0.00      % FAIL (AB)   
 24ITR099   Suriya K                  IT       9.00     90.00     % ALL CLEAR   
====================================================================================================================
```

