/**
 * Course entity representing a course in KEC Regulation 2024.
 * Course Types: THEORY, PRACTICAL, EMBEDDED
 */
public class Course {
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String courseType; // THEORY, PRACTICAL, EMBEDDED
    private String department;
    private int semester;

    public Course() {
    }

    public Course(String courseCode, String courseTitle, int credits, String courseType, String department, int semester) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.courseType = courseType.toUpperCase();
        this.department = department;
        this.semester = semester;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType != null ? courseType.toUpperCase() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-35s | Credits: %d | Type: %-10s | Sem: %d",
                courseCode, courseTitle, credits, courseType, semester);
    }
}

