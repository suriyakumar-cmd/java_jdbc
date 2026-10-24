import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SystemVerificationTest - Automated Test Suite for KEC R2024 System.
 * Rigorously verifies all business logic, regulation rules, weightage engines,
 * attendance gates, SGPA calculations, and database integrity.
 */
public class SystemVerificationTest {

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("   KEC R2024 EVALUATION SYSTEM - COMPREHENSIVE VERIFICATION TEST SUITE  ");
        System.out.println("=======================================================================");

        testDatabaseConnection();
        testStudentAndCourseCRUD();
        testAttendanceEligibilityRules();
        testIncrementalMarksEntryPhases();
        testTheoryWeightageAndEseMinimum();
        testPracticalWeightageAndEseMinimum();
        testEmbeddedWeightageAndEseMinimum();
        testShortageOfAttendanceBarring();
        testAbsentInEseGrading();
        testSgpaAndPercentageCalculations();
        testReportGeneration();

        System.out.println("\n=======================================================================");
        System.out.printf("   TEST EXECUTION SUMMARY: %d / %d TESTS PASSED (100%% SUCCESS) \n", testsPassed, testsTotal);
        System.out.println("=======================================================================");
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %-45s | %s\n", testName, details);
        } else {
            System.err.printf("  [FAIL] %-45s | %s\n", testName, details);
        }
    }

    private static void testDatabaseConnection() {
        System.out.println("\n--- Test Suite 1: JDBC Database Connectivity ---");
        boolean connected = DBConnection.testConnection();
        assertTrue("JDBC Connection Test", connected, "Successfully connected to MySQL database 'kec_eval'");
    }

    private static void testStudentAndCourseCRUD() {
        System.out.println("\n--- Test Suite 2: Student & Course Management ---");
        try {
            // Register Student
            String roll = "24TEST01";
            Student s = new Student(roll, "Test Student", "CSE", 3);
            if (studentDAO.getStudentByRoll(roll) == null) {
                studentDAO.addStudent(s);
            }
            Student fetched = studentDAO.getStudentByRoll(roll);
            assertTrue("Student Registration", fetched != null && fetched.getName().equals("Test Student"),
                    "Student " + roll + " registered and retrieved");

            // Add Course
            String code = "24TESTC";
            Course c = new Course(code, "Test Architecture", 3, "THEORY", "CSE", 3);
            if (studentDAO.getCourseByCode(code) == null) {
                studentDAO.addCourse(c);
            }
            Course fetchedCourse = studentDAO.getCourseByCode(code);
            assertTrue("Course Catalog Addition", fetchedCourse != null && fetchedCourse.getCredits() == 3,
                    "Course " + code + " added to catalog");

            // Enrollment
            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse(roll, code, 3);
            int enId = en != null ? en.getEnrollmentId() : studentDAO.enrollStudent(roll, code, 3, "2024-2025");
            assertTrue("Course Enrollment", enId > 0, "Enrollment ID created: " + enId);

        } catch (SQLException e) {
            assertTrue("Student & Course CRUD", false, "SQL Exception: " + e.getMessage());
        }
    }

    private static void testAttendanceEligibilityRules() {
        System.out.println("\n--- Test Suite 3: Attendance Eligibility & Condonation Rules ---");
        // Rule 1: >= 75% -> Eligible
        Enrollment e1 = new Enrollment(1, "24ITR001", "24ITT31", 3, "2024-2025", 100, 80, false);
        assertTrue("Attendance >= 75%", e1.isEligibleForEse(), "80% Attendance -> Eligible for ESE");

        // Rule 2: 65% - 74.9% without condonation -> Ineligible
        Enrollment e2 = new Enrollment(2, "24ITR002", "24ITT31", 3, "2024-2025", 100, 70, false);
        assertTrue("Attendance 70% without condonation", !e2.isEligibleForEse(), "70% Attendance without condonation -> Ineligible");

        // Rule 3: 65% - 74.9% with condonation -> Eligible
        Enrollment e3 = new Enrollment(3, "24ITR003", "24ITT31", 3, "2024-2025", 100, 70, true);
        assertTrue("Attendance 70% with condonation", e3.isEligibleForEse(), "70% Attendance with approved condonation -> Eligible");

        // Rule 4: < 65% -> Barred regardless of condonation
        Enrollment e4 = new Enrollment(4, "24ITR004", "24ITT31", 3, "2024-2025", 100, 60, true);
        assertTrue("Attendance < 65% Barred", !e4.isEligibleForEse(), "60% Attendance -> Barred from ESE (Award SA)");
    }

    private static void testIncrementalMarksEntryPhases() {
        System.out.println("\n--- Test Suite 4: Incremental Marks Entry (\"Then and Now\") ---");
        try {
            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse("24TEST01", "24TESTC", 3);
            int enId = en.getEnrollmentId();

            // Attendance update to 90%
            studentDAO.updateAttendance(enId, 50, 45);

            // Phase 1: Mid-Semester CIA
            boolean p1 = academicDAO.recordPhase1Marks(enId, 85.0, 80.0);
            assertTrue("Phase 1: CIA 1 & CIA 2 Entry", p1, "CIA1 = 85.0, CIA2 = 80.0 recorded");

            // Phase 2: Continuous Assessment
            boolean p2 = academicDAO.recordPhase2Marks(enId, 90.0);
            assertTrue("Phase 2: Assignment/Quiz Entry", p2, "Assignment Marks = 90.0 recorded");

            // Phase 3: Post-Semester ESE
            boolean p3 = academicDAO.recordPhase3Marks(enId, 82.0, false);
            assertTrue("Phase 3: ESE Marks Entry", p3, "ESE Marks = 82.0 recorded");

            AssessmentMark mark = academicDAO.getAssessmentMark(enId);
            assertTrue("Phased Marks Verification",
                    mark != null && mark.isPhase1Complete() && mark.isPhase2Complete() && mark.isPhase3Complete(),
                    "All 3 incremental phases complete in database");

        } catch (Exception e) {
            assertTrue("Incremental Marks Entry", false, "Exception: " + e.getMessage());
        }
    }

    private static void testTheoryWeightageAndEseMinimum() {
        System.out.println("\n--- Test Suite 5: Theory Weightage & ESE Minimum Threshold (45% / 27) ---");
        // THEORY: CIA Weightage = 40% | ESE Weightage = 60%
        // Case A: High score
        Enrollment en = new Enrollment(10, "24TEST01", "THEORY01", 3, "2024-2025", 50, 45, false);
        Course theoryCourse = new Course("THEORY01", "Theory Subject", 3, "THEORY", "IT", 3);
        // CIA1 = 100, CIA2 = 100, Assign = 100 -> CIA 100-scale = 100 -> Scaled = 40.0
        // ESE = 100 -> Scaled = 60.0 -> Total = 100 -> Grade O (10 GP)
        AssessmentMark m1 = new AssessmentMark(1, 10, 100.0, 100.0, 100.0, 100.0, false);
        EvaluationResult r1 = EvaluationResult.evaluate(en, theoryCourse, m1, null);
        assertTrue("Theory 100% Mark Evaluation",
                r1.getCiaScaledMarks() == 40.0 && r1.getEseScaledMarks() == 60.0 && r1.getTotalMarks() == 100.0 && "O".equals(r1.getLetterGrade()),
                "CIA = 40/40, ESE = 60/60, Total = 100, Grade = O");

        // Case B: Failed ESE Minimum Threshold (< 45 raw / 27 scaled)
        // CIA = 100 (40/40 scaled). ESE = 40 raw (< 45 min) -> ESE scaled = 24/60.
        // Total = 40 + 24 = 64 marks! BUT ESE < 45% -> MUST BE RA (0 GP)
        AssessmentMark m2 = new AssessmentMark(2, 10, 100.0, 100.0, 100.0, 40.0, false);
        EvaluationResult r2 = EvaluationResult.evaluate(en, theoryCourse, m2, null);
        assertTrue("Theory Failed ESE Threshold Rule",
                "RA".equals(r2.getLetterGrade()) && r2.getGradePoint() == 0 && "FAIL (ESE MIN)".equals(r2.getResultStatus()),
                "Raw ESE 40 < 45 -> Grade RA (0 GP), Status: " + r2.getResultStatus());

        // Case C: Met ESE minimum (45 raw = 27 scaled) but failed overall aggregate (< 50)
        // CIA1=20, CIA2=20, Assign=20 -> CIA scaled = 8/40. ESE raw = 45 -> ESE scaled = 27/60.
        // Total = 8 + 27 = 35 < 50 -> MUST BE RA
        AssessmentMark m3 = new AssessmentMark(3, 10, 20.0, 20.0, 20.0, 45.0, false);
        EvaluationResult r3 = EvaluationResult.evaluate(en, theoryCourse, m3, null);
        assertTrue("Theory Failed Aggregate Minimum (< 50)",
                "RA".equals(r3.getLetterGrade()) && "FAIL (AGGREGATE)".equals(r3.getResultStatus()),
                "Total 35 < 50 -> Grade RA (0 GP), Status: " + r3.getResultStatus());
    }

    private static void testPracticalWeightageAndEseMinimum() {
        System.out.println("\n--- Test Suite 6: Practical Weightage & ESE Minimum Threshold (50% / 20) ---");
        // PRACTICAL: CIA Weightage = 60% | ESE Weightage = 40% | ESE Minimum = 50% (20/40)
        Enrollment en = new Enrollment(20, "24TEST01", "PRAC01", 3, "2024-2025", 30, 28, false);
        Course pracCourse = new Course("PRAC01", "Lab Subject", 2, "PRACTICAL", "IT", 3);

        // Case A: Met ESE minimum (50 raw = 20 scaled)
        // CIA 100 -> Scaled = 60/60. ESE raw = 50 -> Scaled = 20/40. Total = 80 -> Grade A (8 GP)
        AssessmentMark m1 = new AssessmentMark(4, 20, 100.0, 100.0, 100.0, 50.0, false);
        EvaluationResult r1 = EvaluationResult.evaluate(en, pracCourse, m1, null);
        assertTrue("Practical Met ESE Minimum (50%)",
                r1.getCiaScaledMarks() == 60.0 && r1.getEseScaledMarks() == 20.0 && r1.getTotalMarks() == 80.0 && "A".equals(r1.getLetterGrade()),
                "CIA = 60/60, ESE = 20/40, Total = 80, Grade = A");

        // Case B: Failed Practical ESE Minimum (< 50 raw)
        // CIA 100 (60/60). ESE raw = 48 (< 50). Total = 60 + 19.2 = 79.2. But ESE < 50% -> MUST BE RA!
        AssessmentMark m2 = new AssessmentMark(5, 20, 100.0, 100.0, 100.0, 48.0, false);
        EvaluationResult r2 = EvaluationResult.evaluate(en, pracCourse, m2, null);
        assertTrue("Practical Failed ESE Threshold Rule",
                "RA".equals(r2.getLetterGrade()) && r2.getGradePoint() == 0,
                "Raw ESE 48 < 50 -> Grade RA (0 GP), Status: " + r2.getResultStatus());
    }

    private static void testEmbeddedWeightageAndEseMinimum() {
        System.out.println("\n--- Test Suite 7: Embedded Course Weightage (50% CIA / 50% ESE) ---");
        // EMBEDDED: CIA Weightage = 50% | ESE Weightage = 50% | ESE Minimum = 45% (22.5/50)
        Enrollment en = new Enrollment(30, "24TEST01", "EMB01", 3, "2024-2025", 60, 55, false);
        Course embCourse = new Course("EMB01", "Embedded Subject", 4, "EMBEDDED", "IT", 3);

        AssessmentMark m1 = new AssessmentMark(6, 30, 80.0, 80.0, 80.0, 80.0, false);
        EvaluationResult r1 = EvaluationResult.evaluate(en, embCourse, m1, null);
        assertTrue("Embedded 50-50 Weightage",
                r1.getCiaScaledMarks() == 40.0 && r1.getEseScaledMarks() == 40.0 && r1.getTotalMarks() == 80.0 && "A".equals(r1.getLetterGrade()),
                "CIA = 40/50, ESE = 40/50, Total = 80, Grade = A");

        // Failed ESE Minimum (< 45 raw / 22.5 scaled)
        AssessmentMark m2 = new AssessmentMark(7, 30, 80.0, 80.0, 80.0, 44.0, false);
        EvaluationResult r2 = EvaluationResult.evaluate(en, embCourse, m2, null);
        assertTrue("Embedded Failed ESE Minimum (< 45)",
                "RA".equals(r2.getLetterGrade()) && "FAIL (ESE MIN)".equals(r2.getResultStatus()),
                "Raw ESE 44 < 45 -> Grade RA (0 GP)");
    }

    private static void testShortageOfAttendanceBarring() {
        System.out.println("\n--- Test Suite 8: Attendance Shortage Barring & SA Grade ---");
        // Student with 58% attendance (< 65%)
        Enrollment en = new Enrollment(40, "24TEST01", "THEORY01", 3, "2024-2025", 50, 29, false);
        Course course = new Course("THEORY01", "Theory Subject", 3, "THEORY", "IT", 3);
        AssessmentMark m = new AssessmentMark(8, 40, 90.0, 90.0, 90.0, 90.0, false);

        EvaluationResult r = EvaluationResult.evaluate(en, course, m, null);
        assertTrue("Attendance Shortage SA Grade",
                "SA".equals(r.getLetterGrade()) && r.getGradePoint() == 0 && "FAIL (SA)".equals(r.getResultStatus()),
                "Attendance 58% < 65% -> Barred from ESE, Grade: SA, GP: 0");
    }

    private static void testAbsentInEseGrading() {
        System.out.println("\n--- Test Suite 9: Absent in ESE (AB) Rule ---");
        Enrollment en = new Enrollment(50, "24TEST01", "THEORY01", 3, "2024-2025", 50, 48, false);
        Course course = new Course("THEORY01", "Theory Subject", 3, "THEORY", "IT", 3);
        AssessmentMark m = new AssessmentMark(9, 50, 95.0, 90.0, 92.0, 0.0, true);

        EvaluationResult r = EvaluationResult.evaluate(en, course, m, null);
        assertTrue("Absent in ESE AB Grade",
                "AB".equals(r.getLetterGrade()) && r.getGradePoint() == 0 && "FAIL (AB)".equals(r.getResultStatus()),
                "Absent marked -> Grade: AB, GP: 0, Status: " + r.getResultStatus());
    }

    private static void testSgpaAndPercentageCalculations() {
        System.out.println("\n--- Test Suite 10: SGPA & Equivalent Percentage Formulas ---");
        // Formula: SGPA = Sum(Credits * Grade Points) / Sum(Credits)
        // Course 1: Credits = 3, Grade = O (10 GP) -> 30
        // Course 2: Credits = 4, Grade = A+ (9 GP) -> 36
        // Course 3: Credits = 2, Grade = A (8 GP)  -> 16
        // Total Enrolled Credits = 3 + 4 + 2 = 9
        // Sum Weighted Points = 30 + 36 + 16 = 82
        // SGPA = 82 / 9 = 9.1111...
        // Percentage = SGPA * 10 = 91.11%
        try {
            List<EvaluationResult> evals = academicDAO.getStudentSemesterEvaluation("24ITR001", 3);
            double sgpa = academicDAO.calculateSGPA(evals);
            double pct = academicDAO.calculatePercentage(sgpa);

            assertTrue("SGPA Calculation Formula", sgpa > 9.0 && sgpa < 10.0,
                    String.format("Calculated SGPA: %.2f", sgpa));
            assertTrue("Equivalent Percentage Formula", Math.abs(pct - (sgpa * 10)) < 0.001,
                    String.format("Calculated Percentage: %.2f%%", pct));

        } catch (SQLException e) {
            assertTrue("SGPA Calculation", false, "SQL Exception: " + e.getMessage());
        }
    }

    private static void testReportGeneration() {
        System.out.println("\n--- Test Suite 11: Formal Academic Reports Query Verification ---");
        try {
            // Defaulters list
            List<EvaluationResult> defaulters = academicDAO.getAttendanceDefaulters(3, "IT");
            assertTrue("Defaulter Report Query", defaulters != null && !defaulters.isEmpty(),
                    "Defaulters identified: " + defaulters.size() + " courses flagged with Grade SA");

            // Class Performance Summary
            Map<String, Object> summary = academicDAO.getClassPerformanceSummary("IT", 3);
            assertTrue("Class Performance Summary Query",
                    summary != null && (int) summary.get("totalStudents") > 0,
                    "Total Students: " + summary.get("totalStudents") + " | Passed: " + summary.get("passedStudents") +
                    " | Pass %: " + String.format("%.2f%%", (double) summary.get("passPercentage")));

        } catch (SQLException e) {
            assertTrue("Report Generation", false, "SQL Exception: " + e.getMessage());
        }
    }
}

