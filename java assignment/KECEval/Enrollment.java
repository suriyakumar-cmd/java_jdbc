/**
 * Enrollment entity representing a student's enrollment into a course
 * and continuous attendance tracking under KEC R2024.
 */
public class Enrollment {
    private int enrollmentId;
    private String rollNumber;
    private String courseCode;
    private int semester;
    private String academicYear;
    private int classesHeld;
    private int classesAttended;
    private boolean medicalCondonation;

    // Optional joined fields for display convenience
    private String studentName;
    private String courseTitle;
    private String courseType;
    private int credits;

    public Enrollment() {
        this.academicYear = "2024-2025";
    }

    public Enrollment(int enrollmentId, String rollNumber, String courseCode, int semester,
                      String academicYear, int classesHeld, int classesAttended, boolean medicalCondonation) {
        this.enrollmentId = enrollmentId;
        this.rollNumber = rollNumber;
        this.courseCode = courseCode;
        this.semester = semester;
        this.academicYear = academicYear != null ? academicYear : "2024-2025";
        this.classesHeld = classesHeld;
        this.classesAttended = classesAttended;
        this.medicalCondonation = medicalCondonation;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getClassesHeld() {
        return classesHeld;
    }

    public void setClassesHeld(int classesHeld) {
        this.classesHeld = classesHeld;
    }

    public int getClassesAttended() {
        return classesAttended;
    }

    public void setClassesAttended(int classesAttended) {
        this.classesAttended = classesAttended;
    }

    public boolean isMedicalCondonation() {
        return medicalCondonation;
    }

    public void setMedicalCondonation(boolean medicalCondonation) {
        this.medicalCondonation = medicalCondonation;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * Calculates Attendance Percentage.
     * If classesHeld == 0, returns 100.0 (no classes conducted yet).
     */
    public double getAttendancePercentage() {
        if (classesHeld <= 0) {
            return 100.0;
        }
        return (classesAttended * 100.0) / classesHeld;
    }

    /**
     * Determines ESE Eligibility under KEC R2024:
     * - >= 75%: Eligible
     * - 65% to 74.9%: Conditionally eligible with Medical Condonation approval
     * - < 65%: Barred from ESE (Award SA grade)
     */
    public boolean isEligibleForEse() {
        double att = getAttendancePercentage();
        if (att >= 75.0) {
            return true;
        } else if (att >= 65.0) {
            return medicalCondonation;
        } else {
            return false;
        }
    }

    public String getAttendanceStatusText() {
        double att = getAttendancePercentage();
        if (att >= 75.0) {
            return "ELIGIBLE (>= 75%)";
        } else if (att >= 65.0) {
            if (medicalCondonation) {
                return "CONDONED (Approved 65-74.9%)";
            } else {
                return "CONDITIONAL (Pending Condonation)";
            }
        } else {
            return "BARRED (< 65% Shortage - SA)";
        }
    }
}

