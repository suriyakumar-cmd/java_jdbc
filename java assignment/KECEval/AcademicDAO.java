import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AcademicDAO - Data Access Object & Assessment Engine for KEC R2024.
 * Handles incremental marks entry (Phases 1, 2, 3), attendance eligibility enforcement,
 * grade calculation, SGPA computation, and academic reporting queries.
 */
public class AcademicDAO {

    private final StudentDAO studentDAO = new StudentDAO();

    // =========================================================================
    // 1. Phased Incremental Marks Entry
    // =========================================================================

    /**
     * Phase 1 (Mid-Semester): Record CIA Test 1 (0-100) and CIA Test 2 (0-100).
     */
    public boolean recordPhase1Marks(int enrollmentId, double cia1, double cia2) throws SQLException, IllegalArgumentException {
        if (cia1 < 0 || cia1 > 100 || cia2 < 0 || cia2 > 100) {
            throw new IllegalArgumentException("CIA marks must be within 0 to 100.");
        }

        String sql = "INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE cia_test1 = VALUES(cia_test1), cia_test2 = VALUES(cia_test2)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setDouble(2, cia1);
            ps.setDouble(3, cia2);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Phase 2 (End-Semester CIA): Record Assignments / Quizzes / Lab Evaluations (0-100).
     */
    public boolean recordPhase2Marks(int enrollmentId, double assignmentMarks) throws SQLException, IllegalArgumentException {
        if (assignmentMarks < 0 || assignmentMarks > 100) {
            throw new IllegalArgumentException("Assignment / Lab evaluation marks must be within 0 to 100.");
        }

        String sql = "INSERT INTO assessment_marks (enrollment_id, assignment_marks) " +
                     "VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE assignment_marks = VALUES(assignment_marks)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setDouble(2, assignmentMarks);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Phase 3 (Post-Semester): Record End Semester Examination (ESE) Marks (0-100) or Absence.
     * Enforces KEC R2024 Attendance Eligibility before recording ESE marks:
     * - Attendance >= 75%: Allowed
     * - Attendance 65% - 74.9%: Allowed only if Medical Condonation is approved
     * - Attendance < 65%: Barred from ESE (Award SA grade)
     */
    public boolean recordPhase3Marks(int enrollmentId, Double eseMarks, boolean isAbsent) throws SQLException, IllegalStateException, IllegalArgumentException {
        Enrollment enrollment = studentDAO.getEnrollment(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment record not found for ID: " + enrollmentId);
        }

        // Attendance Eligibility Check
        if (!enrollment.isEligibleForEse()) {
            double att = enrollment.getAttendancePercentage();
            String reason = att < 65.0 ?
                    String.format("Attendance shortage (%.1f%% < 65%%). Student is BARRED from ESE and awarded SA grade.", att) :
                    String.format("Attendance (%.1f%%) requires medical condonation approval before ESE entry.", att);
            throw new IllegalStateException(reason);
        }

        if (!isAbsent && eseMarks != null && (eseMarks < 0 || eseMarks > 100)) {
            throw new IllegalArgumentException("ESE marks must be within 0 to 100.");
        }

        String sql = "INSERT INTO assessment_marks (enrollment_id, ese_marks, is_absent_ese) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE ese_marks = VALUES(ese_marks), is_absent_ese = VALUES(is_absent_ese)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            if (isAbsent || eseMarks == null) {
                ps.setNull(2, java.sql.Types.DECIMAL);
                ps.setBoolean(3, true);
            } else {
                ps.setDouble(2, eseMarks);
                ps.setBoolean(3, false);
            }
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 2. Fetch Assessment Marks
    // =========================================================================

    public AssessmentMark getAssessmentMark(int enrollmentId) throws SQLException {
        String sql = "SELECT mark_id, enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent_ese " +
                     "FROM assessment_marks WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Double cia1 = rs.getObject("cia_test1") != null ? rs.getDouble("cia_test1") : null;
                    Double cia2 = rs.getObject("cia_test2") != null ? rs.getDouble("cia_test2") : null;
                    Double assign = rs.getObject("assignment_marks") != null ? rs.getDouble("assignment_marks") : null;
                    Double ese = rs.getObject("ese_marks") != null ? rs.getDouble("ese_marks") : null;
                    boolean absent = rs.getBoolean("is_absent_ese");
                    return new AssessmentMark(rs.getInt("mark_id"), enrollmentId, cia1, cia2, assign, ese, absent);
                }
            }
        }
        return null;
    }

    // =========================================================================
    // 3. Evaluation & Reporting Engine
    // =========================================================================

    /**
     * Evaluates a single course enrollment.
     */
    public EvaluationResult getCourseEvaluation(int enrollmentId) throws SQLException {
        Enrollment enrollment = studentDAO.getEnrollment(enrollmentId);
        if (enrollment == null) return null;

        Student student = studentDAO.getStudentByRoll(enrollment.getRollNumber());
        Course course = studentDAO.getCourseByCode(enrollment.getCourseCode());
        AssessmentMark mark = getAssessmentMark(enrollmentId);

        return EvaluationResult.evaluate(enrollment, course, mark, student);
    }

    /**
     * Evaluates all courses enrolled by a student for a semester.
     */
    public List<EvaluationResult> getStudentSemesterEvaluation(String rollNumber, int semester) throws SQLException {
        List<EvaluationResult> results = new ArrayList<>();
        Student student = studentDAO.getStudentByRoll(rollNumber);
        if (student == null) return results;

        List<Enrollment> enrollments = studentDAO.getEnrollmentsByStudent(rollNumber, semester);
        for (Enrollment en : enrollments) {
            Course course = studentDAO.getCourseByCode(en.getCourseCode());
            AssessmentMark mark = getAssessmentMark(en.getEnrollmentId());
            results.add(EvaluationResult.evaluate(en, course, mark, student));
        }
        return results;
    }

    /**
     * SGPA Calculation Formula (KEC R2024):
     * SGPA = Sum(Course Credits * Grade Points Earned) / Sum(Total Enrolled Credits)
     */
    public double calculateSGPA(List<EvaluationResult> results) {
        int totalCredits = 0;
        double weightedPoints = 0.0;

        for (EvaluationResult r : results) {
            totalCredits += r.getCredits();
            weightedPoints += (r.getCredits() * r.getGradePoint());
        }

        if (totalCredits == 0) return 0.0;
        return weightedPoints / totalCredits;
    }

    /**
     * Percentage Equivalent Formula (KEC R2024):
     * Equivalent Percentage = SGPA * 10
     */
    public double calculatePercentage(double sgpa) {
        return sgpa * 10.0;
    }

    /**
     * Finds all students with attendance less than 65% or unapproved condonation.
     */
    public List<EvaluationResult> getAttendanceDefaulters(Integer semester, String department) throws SQLException {
        List<EvaluationResult> defaulters = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.enrollment_id FROM enrollments e ")
           .append("JOIN students s ON e.roll_number = s.roll_number ")
           .append("WHERE 1=1 ");

        if (semester != null && semester > 0) {
            sql.append("AND e.semester = ").append(semester).append(" ");
        }
        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("ALL")) {
            sql.append("AND s.department = '").append(department.trim().toUpperCase()).append("' ");
        }
        sql.append("ORDER BY s.department, e.roll_number, e.course_code");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int enId = rs.getInt("enrollment_id");
                EvaluationResult eval = getCourseEvaluation(enId);
                if (eval != null && !eval.isEligibleForEse()) {
                    defaulters.add(eval);
                }
            }
        }
        return defaulters;
    }

    /**
     * Class Performance & Result Summary.
     * Department-wise & semester-wise pass/fail counts and class average SGPA.
     */
    public Map<String, Object> getClassPerformanceSummary(String department, int semester) throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        List<Student> students;
        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("ALL")) {
            students = studentDAO.getStudentsByDeptAndSem(department, semester);
        } else {
            // All students in that semester
            students = new ArrayList<>();
            for (Student s : studentDAO.getAllStudents()) {
                if (s.getCurrentSemester() == semester) {
                    students.add(s);
                }
            }
        }

        int totalStudents = students.size();
        int passedStudents = 0;
        int failedStudents = 0;
        int saCount = 0;
        int abCount = 0;
        int raCount = 0;

        double sumSGPA = 0.0;
        int studentsWithCompletedExams = 0;

        List<Map<String, Object>> studentSummaries = new ArrayList<>();

        for (Student s : students) {
            List<EvaluationResult> evals = getStudentSemesterEvaluation(s.getRollNumber(), semester);
            if (evals.isEmpty()) continue;

            boolean allPassed = true;
            boolean hasSA = false;
            boolean hasAB = false;
            boolean hasRA = false;
            boolean allCompleted = true;

            for (EvaluationResult ev : evals) {
                if ("SA".equals(ev.getLetterGrade())) {
                    hasSA = true;
                    allPassed = false;
                } else if ("AB".equals(ev.getLetterGrade())) {
                    hasAB = true;
                    allPassed = false;
                } else if ("RA".equals(ev.getLetterGrade())) {
                    hasRA = true;
                    allPassed = false;
                } else if ("--".equals(ev.getLetterGrade())) {
                    allCompleted = false;
                    allPassed = false;
                }
            }

            double sgpa = calculateSGPA(evals);
            if (allCompleted) {
                studentsWithCompletedExams++;
                sumSGPA += sgpa;
            }

            if (allPassed) {
                passedStudents++;
            } else {
                failedStudents++;
                if (hasSA) saCount++;
                if (hasAB) abCount++;
                if (hasRA) raCount++;
            }

            Map<String, Object> studentRow = new HashMap<>();
            studentRow.put("rollNumber", s.getRollNumber());
            studentRow.put("name", s.getName());
            studentRow.put("department", s.getDepartment());
            studentRow.put("semester", semester);
            studentRow.put("sgpa", sgpa);
            studentRow.put("percentage", calculatePercentage(sgpa));
            studentRow.put("status", allPassed ? "ALL CLEAR" : (hasSA ? "FAIL (SA)" : (hasAB ? "FAIL (AB)" : "FAIL (RA)")));
            studentSummaries.add(studentRow);
        }

        double classAvgSGPA = studentsWithCompletedExams > 0 ? (sumSGPA / studentsWithCompletedExams) : 0.0;
        double passPercentage = totalStudents > 0 ? ((passedStudents * 100.0) / totalStudents) : 0.0;

        summary.put("department", (department == null || department.equalsIgnoreCase("ALL")) ? "ALL DEPARTMENTS" : department);
        summary.put("semester", semester);
        summary.put("totalStudents", totalStudents);
        summary.put("passedStudents", passedStudents);
        summary.put("failedStudents", failedStudents);
        summary.put("saCount", saCount);
        summary.put("abCount", abCount);
        summary.put("raCount", raCount);
        summary.put("passPercentage", passPercentage);
        summary.put("classAvgSGPA", classAvgSGPA);
        summary.put("avgPercentage", classAvgSGPA * 10.0);
        summary.put("studentSummaries", studentSummaries);

        return summary;
    }
}

