import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO - Data Access Object for Student, Course, and Enrollment management.
 */
public class StudentDAO {

    // =========================================================================
    // 1. Student Registration & Management
    // =========================================================================

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_number, name, department, current_semester) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNumber().trim().toUpperCase());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getDepartment().trim().toUpperCase());
            ps.setInt(4, student.getCurrentSemester());
            return ps.executeUpdate() > 0;
        }
    }

    public Student getStudentByRoll(String rollNumber) throws SQLException {
        String sql = "SELECT roll_number, name, department, current_semester FROM students WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("roll_number"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("current_semester")
                    );
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_number, name, department, current_semester FROM students ORDER BY roll_number";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("current_semester")
                ));
            }
        }
        return list;
    }

    public List<Student> getStudentsByDeptAndSem(String department, int semester) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_number, name, department, current_semester FROM students " +
                     "WHERE department = ? AND current_semester = ? ORDER BY roll_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.trim().toUpperCase());
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getString("roll_number"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("current_semester")
                    ));
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 2. Course Catalog
    // =========================================================================

    public boolean addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_title, credits, course_type, department, semester) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode().trim().toUpperCase());
            ps.setString(2, course.getCourseTitle().trim());
            ps.setInt(3, course.getCredits());
            ps.setString(4, course.getCourseType().trim().toUpperCase());
            ps.setString(5, course.getDepartment() != null ? course.getDepartment().trim().toUpperCase() : "ALL");
            ps.setInt(6, course.getSemester());
            return ps.executeUpdate() > 0;
        }
    }

    public Course getCourseByCode(String courseCode) throws SQLException {
        String sql = "SELECT course_code, course_title, credits, course_type, department, semester FROM courses WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getInt("credits"),
                            rs.getString("course_type"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    );
                }
            }
        }
        return null;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT course_code, course_title, credits, course_type, department, semester FROM courses ORDER BY course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getInt("credits"),
                        rs.getString("course_type"),
                        rs.getString("department"),
                        rs.getInt("semester")
                ));
            }
        }
        return list;
    }

    // =========================================================================
    // 3. Course Enrollment & Attendance Tracking
    // =========================================================================

    public int enrollStudent(String rollNumber, String courseCode, int semester, String academicYear) throws SQLException {
        String sql = "INSERT INTO enrollments (roll_number, course_code, semester, academic_year, classes_held, classes_attended, medical_condonation) " +
                     "VALUES (?, ?, ?, ?, 0, 0, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            ps.setString(2, courseCode.trim().toUpperCase());
            ps.setInt(3, semester);
            ps.setString(4, academicYear != null ? academicYear : "2024-2025");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int enrollmentId = rs.getInt(1);
                        // Initialize empty row in assessment_marks for seamless incremental updates
                        initAssessmentMarks(conn, enrollmentId);
                        return enrollmentId;
                    }
                }
            }
        }
        return -1;
    }

    private void initAssessmentMarks(Connection conn, int enrollmentId) throws SQLException {
        String sql = "INSERT IGNORE INTO assessment_marks (enrollment_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    public boolean updateAttendance(int enrollmentId, int classesHeld, int classesAttended) throws SQLException {
        String sql = "UPDATE enrollments SET classes_held = ?, classes_attended = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classesHeld);
            ps.setInt(2, classesAttended);
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setMedicalCondonation(int enrollmentId, boolean approved) throws SQLException {
        String sql = "UPDATE enrollments SET medical_condonation = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, approved);
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public Enrollment getEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_number, e.course_code, e.semester, e.academic_year, " +
                     "e.classes_held, e.classes_attended, e.medical_condonation, " +
                     "s.name AS student_name, c.course_title, c.course_type, c.credits " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_number = s.roll_number " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_number"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getString("academic_year"),
                            rs.getInt("classes_held"),
                            rs.getInt("classes_attended"),
                            rs.getBoolean("medical_condonation")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCourseType(rs.getString("course_type"));
                    e.setCredits(rs.getInt("credits"));
                    return e;
                }
            }
        }
        return null;
    }

    public Enrollment getEnrollmentByStudentAndCourse(String rollNumber, String courseCode, int semester) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_number, e.course_code, e.semester, e.academic_year, " +
                     "e.classes_held, e.classes_attended, e.medical_condonation, " +
                     "s.name AS student_name, c.course_title, c.course_type, c.credits " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_number = s.roll_number " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.roll_number = ? AND e.course_code = ? AND e.semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            ps.setString(2, courseCode.trim().toUpperCase());
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_number"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getString("academic_year"),
                            rs.getInt("classes_held"),
                            rs.getInt("classes_attended"),
                            rs.getBoolean("medical_condonation")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCourseType(rs.getString("course_type"));
                    e.setCredits(rs.getInt("credits"));
                    return e;
                }
            }
        }
        return null;
    }

    public List<Enrollment> getEnrollmentsByStudent(String rollNumber, int semester) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_number, e.course_code, e.semester, e.academic_year, " +
                     "e.classes_held, e.classes_attended, e.medical_condonation, " +
                     "s.name AS student_name, c.course_title, c.course_type, c.credits " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_number = s.roll_number " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.roll_number = ? AND e.semester = ? " +
                     "ORDER BY e.course_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_number"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getString("academic_year"),
                            rs.getInt("classes_held"),
                            rs.getInt("classes_attended"),
                            rs.getBoolean("medical_condonation")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCourseType(rs.getString("course_type"));
                    e.setCredits(rs.getInt("credits"));
                    list.add(e);
                }
            }
        }
        return list;
    }
}

