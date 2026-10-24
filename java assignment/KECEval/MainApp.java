import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * MainApp - Menu-driven interface and reporting engine for
 * Kongu Engineering College (R2024) Student Academic & Continuous Evaluation System.
 */
public class MainApp {

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0) {
            handleCliArguments(args);
            return;
        }

        printBanner();
        if (!DBConnection.testConnection()) {
            System.err.println("\n[ERROR] Unable to connect to MySQL database!");
            System.err.println("Please ensure MySQL is running on localhost:3306 and database 'kec_eval' exists.");
            System.err.println("Database URL: " + DBConnection.getUrl());
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter your choice (1-10): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleRegisterStudent();
                    break;
                case "2":
                    handleAddCourse();
                    break;
                case "3":
                    handleEnrollStudent();
                    break;
                case "4":
                    handleUpdateAttendance();
                    break;
                case "5":
                    handleMedicalCondonation();
                    break;
                case "6":
                    handleIncrementalMarksEntry();
                    break;
                case "7":
                    handleStudentMarkSheet();
                    break;
                case "8":
                    handleAttendanceDefaulterReport();
                    break;
                case "9":
                    handleClassPerformanceSummary();
                    break;
                case "10":
                    handleRunFullSystemDemo();
                    break;
                case "0":
                case "exit":
                case "q":
                    running = false;
                    System.out.println("\nThank you for using KEC R2024 Evaluation System. Exiting...");
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please enter a number between 1 and 10, or 0 to exit.");
            }
        }
    }

    private static void printBanner() {
        System.out.println("==================================================================================");
        System.out.println("            KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI - 638060          ");
        System.out.println("              STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM (R2024)             ");
        System.out.println("                    Java JDBC Database-Driven Implementation                      ");
        System.out.println("==================================================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n================================= MAIN MENU ======================================");
        System.out.println(" [1] Register New Student");
        System.out.println(" [2] Add Course to Catalog");
        System.out.println(" [3] Enroll Student into Course");
        System.out.println(" [4] Record / Update Attendance (Classes Held vs Attended)");
        System.out.println(" [5] Manage Medical Condonation (65% - 74.9% Eligibility)");
        System.out.println(" [6] Incremental Marks Entry (Phase 1: CIA1/2 | Phase 2: Assign | Phase 3: ESE)");
        System.out.println(" [7] REPORT 1: Student Semester Mark Sheet & SGPA");
        System.out.println(" [8] REPORT 2: Attendance Defaulter Report (Grade SA < 65%)");
        System.out.println(" [9] REPORT 3: Class Performance & Result Summary");
        System.out.println(" [10] Run Automated Demonstration Walkthrough (All Features)");
        System.out.println(" [0] Exit Application");
        System.out.println("==================================================================================");
    }

    // =========================================================================
    // 1. Student Registration
    // =========================================================================

    private static void handleRegisterStudent() {
        System.out.println("\n--- [1] Student Registration ---");
        try {
            System.out.print("Enter Roll Number (e.g., 24ITR010): ");
            String roll = scanner.nextLine().trim().toUpperCase();
            if (studentDAO.getStudentByRoll(roll) != null) {
                System.out.println("[!] Error: Student with Roll Number '" + roll + "' already exists!");
                return;
            }

            System.out.print("Enter Student Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Department (e.g., IT, CSE, ECE, MECH): ");
            String dept = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Current Semester (1-8): ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            Student s = new Student(roll, name, dept, sem);
            if (studentDAO.addStudent(s)) {
                System.out.println("[SUCCESS] Student registered successfully!");
                System.out.println("  " + s);
            } else {
                System.out.println("[!] Registration failed.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to register student: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. Course Catalog
    // =========================================================================

    private static void handleAddCourse() {
        System.out.println("\n--- [2] Add Course to Catalog ---");
        try {
            System.out.print("Enter Course Code (e.g., 24ITT31): ");
            String code = scanner.nextLine().trim().toUpperCase();
            if (studentDAO.getCourseByCode(code) != null) {
                System.out.println("[!] Error: Course with Code '" + code + "' already exists!");
                return;
            }

            System.out.print("Enter Course Title: ");
            String title = scanner.nextLine().trim();

            System.out.print("Enter Credits (e.g., 1, 2, 3, 4): ");
            int credits = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Course Type [1: THEORY, 2: PRACTICAL, 3: EMBEDDED]: ");
            String typeOpt = scanner.nextLine().trim();
            String type;
            if ("1".equals(typeOpt) || "THEORY".equalsIgnoreCase(typeOpt)) {
                type = "THEORY";
            } else if ("2".equals(typeOpt) || "PRACTICAL".equalsIgnoreCase(typeOpt)) {
                type = "PRACTICAL";
            } else if ("3".equals(typeOpt) || "EMBEDDED".equalsIgnoreCase(typeOpt)) {
                type = "EMBEDDED";
            } else {
                System.out.println("[!] Invalid course type. Defaulting to THEORY.");
                type = "THEORY";
            }

            System.out.print("Enter Department (e.g., IT / ALL): ");
            String dept = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Semester (1-8): ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            Course c = new Course(code, title, credits, type, dept, sem);
            if (studentDAO.addCourse(c)) {
                System.out.println("[SUCCESS] Course added to catalog successfully!");
                System.out.println("  " + c);
            } else {
                System.out.println("[!] Failed to add course.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to add course: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. Course Enrollment
    // =========================================================================

    private static void handleEnrollStudent() {
        System.out.println("\n--- [3] Course Enrollment ---");
        try {
            System.out.print("Enter Student Roll Number: ");
            String roll = scanner.nextLine().trim().toUpperCase();
            Student s = studentDAO.getStudentByRoll(roll);
            if (s == null) {
                System.out.println("[!] Student not found with Roll Number: " + roll);
                return;
            }

            System.out.print("Enter Course Code: ");
            String code = scanner.nextLine().trim().toUpperCase();
            Course c = studentDAO.getCourseByCode(code);
            if (c == null) {
                System.out.println("[!] Course not found with Code: " + code);
                return;
            }

            System.out.print("Enter Semester (e.g., " + s.getCurrentSemester() + "): ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            int enId = studentDAO.enrollStudent(roll, code, sem, "2024-2025");
            if (enId > 0) {
                System.out.println("[SUCCESS] Enrolled " + s.getName() + " (" + roll + ") into " + code + " - " + c.getCourseTitle());
                System.out.println("  Enrollment ID: " + enId);
            } else {
                System.out.println("[!] Enrollment failed or student already enrolled in this course for semester " + sem + ".");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to enroll student: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. Attendance Tracking
    // =========================================================================

    private static void handleUpdateAttendance() {
        System.out.println("\n--- [4] Record / Update Attendance ---");
        try {
            System.out.print("Enter Student Roll Number: ");
            String roll = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Course Code: ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse(roll, code, sem);
            if (en == null) {
                System.out.println("[!] No enrollment found for " + roll + " in " + code + " (Sem " + sem + ")");
                return;
            }

            System.out.printf("Current Record: Classes Held = %d | Classes Attended = %d | Attd %% = %.1f%%\n",
                    en.getClassesHeld(), en.getClassesAttended(), en.getAttendancePercentage());
            System.out.println("Current Status: " + en.getAttendanceStatusText());

            System.out.print("Enter Total Classes Held: ");
            int held = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Total Classes Attended: ");
            int attended = Integer.parseInt(scanner.nextLine().trim());

            if (attended > held) {
                System.out.println("[!] Classes attended cannot exceed total classes held.");
                return;
            }

            if (studentDAO.updateAttendance(en.getEnrollmentId(), held, attended)) {
                Enrollment updated = studentDAO.getEnrollment(en.getEnrollmentId());
                System.out.printf("[SUCCESS] Attendance updated! New Attendance: %.2f%% (%d/%d)\n",
                        updated.getAttendancePercentage(), attended, held);
                System.out.println("  ESE Eligibility Status: " + updated.getAttendanceStatusText());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Attendance update failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. Medical Condonation Management
    // =========================================================================

    private static void handleMedicalCondonation() {
        System.out.println("\n--- [5] Medical Condonation Management ---");
        System.out.println("KEC R2024 Rule: Attendance between 65% and 74.9% requires Medical Condonation approval.");
        try {
            System.out.print("Enter Student Roll Number: ");
            String roll = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Course Code: ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse(roll, code, sem);
            if (en == null) {
                System.out.println("[!] Enrollment record not found.");
                return;
            }

            double att = en.getAttendancePercentage();
            System.out.printf("Student: %s (%s) | Course: %s | Attendance: %.2f%%\n",
                    en.getStudentName(), roll, code, att);
            System.out.println("Current Condonation Status: " + (en.isMedicalCondonation() ? "APPROVED" : "NOT APPROVED"));

            if (att < 65.0) {
                System.out.println("[!] WARNING: Attendance is strictly below 65%. Condonation cannot be granted under KEC R2024!");
                System.out.println("    Student is barred from ESE and awarded SA grade.");
                return;
            } else if (att >= 75.0) {
                System.out.println("[i] Student already has >= 75% attendance. Condonation is not required.");
                return;
            }

            System.out.print("Do you want to APPROVE Medical Condonation for this student? (Y/N): ");
            String ans = scanner.nextLine().trim().toUpperCase();
            boolean approve = ans.startsWith("Y");

            studentDAO.setMedicalCondonation(en.getEnrollmentId(), approve);
            System.out.println("[SUCCESS] Medical condonation status updated to: " + (approve ? "APPROVED" : "REJECTED"));
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to update condonation: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. Incremental Marks Entry ("Then and Now")
    // =========================================================================

    private static void handleIncrementalMarksEntry() {
        System.out.println("\n================== INCREMENTAL MARKS ENTRY (THEN AND NOW) ==================");
        System.out.println(" [1] Phase 1 (Mid-Semester)      : Record CIA Test 1 (0-100) and CIA Test 2 (0-100)");
        System.out.println(" [2] Phase 2 (End-Semester CIA)  : Record Assignments / Quizzes / Lab Evaluations (0-100)");
        System.out.println(" [3] Phase 3 (Post-Semester ESE) : Record End Semester Exam Marks (0-100) or Absence (AB)");
        System.out.println("============================================================================");
        System.out.print("Select Phase (1-3): ");
        String phaseChoice = scanner.nextLine().trim();

        try {
            System.out.print("Enter Student Roll Number: ");
            String roll = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Course Code: ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Semester: ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse(roll, code, sem);
            if (en == null) {
                System.out.println("[!] Enrollment record not found.");
                return;
            }

            int enId = en.getEnrollmentId();
            AssessmentMark current = academicDAO.getAssessmentMark(enId);
            if (current != null) {
                System.out.printf("Current Record -> CIA1: %s | CIA2: %s | Assign: %s | ESE: %s | Absent: %b\n",
                        current.getCiaTest1(), current.getCiaTest2(), current.getAssignmentMarks(),
                        current.getEseMarks(), current.isAbsentEse());
            }

            switch (phaseChoice) {
                case "1": {
                    System.out.println("\n--- Phase 1: Mid-Semester CIA Tests ---");
                    System.out.print("Enter CIA Test 1 Marks (0 - 100): ");
                    double c1 = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("Enter CIA Test 2 Marks (0 - 100): ");
                    double c2 = Double.parseDouble(scanner.nextLine().trim());
                    if (academicDAO.recordPhase1Marks(enId, c1, c2)) {
                        System.out.println("[SUCCESS] Phase 1 marks recorded successfully!");
                    }
                    break;
                }
                case "2": {
                    System.out.println("\n--- Phase 2: End-Semester CIA Component ---");
                    System.out.print("Enter Assignments / Quizzes / Lab Evaluation Marks (0 - 100): ");
                    double as = Double.parseDouble(scanner.nextLine().trim());
                    if (academicDAO.recordPhase2Marks(enId, as)) {
                        System.out.println("[SUCCESS] Phase 2 continuous assessment recorded successfully!");
                    }
                    break;
                }
                case "3": {
                    System.out.println("\n--- Phase 3: Post-Semester End Semester Examination (ESE) ---");
                    System.out.printf("Verifying Attendance Eligibility: %.2f%% | %s\n",
                            en.getAttendancePercentage(), en.getAttendanceStatusText());

                    if (!en.isEligibleForEse()) {
                        System.out.println("[BARRED] Student does NOT meet KEC R2024 Attendance requirements!");
                        System.out.println("         Student is barred from taking ESE. Grade awarded: SA (0 GP).");
                        return;
                    }

                    System.out.print("Was the student ABSENT for the ESE? (Y/N): ");
                    String abs = scanner.nextLine().trim().toUpperCase();
                    if (abs.startsWith("Y")) {
                        academicDAO.recordPhase3Marks(enId, null, true);
                        System.out.println("[SUCCESS] Student marked as ABSENT (AB). Grade awarded: AB (0 GP).");
                    } else {
                        System.out.print("Enter ESE Marks (0 - 100): ");
                        double ese = Double.parseDouble(scanner.nextLine().trim());
                        academicDAO.recordPhase3Marks(enId, ese, false);
                        System.out.println("[SUCCESS] Phase 3 ESE marks recorded successfully!");
                    }
                    break;
                }
                default:
                    System.out.println("[!] Invalid phase selection.");
            }

            // Display updated course evaluation
            EvaluationResult eval = academicDAO.getCourseEvaluation(enId);
            if (eval != null) {
                System.out.println("\n--- Updated Course Evaluation ---");
                System.out.printf("Course Type: %s | Credits: %d | Attd: %.1f%%\n", eval.getCourseType(), eval.getCredits(), eval.getAttendancePercentage());
                System.out.printf("CIA Scaled: %.2f/%-2.0f | ESE Scaled: %.2f/%-2.0f | Total: %.2f/100\n",
                        eval.getCiaScaledMarks(), eval.getCiaMaxScaled(),
                        eval.getEseScaledMarks(), eval.getEseMaxScaled(),
                        eval.getTotalMarks());
                System.out.printf("Final Grade: %s | Grade Points: %d | Result Status: %s\n",
                        eval.getLetterGrade(), eval.getGradePoint(), eval.getResultStatus());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Marks entry failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. REPORT 1: Student Semester Mark Sheet & SGPA
    // =========================================================================

    public static void printStudentMarkSheet(String rollNumber, int semester) {
        try {
            Student student = studentDAO.getStudentByRoll(rollNumber);
            if (student == null) {
                System.out.println("\n[!] Student not found with Roll Number: " + rollNumber);
                return;
            }

            List<EvaluationResult> results = academicDAO.getStudentSemesterEvaluation(rollNumber, semester);
            if (results.isEmpty()) {
                System.out.println("\n[!] No course enrollments found for student " + rollNumber + " in semester " + semester);
                return;
            }

            int totalEnrolledCredits = 0;
            int totalCreditsEarned = 0;
            boolean allPassed = true;
            boolean anyIncomplete = false;

            for (EvaluationResult r : results) {
                totalEnrolledCredits += r.getCredits();
                if (r.isPassed()) {
                    totalCreditsEarned += r.getCredits();
                } else {
                    allPassed = false;
                    if ("INCOMPLETE".equals(r.getResultStatus())) {
                        anyIncomplete = true;
                    }
                }
            }

            double sgpa = academicDAO.calculateSGPA(results);
            double percentage = academicDAO.calculatePercentage(sgpa);

            System.out.println("\n====================================================================================================================");
            System.out.println("                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              ");
            System.out.println("                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS                                      ");
            System.out.println("                                STUDENT ACADEMIC & CONTINUOUS EVALUATION GRADE SHEET                                ");
            System.out.println("                                           (REGULATION 2024 - B.E. / B.TECH)                                        ");
            System.out.println("====================================================================================================================");
            System.out.printf(" Roll Number   : %-25s Student Name : %-30s\n", student.getRollNumber(), student.getName());
            System.out.printf(" Department    : %-25s Semester     : %d\n", student.getDepartment(), semester);
            System.out.printf(" Academic Year : 2024-2025                   Regulation   : R2024\n");
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
            System.out.printf(" %-8s %-32s %-9s %-4s %-6s %-5s %-5s %-6s %-8s %-8s %-8s %-6s %-5s %-3s %-12s\n",
                    "Code", "Course Title", "Type", "Cred", "Attd%", "CIA1", "CIA2", "Assgn", "CIA(Scl)", "ESE(Raw)", "ESE(Scl)", "Total", "Grade", "GP", "Status");
            System.out.println("--------------------------------------------------------------------------------------------------------------------");

            for (EvaluationResult r : results) {
                String c1Str = r.getCia1() != null ? String.format("%.1f", r.getCia1()) : "-";
                String c2Str = r.getCia2() != null ? String.format("%.1f", r.getCia2()) : "-";
                String asStr = r.getAssignment() != null ? String.format("%.1f", r.getAssignment()) : "-";
                String eseRawStr = r.isAbsentEse() ? "AB" : (r.getEseRawMarks() != null ? String.format("%.1f", r.getEseRawMarks()) : "-");
                String eseSclStr = r.isAbsentEse() ? "0.0" : (r.getEseRawMarks() != null ? String.format("%.1f", r.getEseScaledMarks()) : "-");

                System.out.printf(" %-8s %-32s %-9s %-4d %5.1f%% %-5s %-5s %-6s %5.1f/%-2.0f %-8s %-8s %5.1f  %-5s %-3d %-12s\n",
                        r.getCourseCode(),
                        r.getCourseTitle().length() > 32 ? r.getCourseTitle().substring(0, 31) : r.getCourseTitle(),
                        r.getCourseType(),
                        r.getCredits(),
                        r.getAttendancePercentage(),
                        c1Str, c2Str, asStr,
                        r.getCiaScaledMarks(), r.getCiaMaxScaled(),
                        eseRawStr, eseSclStr,
                        r.getTotalMarks(),
                        r.getLetterGrade(),
                        r.getGradePoint(),
                        r.getResultStatus());
            }

            System.out.println("--------------------------------------------------------------------------------------------------------------------");
            System.out.printf(" Total Enrolled Credits : %-15d Total Credits Earned  : %-15d\n", totalEnrolledCredits, totalCreditsEarned);
            System.out.printf(" SGPA Earned            : %-15.2f Equivalent Percentage: %.2f%%\n", sgpa, percentage);

            String finalClassification;
            if (anyIncomplete) {
                finalClassification = "INCOMPLETE (ESE Marks Pending)";
            } else if (allPassed) {
                if (sgpa >= 8.5) finalClassification = "FIRST CLASS WITH DISTINCTION";
                else if (sgpa >= 6.5) finalClassification = "FIRST CLASS";
                else finalClassification = "SECOND CLASS";
            } else {
                finalClassification = "ARREAR / RE-APPEARANCE";
            }
            System.out.printf(" Overall Result Status  : %s\n", finalClassification);
            System.out.println("====================================================================================================================");
            System.out.println(" [Grading Key: O(10: 91-100), A+(9: 81-90), A(8: 71-80), B+(7: 61-70), B(6: 50-60), RA/SA/AB(0: Fail)]");
            System.out.println(" [Attendance Rules: >=75% Eligible | 65-74.9% Conditionally Eligible (Condonation) | <65% SA (Barred)]");
            System.out.println("====================================================================================================================\n");

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to generate Mark Sheet: " + e.getMessage());
        }
    }

    private static void handleStudentMarkSheet() {
        System.out.println("\n--- [7] Generate Student Semester Mark Sheet ---");
        System.out.print("Enter Student Roll Number: ");
        String roll = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Semester: ");
        try {
            int sem = Integer.parseInt(scanner.nextLine().trim());
            printStudentMarkSheet(roll, sem);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid semester format.");
        }
    }

    // =========================================================================
    // 8. REPORT 2: Attendance Defaulter Report
    // =========================================================================

    public static void printAttendanceDefaulters(Integer semester, String department) {
        try {
            List<EvaluationResult> defaulters = academicDAO.getAttendanceDefaulters(semester, department);

            System.out.println("\n====================================================================================================================");
            System.out.println("                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              ");
            System.out.println("                                         ATTENDANCE DEFAULTER REPORT (< 65%)                                        ");
            System.out.println("                                           (REGULATION 2024 - B.E. / B.TECH)                                        ");
            System.out.println("====================================================================================================================");
            System.out.printf(" Filter: Department = %-15s | Semester = %s\n",
                    (department == null || department.isEmpty() ? "ALL" : department),
                    (semester != null && semester > 0 ? String.valueOf(semester) : "ALL"));
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
            System.out.printf(" %-10s %-25s %-6s %-8s %-5s %-5s %-7s %-30s %-12s\n",
                    "Roll No", "Student Name", "Dept", "Course", "Held", "Attd", "Attd%", "Eligibility / Reason", "Awarded Grade");
            System.out.println("--------------------------------------------------------------------------------------------------------------------");

            if (defaulters.isEmpty()) {
                System.out.println("                      No attendance defaulters found! All enrolled students meet eligibility criteria.             ");
            } else {
                for (EvaluationResult r : defaulters) {
                    String reason;
                    if (r.getAttendancePercentage() < 65.0) {
                        reason = "SHORTAGE (<65%) - BARRED";
                    } else {
                        reason = "CONDITIONAL - LACKS CONDONATION";
                    }

                    System.out.printf(" %-10s %-25s %-6s %-8s %-5d %-5d %5.1f%% %-30s %-12s\n",
                            r.getRollNumber(),
                            r.getStudentName().length() > 25 ? r.getStudentName().substring(0, 24) : r.getStudentName(),
                            r.getDepartment(),
                            r.getCourseCode(),
                            r.getClassesHeld(),
                            r.getClassesAttended(),
                            r.getAttendancePercentage(),
                            reason,
                            "SA (0 GP)");
                }
            }
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
            System.out.printf(" Total Defaulter Courses Flagged: %d (All barred from taking ESE, Grade awarded: SA)\n", defaulters.size());
            System.out.println("====================================================================================================================\n");

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to generate Attendance Defaulter Report: " + e.getMessage());
        }
    }

    private static void handleAttendanceDefaulterReport() {
        System.out.println("\n--- [8] Attendance Defaulter Report ---");
        System.out.print("Enter Semester (or 0 for ALL): ");
        int sem = 0;
        try {
            sem = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception ignored) {}

        System.out.print("Enter Department (or ALL): ");
        String dept = scanner.nextLine().trim().toUpperCase();

        printAttendanceDefaulters(sem == 0 ? null : sem, dept.isEmpty() ? "ALL" : dept);
    }

    // =========================================================================
    // 9. REPORT 3: Class Performance & Result Summary
    // =========================================================================

    public static void printClassPerformanceSummary(String department, int semester) {
        try {
            Map<String, Object> summary = academicDAO.getClassPerformanceSummary(department, semester);

            System.out.println("\n====================================================================================================================");
            System.out.println("                             KONGU ENGINEERING COLLEGE (AUTONOMOUS), PERUNDURAI, ERODE                              ");
            System.out.println("                                         CLASS PERFORMANCE & RESULT SUMMARY                                         ");
            System.out.println("                                           (REGULATION 2024 - B.E. / B.TECH)                                        ");
            System.out.println("====================================================================================================================");
            System.out.printf(" Department : %-25s Semester : %d\n", summary.get("department"), summary.get("semester"));
            System.out.printf(" Total Students Evaluated : %-15s Total Passed / All Clear : %-15s\n",
                    summary.get("totalStudents"), summary.get("passedStudents"));
            System.out.printf(" Total Failed / Arrears   : %-15s Overall Pass Percentage  : %.2f%%\n",
                    summary.get("failedStudents"), (double) summary.get("passPercentage"));
            System.out.printf(" Breakup: RA = %s | SA = %s | AB = %s       Class Average SGPA      : %.2f (%.2f%%)\n",
                    summary.get("raCount"), summary.get("saCount"), summary.get("abCount"),
                    (double) summary.get("classAvgSGPA"), (double) summary.get("avgPercentage"));
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
            System.out.printf(" %-10s %-25s %-8s %-8s %-10s %-12s\n",
                    "Roll No", "Student Name", "Dept", "SGPA", "Percentage", "Result Status");
            System.out.println("--------------------------------------------------------------------------------------------------------------------");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> students = (List<Map<String, Object>>) summary.get("studentSummaries");
            if (students == null || students.isEmpty()) {
                System.out.println("                                 No student records found for the specified class.                                  ");
            } else {
                for (Map<String, Object> s : students) {
                    System.out.printf(" %-10s %-25s %-8s %-8.2f %-10.2f%% %-12s\n",
                            s.get("rollNumber"),
                            ((String) s.get("name")).length() > 25 ? ((String) s.get("name")).substring(0, 24) : s.get("name"),
                            s.get("department"),
                            (double) s.get("sgpa"),
                            (double) s.get("percentage"),
                            s.get("status"));
                }
            }
            System.out.println("====================================================================================================================\n");

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to generate Class Performance Summary: " + e.getMessage());
        }
    }

    private static void handleClassPerformanceSummary() {
        System.out.println("\n--- [9] Class Performance & Result Summary ---");
        System.out.print("Enter Department (e.g., IT, CSE, or ALL): ");
        String dept = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Semester: ");
        try {
            int sem = Integer.parseInt(scanner.nextLine().trim());
            printClassPerformanceSummary(dept.isEmpty() ? "ALL" : dept, sem);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid semester format.");
        }
    }

    // =========================================================================
    // 10. Automated Demonstration Walkthrough
    // =========================================================================

    public static void runAutomatedDemo() {
        System.out.println("\n==================================================================================");
        System.out.println("               STARTING KEC R2024 SYSTEM DEMONSTRATION WALKTHROUGH                ");
        System.out.println("==================================================================================");

        try {
            // Step 1: Add a new student
            System.out.println("\n[DEMO STEP 1] Adding a new student: 24ITR099 - 'Suriya K' (IT Dept, Sem 3)...");
            Student newStudent = new Student("24ITR099", "Suriya K", "IT", 3);
            if (studentDAO.getStudentByRoll("24ITR099") == null) {
                studentDAO.addStudent(newStudent);
                System.out.println(" -> Successfully added student: " + newStudent);
            } else {
                System.out.println(" -> Student 24ITR099 already exists in database.");
            }

            // Step 2: Add a new course
            System.out.println("\n[DEMO STEP 2] Adding a new course: 24ITT39 - 'Cloud Computing Architecture' (THEORY, 3 Credits)...");
            Course newCourse = new Course("24ITT39", "Cloud Computing Architecture", 3, "THEORY", "IT", 3);
            if (studentDAO.getCourseByCode("24ITT39") == null) {
                studentDAO.addCourse(newCourse);
                System.out.println(" -> Successfully added course: " + newCourse);
            } else {
                System.out.println(" -> Course 24ITT39 already exists in database.");
            }

            // Step 3: Enroll student
            System.out.println("\n[DEMO STEP 3] Enrolling 24ITR099 into 24ITT39 (Semester 3)...");
            Enrollment en = studentDAO.getEnrollmentByStudentAndCourse("24ITR099", "24ITT39", 3);
            int enId;
            if (en == null) {
                enId = studentDAO.enrollStudent("24ITR099", "24ITT39", 3, "2024-2025");
                System.out.println(" -> Successfully enrolled! Generated Enrollment ID: " + enId);
            } else {
                enId = en.getEnrollmentId();
                System.out.println(" -> Student already enrolled with Enrollment ID: " + enId);
            }

            // Step 4: Record Attendance
            System.out.println("\n[DEMO STEP 4] Recording attendance: 45 classes attended out of 50 classes held (90.0%)...");
            studentDAO.updateAttendance(enId, 50, 45);
            Enrollment enUpdated = studentDAO.getEnrollment(enId);
            System.out.printf(" -> Attendance updated: %.1f%% - Status: %s\n",
                    enUpdated.getAttendancePercentage(), enUpdated.getAttendanceStatusText());

            // Step 5: Incremental Marks Entry - Phase 1 (Mid-Semester)
            System.out.println("\n[DEMO STEP 5A] Phase 1 (Mid-Semester): Recording CIA Test 1 = 92.0, CIA Test 2 = 88.0...");
            academicDAO.recordPhase1Marks(enId, 92.0, 88.0);
            System.out.println(" -> Phase 1 recorded successfully!");

            // Step 6: Incremental Marks Entry - Phase 2 (Continuous Evaluation)
            System.out.println("\n[DEMO STEP 5B] Phase 2 (End-Semester CIA): Recording Assignment / Quiz Marks = 95.0...");
            academicDAO.recordPhase2Marks(enId, 95.0);
            System.out.println(" -> Phase 2 recorded successfully!");

            // Step 7: Incremental Marks Entry - Phase 3 (End Semester Exam)
            System.out.println("\n[DEMO STEP 5C] Phase 3 (Post-Semester): Recording End Semester Exam Marks (ESE) = 90.0...");
            academicDAO.recordPhase3Marks(enId, 90.0, false);
            System.out.println(" -> Phase 3 recorded successfully!");

            // Step 8: Test Attendance Shortage Barring
            System.out.println("\n[DEMO STEP 6] Testing KEC R2024 Attendance Shortage Barring Rule (<65%)...");
            System.out.println(" -> Verifying 24ITR004 (Gowtham V) with 58.0% attendance in 24ITT31...");
            Enrollment defaulterEn = studentDAO.getEnrollmentByStudentAndCourse("24ITR004", "24ITT31", 3);
            if (defaulterEn != null) {
                System.out.printf("    Held: %d | Attended: %d | Attd %%: %.1f%% | Eligible for ESE: %b\n",
                        defaulterEn.getClassesHeld(), defaulterEn.getClassesAttended(),
                        defaulterEn.getAttendancePercentage(), defaulterEn.isEligibleForEse());
                try {
                    System.out.println("    Attempting to enter ESE marks for barred student...");
                    academicDAO.recordPhase3Marks(defaulterEn.getEnrollmentId(), 75.0, false);
                } catch (IllegalStateException e) {
                    System.out.println("    [EXPECTED BARRED BEHAVIOR TRIGGERED]: " + e.getMessage());
                }
            }

            // Step 9: Print Reports
            System.out.println("\n[DEMO STEP 7] Generating Formal Reports...");
            System.out.println("\n>>> REPORT 1A: High Achiever Grade Sheet (24ITR001 - Aravind K)");
            printStudentMarkSheet("24ITR001", 3);

            System.out.println("\n>>> REPORT 1B: Newly Added Student Grade Sheet (24ITR099 - Suriya K)");
            printStudentMarkSheet("24ITR099", 3);

            System.out.println("\n>>> REPORT 1C: Attendance Shortage Barred Grade Sheet (24ITR004 - Gowtham V)");
            printStudentMarkSheet("24ITR004", 3);

            System.out.println("\n>>> REPORT 2: Attendance Defaulter Report (All Students with Grade SA < 65%)");
            printAttendanceDefaulters(3, "IT");

            System.out.println("\n>>> REPORT 3: Class Performance & Result Summary (Department: IT, Semester: 3)");
            printClassPerformanceSummary("IT", 3);

            System.out.println("==================================================================================");
            System.out.println("                DEMONSTRATION WALKTHROUGH COMPLETED SUCCESSFULLY!                 ");
            System.out.println("==================================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Demonstration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleRunFullSystemDemo() {
        runAutomatedDemo();
    }

    private static void handleCliArguments(String[] args) {
        String cmd = args[0].toLowerCase();
        if (cmd.equals("--demo") || cmd.equals("-d")) {
            runAutomatedDemo();
        } else if (cmd.equals("--marksheet") || cmd.equals("-m")) {
            String roll = args.length > 1 ? args[1] : "24ITR001";
            int sem = args.length > 2 ? Integer.parseInt(args[2]) : 3;
            printStudentMarkSheet(roll, sem);
        } else if (cmd.equals("--defaulters") || cmd.equals("-df")) {
            int sem = args.length > 1 ? Integer.parseInt(args[1]) : 3;
            String dept = args.length > 2 ? args[2] : "ALL";
            printAttendanceDefaulters(sem, dept);
        } else if (cmd.equals("--summary") || cmd.equals("-s")) {
            String dept = args.length > 1 ? args[1] : "IT";
            int sem = args.length > 2 ? Integer.parseInt(args[2]) : 3;
            printClassPerformanceSummary(dept, sem);
        } else {
            System.out.println("Available CLI Options:");
            System.out.println("  --demo                        Run complete automated system demonstration");
            System.out.println("  --marksheet <roll> [sem]      Generate student semester mark sheet");
            System.out.println("  --defaulters [sem] [dept]     Generate attendance defaulter report");
            System.out.println("  --summary [dept] [sem]        Generate class performance summary");
        }
    }
}

