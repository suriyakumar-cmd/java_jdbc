# Kongu Engineering College (Autonomous), Perundurai - 638060
# Regulation 2024 (R2024) - Academic & Continuous Evaluation System
## Formal Execution Documentation & Verification Proofs

---

## 📋 Document Control
- **Institution:** Kongu Engineering College (Autonomous), Perundurai, Erode - 638060
- **Academic Regulation:** Regulation 2024 (R2024)
- **Module:** Student Academic & Continuous Evaluation System
- **Implementation:** Java Standard Edition (JDK 21) & JDBC (MySQL MariaDB)
- **Database Engine:** MySQL InnoDB (`kec_eval`)
- **Author:** Department of Information Technology / Advanced Software Development

---

## 1. System Requirements & Specification Mapping

| Requirement / Module | Implementation Artifact | Verification Status |
| :--- | :--- | :---: |
| **Student Registration** | `StudentDAO.java` (`addStudent`, `getStudentByRoll`) | **VERIFIED (PASS)** |
| **Course Catalog (THEORY, PRACTICAL, EMBEDDED)** | `StudentDAO.java` (`addCourse`, `getCourseByCode`) | **VERIFIED (PASS)** |
| **Enrollment & Attendance Tracking** | `StudentDAO.java` (`enrollStudent`, `updateAttendance`) | **VERIFIED (PASS)** |
| **Attendance Eligibility (>=75%, 65-74.9%, <65%)** | `Enrollment.java` (`isEligibleForEse`), `AcademicDAO.java` | **VERIFIED (PASS)** |
| **Medical Condonation Workflow** | `StudentDAO.java` (`setMedicalCondonation`) | **VERIFIED (PASS)** |
| **Phase 1: Mid-Sem CIA Marks (0-100)** | `AcademicDAO.java` (`recordPhase1Marks`) | **VERIFIED (PASS)** |
| **Phase 2: End-Sem Continuous Assessment (0-100)**| `AcademicDAO.java` (`recordPhase2Marks`) | **VERIFIED (PASS)** |
| **Phase 3: Post-Sem ESE Marks (0-100) & AB** | `AcademicDAO.java` (`recordPhase3Marks`) | **VERIFIED (PASS)** |
| **KEC R2024 Course Weightage Engine** | `EvaluationResult.java` (`evaluate`) | **VERIFIED (PASS)** |
| **ESE Minimum Threshold & Aggregate 50 Min** | `EvaluationResult.java` (`eseMinRequiredRaw`) | **VERIFIED (PASS)** |
| **10-Point Grading System (O, A+, A, B+, B, RA, SA, AB)**| `EvaluationResult.java` (`letterGrade`, `gradePoint`) | **VERIFIED (PASS)** |
| **SGPA & Equivalent Percentage Engine** | `AcademicDAO.java` (`calculateSGPA`, `calculatePercentage`)| **VERIFIED (PASS)** |
| **Report 1: Student Semester Mark Sheet** | `MainApp.java` (`printStudentMarkSheet`) | **VERIFIED (PASS)** |
| **Report 2: Attendance Defaulter Report (Grade SA)**| `MainApp.java` (`printAttendanceDefaulters`) | **VERIFIED (PASS)** |
| **Report 3: Class Performance & Result Summary** | `MainApp.java` (`printClassPerformanceSummary`) | **VERIFIED (PASS)** |

---

## 2. Execution Proof 1: Adding a Student and Course

### Terminal Input & Output Trace:
```text
[DEMO STEP 1] Adding a new student: 24ITR099 - 'Suriya K' (IT Dept, Sem 3)...
 -> Successfully added student: 24ITR099     | Suriya K                  | IT       | Sem: 3

[DEMO STEP 2] Adding a new course: 24ITT39 - 'Cloud Computing Architecture' (THEORY, 3 Credits)...
 -> Successfully added course: 24ITT39    | Cloud Computing Architecture        | Credits: 3 | Type: THEORY     | Sem: 3

[DEMO STEP 3] Enrolling 24ITR099 into 24ITT39 (Semester 3)...
 -> Successfully enrolled! Generated Enrollment ID: 18

[DEMO STEP 4] Recording attendance: 45 classes attended out of 50 classes held (90.0%)...
 -> Attendance updated: 90.0% - Status: ELIGIBLE (>= 75%)
```

### Database Verification Query:
```sql
SELECT * FROM students WHERE roll_number = '24ITR099';
+-------------+----------+------------+------------------+---------------------+
| roll_number | name     | department | current_semester | created_at          |
+-------------+----------+------------+------------------+---------------------+
| 24ITR099    | Suriya K | IT         |                3 | 2026-10-22 22:11:42 |
+-------------+----------+------------+------------------+---------------------+

SELECT * FROM courses WHERE course_code = '24ITT39';
+-------------+------------------------------+---------+-------------+------------+----------+
| course_code | course_title                 | credits | course_type | department | semester |
+-------------+------------------------------+---------+-------------+------------+----------+
| 24ITT39     | Cloud Computing Architecture |       3 | THEORY      | IT         |        3 |
+-------------+------------------------------+---------+-------------+------------+----------+

SELECT enrollment_id, roll_number, course_code, semester, classes_held, classes_attended, medical_condonation 
FROM enrollments WHERE roll_number = '24ITR099';
+---------------+-------------+-------------+----------+--------------+------------------+---------------------+
| enrollment_id | roll_number | course_code | semester | classes_held | classes_attended | medical_condonation |
+---------------+-------------+-------------+----------+--------------+------------------+---------------------+
|            18 | 24ITR099    | 24ITT39     |        3 |           50 |               45 |                   0 |
+---------------+-------------+-------------+----------+--------------+------------------+---------------------+
```

---

## 3. Execution Proof 2: Incremental Marks Entry ("Then and Now")

The system enables progressive phased marks recording throughout the semester:

### Phase 1 (Mid-Semester): CIA Test 1 and CIA Test 2
- **Command:** `recordPhase1Marks(enrollmentId=18, cia1=92.0, cia2=88.0)`
- **Console Proof:**
  ```text
  [DEMO STEP 5A] Phase 1 (Mid-Semester): Recording CIA Test 1 = 92.0, CIA Test 2 = 88.0...
   -> Phase 1 recorded successfully!
  ```

### Phase 2 (End of Semester - CIA): Continuous Assessment
- **Command:** `recordPhase2Marks(enrollmentId=18, assignmentMarks=95.0)`
- **Console Proof:**
  ```text
  [DEMO STEP 5B] Phase 2 (End-Semester CIA): Recording Assignment / Quiz Marks = 95.0...
   -> Phase 2 recorded successfully!
  ```

### Phase 3 (Post Semester): End Semester Examination (ESE)
- **Gate Check:** Student attendance is verified before permitting entry. For 24ITR099, attendance is 90% ($\ge$ 75%), thus eligible.
- **Command:** `recordPhase3Marks(enrollmentId=18, eseMarks=90.0, isAbsent=false)`
- **Console Proof:**
  ```text
  [DEMO STEP 5C] Phase 3 (Post-Semester): Recording End Semester Exam Marks (ESE) = 90.0...
   -> Phase 3 recorded successfully!
  ```

### Assessment Marks Row in Database:
```sql
SELECT * FROM assessment_marks WHERE enrollment_id = 18;
+---------+---------------+-----------+-----------+------------------+-----------+---------------+
| mark_id | enrollment_id | cia_test1 | cia_test2 | assignment_marks | ese_marks | is_absent_ese |
+---------+---------------+-----------+-----------+------------------+-----------+---------------+
|      18 |            18 |     92.00 |     88.00 |            95.00 |     90.00 |             0 |
+---------+---------------+-----------+-----------+------------------+-----------+---------------+
```

---

## 4. Execution Proof 3: Attendance Shortage Barring Rule (< 65%)

For student **24ITR004 (Gowtham V)** in course `24ITT31`:
- Classes Held = 50, Attended = 29 $\implies$ Attendance = 58.0% (< 65%).
- When attempting Phase 3 ESE marks entry, the system raises an exception and bars entry:
```text
[DEMO STEP 6] Testing KEC R2024 Attendance Shortage Barring Rule (<65%)...
 -> Verifying 24ITR004 (Gowtham V) with 58.0% attendance in 24ITT31...
    Held: 50 | Attended: 29 | Attd %: 58.0% | Eligible for ESE: false
    Attempting to enter ESE marks for barred student...
    [EXPECTED BARRED BEHAVIOR TRIGGERED]: Attendance shortage (58.0% < 65%). Student is BARRED from ESE and awarded SA grade.
```

---

## 5. Execution Proof 4: Generated Grade Sheet & Calculated SGPA

### Report 1A: Outstanding High Achiever (`24ITR001` - Aravind K)
```text
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
 [Grading Key: O(10: 91-100), A+(9: 81-90), A(8: 71-80), B+(7: 61-70), B(6: 50-60), RA/SA/AB(0: Fail)]
 [Attendance Rules: >=75% Eligible | 65-74.9% Conditionally Eligible (Condonation) | <65% SA (Barred)]
====================================================================================================================
```

### SGPA Verification for 24ITR001:
- Course 1 (`24ITP31`): 2 Credits $\times$ 10 Grade Points = 20
- Course 2 (`24ITR31`): 4 Credits $\times$ 9 Grade Points = 36
- Course 3 (`24ITT31`): 3 Credits $\times$ 10 Grade Points = 30
- Total Enrolled Credits = $2 + 4 + 3 = 9$
- Total Earned Weighted Points = $20 + 36 + 30 = 86$
$$\text{SGPA} = \frac{86}{9} = 9.5555... \approx 9.56$$
$$\text{Equivalent Percentage} = 9.5555... \times 10 = 95.56\%$$

---

### Report 1B: Medical Condonation Approved Case (`24ITR003` - Dinesh Kumar M)
- Attendance in all courses is between 70.0% and 73.3% (within the 65% - 74.9% window).
- Medical condonation was submitted and approved (`medical_condonation = TRUE`), permitting student to write ESE.
```text
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS                                      
                                STUDENT ACADEMIC & CONTINUOUS EVALUATION GRADE SHEET                                
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Roll Number   : 24ITR003                  Student Name : Dinesh Kumar M                
 Department    : IT                        Semester     : 3
 Academic Year : 2024-2025                 Regulation   : R2024
--------------------------------------------------------------------------------------------------------------------
 Code     Course Title                     Type      Cred Attd%  CIA1  CIA2  Assgn  CIA(Scl) ESE(Raw) ESE(Scl) Total  Grade GP  Status      
--------------------------------------------------------------------------------------------------------------------
 24ITP31  Advanced Java Programming Lab    PRACTICAL 2     73.3% 75.0  70.0  78.0    44.2/60 72.0     28.8      73.0  A     8   PASS        
 24ITR31  Data Structures & Algorithms     EMBEDDED  4     71.7% 62.0  64.0  68.0    32.0/50 60.0     30.0      62.0  B+    7   PASS        
 24ITT31  Database Management Systems      THEORY    3     70.0% 65.0  60.0  70.0    25.6/40 58.0     34.8      60.4  B     6   PASS        
--------------------------------------------------------------------------------------------------------------------
 Total Enrolled Credits : 9               Total Credits Earned  : 9              
 SGPA Earned            : 6.89            Equivalent Percentage: 68.89%
 Overall Result Status  : FIRST CLASS
====================================================================================================================
```

---

### Report 1C: Attendance Shortage Barred Student (`24ITR004` - Gowtham V)
- Attendance < 65% in all courses. Barred from ESE. Grade awarded: **SA** (0 GP).
```text
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS                                      
                                STUDENT ACADEMIC & CONTINUOUS EVALUATION GRADE SHEET                                
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Roll Number   : 24ITR004                  Student Name : Gowtham V                     
 Department    : IT                        Semester     : 3
 Academic Year : 2024-2025                 Regulation   : R2024
--------------------------------------------------------------------------------------------------------------------
 Code     Course Title                     Type      Cred Attd%  CIA1  CIA2  Assgn  CIA(Scl) ESE(Raw) ESE(Scl) Total  Grade GP  Status      
--------------------------------------------------------------------------------------------------------------------
 24ITP31  Advanced Java Programming Lab    PRACTICAL 2     53.3% 60.0  58.0  65.0    36.1/60 -        -         36.1  SA    0   FAIL (SA)   
 24ITR31  Data Structures & Algorithms     EMBEDDED  4     56.7% 48.0  52.0  55.0    25.5/50 -        -         25.5  SA    0   FAIL (SA)   
 24ITT31  Database Management Systems      THEORY    3     58.0% 55.0  50.0  60.0    21.6/40 -        -         21.6  SA    0   FAIL (SA)   
--------------------------------------------------------------------------------------------------------------------
 Total Enrolled Credits : 9               Total Credits Earned  : 0              
 SGPA Earned            : 0.00            Equivalent Percentage: 0.00%
 Overall Result Status  : ARREAR / RE-APPEARANCE
====================================================================================================================
```

---

### Report 1D: Absent in ESE Case (`24ITR005` - Harini R)
- Good attendance (86.7% - 90.0%), but marked Absent (`AB`) for End Semester Examinations.
```text
====================================================================================================================
                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              
                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS                                      
                                STUDENT ACADEMIC & CONTINUOUS EVALUATION GRADE SHEET                                
                                           (REGULATION 2024 - B.E. / B.TECH)                                        
====================================================================================================================
 Roll Number   : 24ITR005                  Student Name : Harini R                      
 Department    : IT                        Semester     : 3
 Academic Year : 2024-2025                 Regulation   : R2024
--------------------------------------------------------------------------------------------------------------------
 Code     Course Title                     Type      Cred Attd%  CIA1  CIA2  Assgn  CIA(Scl) ESE(Raw) ESE(Scl) Total  Grade GP  Status      
--------------------------------------------------------------------------------------------------------------------
 24ITP31  Advanced Java Programming Lab    PRACTICAL 2     90.0% 85.0  88.0  90.0    52.3/60 AB       0.0       52.3  AB    0   FAIL (AB)   
 24ITR31  Data Structures & Algorithms     EMBEDDED  4     86.7% 78.0  81.0  80.0    39.8/50 AB       0.0       39.8  AB    0   FAIL (AB)   
 24ITT31  Database Management Systems      THEORY    3     90.0% 82.0  80.0  85.0    32.7/40 AB       0.0       32.7  AB    0   FAIL (AB)   
--------------------------------------------------------------------------------------------------------------------
 Total Enrolled Credits : 9               Total Credits Earned  : 0              
 SGPA Earned            : 0.00            Equivalent Percentage: 0.00%
 Overall Result Status  : ARREAR / RE-APPEARANCE
====================================================================================================================
```

---

## 6. Execution Proof 5: Attendance Defaulter Report

```text
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

---

## 7. Execution Proof 6: Class Performance & Result Summary

```text
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

---

## 8. Comprehensive Automated Test Suite (25 / 25 Passed)

```text
=======================================================================
   KEC R2024 EVALUATION SYSTEM - COMPREHENSIVE VERIFICATION TEST SUITE  
=======================================================================

--- Test Suite 1: JDBC Database Connectivity ---
  [PASS] JDBC Connection Test                          | Successfully connected to MySQL database 'kec_eval'

--- Test Suite 2: Student & Course Management ---
  [PASS] Student Registration                          | Student 24TEST01 registered and retrieved
  [PASS] Course Catalog Addition                       | Course 24TESTC added to catalog
  [PASS] Course Enrollment                             | Enrollment ID created: 19

--- Test Suite 3: Attendance Eligibility & Condonation Rules ---
  [PASS] Attendance >= 75%                             | 80% Attendance -> Eligible for ESE
  [PASS] Attendance 70% without condonation            | 70% Attendance without condonation -> Ineligible
  [PASS] Attendance 70% with condonation               | 70% Attendance with approved condonation -> Eligible
  [PASS] Attendance < 65% Barred                       | 60% Attendance -> Barred from ESE (Award SA)

--- Test Suite 4: Incremental Marks Entry ("Then and Now") ---
  [PASS] Phase 1: CIA 1 & CIA 2 Entry                  | CIA1 = 85.0, CIA2 = 80.0 recorded
  [PASS] Phase 2: Assignment/Quiz Entry                | Assignment Marks = 90.0 recorded
  [PASS] Phase 3: ESE Marks Entry                      | ESE Marks = 82.0 recorded
  [PASS] Phased Marks Verification                     | All 3 incremental phases complete in database

--- Test Suite 5: Theory Weightage & ESE Minimum Threshold (45% / 27) ---
  [PASS] Theory 100% Mark Evaluation                   | CIA = 40/40, ESE = 60/60, Total = 100, Grade = O
  [PASS] Theory Failed ESE Threshold Rule              | Raw ESE 40 < 45 -> Grade RA (0 GP), Status: FAIL (ESE MIN)
  [PASS] Theory Failed Aggregate Minimum (< 50)        | Total 35 < 50 -> Grade RA (0 GP), Status: FAIL (AGGREGATE)

--- Test Suite 6: Practical Weightage & ESE Minimum Threshold (50% / 20) ---
  [PASS] Practical Met ESE Minimum (50%)               | CIA = 60/60, ESE = 20/40, Total = 80, Grade = A
  [PASS] Practical Failed ESE Threshold Rule           | Raw ESE 48 < 50 -> Grade RA (0 GP), Status: FAIL (ESE MIN)

--- Test Suite 7: Embedded Course Weightage (50% CIA / 50% ESE) ---
  [PASS] Embedded 50-50 Weightage                      | CIA = 40/50, ESE = 40/50, Total = 80, Grade = A
  [PASS] Embedded Failed ESE Minimum (< 45)            | Raw ESE 44 < 45 -> Grade RA (0 GP)

--- Test Suite 8: Attendance Shortage Barring & SA Grade ---
  [PASS] Attendance Shortage SA Grade                  | Attendance 58% < 65% -> Barred from ESE, Grade: SA, GP: 0

--- Test Suite 9: Absent in ESE (AB) Rule ---
  [PASS] Absent in ESE AB Grade                        | Absent marked -> Grade: AB, GP: 0, Status: FAIL (AB)

--- Test Suite 10: SGPA & Equivalent Percentage Formulas ---
  [PASS] SGPA Calculation Formula                      | Calculated SGPA: 9.56
  [PASS] Equivalent Percentage Formula                 | Calculated Percentage: 95.56%

--- Test Suite 11: Formal Academic Reports Query Verification ---
  [PASS] Defaulter Report Query                        | Defaulters identified: 3 courses flagged with Grade SA
  [PASS] Class Performance Summary Query               | Total Students: 6 | Passed: 4 | Pass %: 66.67%

=======================================================================
   TEST EXECUTION SUMMARY: 25 / 25 TESTS PASSED (100% SUCCESS) 
=======================================================================
```

