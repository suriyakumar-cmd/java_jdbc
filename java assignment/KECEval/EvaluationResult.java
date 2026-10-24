/**
 * EvaluationResult encapsulates the computed academic results for a single course enrollment
 * strictly adhering to Kongu Engineering College - Regulation 2024 (KEC R2024).
 */
public class EvaluationResult {
    private String rollNumber;
    private String studentName;
    private String department;
    private int semester;

    private String courseCode;
    private String courseTitle;
    private String courseType; // THEORY, PRACTICAL, EMBEDDED
    private int credits;

    private int classesHeld;
    private int classesAttended;
    private double attendancePercentage;
    private boolean medicalCondonation;
    private boolean eligibleForEse;

    private Double cia1;
    private Double cia2;
    private Double assignment;
    private double cia100Scale;
    private double ciaScaledMarks;
    private double ciaMaxScaled;

    private Double eseRawMarks;
    private double eseScaledMarks;
    private double eseMaxScaled;
    private double eseMinRequiredRaw;
    private double eseMinRequiredScaled;
    private boolean isAbsentEse;

    private double totalMarks;
    private String letterGrade;
    private int gradePoint;
    private String resultStatus;
    private boolean passed;

    public EvaluationResult() {
    }

    public static EvaluationResult evaluate(Enrollment enrollment, Course course, AssessmentMark mark, Student student) {
        EvaluationResult res = new EvaluationResult();
        res.rollNumber = enrollment.getRollNumber();
        res.studentName = student != null ? student.getName() : enrollment.getStudentName();
        res.department = student != null ? student.getDepartment() : "";
        res.semester = enrollment.getSemester();

        res.courseCode = course.getCourseCode();
        res.courseTitle = course.getCourseTitle();
        res.courseType = course.getCourseType().toUpperCase();
        res.credits = course.getCredits();

        res.classesHeld = enrollment.getClassesHeld();
        res.classesAttended = enrollment.getClassesAttended();
        res.attendancePercentage = enrollment.getAttendancePercentage();
        res.medicalCondonation = enrollment.isMedicalCondonation();
        res.eligibleForEse = enrollment.isEligibleForEse();

        // Weightage configuration by Course Type (KEC R2024)
        double ciaWeightPercent;
        double eseWeightPercent;
        if ("THEORY".equalsIgnoreCase(res.courseType)) {
            ciaWeightPercent = 40.0;
            eseWeightPercent = 60.0;
            res.eseMinRequiredRaw = 45.0; // 45% of 100
            res.eseMinRequiredScaled = 27.0; // 27/60
        } else if ("PRACTICAL".equalsIgnoreCase(res.courseType)) {
            ciaWeightPercent = 60.0;
            eseWeightPercent = 40.0;
            res.eseMinRequiredRaw = 50.0; // 50% of 100
            res.eseMinRequiredScaled = 20.0; // 20/40
        } else { // EMBEDDED
            ciaWeightPercent = 50.0;
            eseWeightPercent = 50.0;
            res.eseMinRequiredRaw = 45.0; // 45% of 100
            res.eseMinRequiredScaled = 22.5; // 22.5/50
        }

        res.ciaMaxScaled = ciaWeightPercent;
        res.eseMaxScaled = eseWeightPercent;

        if (mark != null) {
            res.cia1 = mark.getCiaTest1();
            res.cia2 = mark.getCiaTest2();
            res.assignment = mark.getAssignmentMarks();
            res.isAbsentEse = mark.isAbsentEse();
            res.eseRawMarks = mark.getEseMarks();

            // Compute 100-scale CIA: CIA1(40%) + CIA2(40%) + Assignment(20%)
            double c1 = res.cia1 != null ? res.cia1 : 0.0;
            double c2 = res.cia2 != null ? res.cia2 : 0.0;
            double as = res.assignment != null ? res.assignment : 0.0;
            res.cia100Scale = (c1 * 0.40) + (c2 * 0.40) + (as * 0.20);
            res.ciaScaledMarks = (res.cia100Scale / 100.0) * ciaWeightPercent;

            if (res.eseRawMarks != null) {
                res.eseScaledMarks = (res.eseRawMarks / 100.0) * eseWeightPercent;
            } else {
                res.eseScaledMarks = 0.0;
            }
        } else {
            res.cia100Scale = 0.0;
            res.ciaScaledMarks = 0.0;
            res.eseScaledMarks = 0.0;
            res.isAbsentEse = false;
        }

        res.totalMarks = res.ciaScaledMarks + res.eseScaledMarks;

        // Determine Grade and Status under KEC R2024
        if (!res.eligibleForEse) {
            // Barred from ESE due to attendance shortage (<65% or 65-74.9% without condonation)
            res.letterGrade = "SA";
            res.gradePoint = 0;
            res.resultStatus = "FAIL (SA)";
            res.passed = false;
        } else if (res.isAbsentEse) {
            res.letterGrade = "AB";
            res.gradePoint = 0;
            res.resultStatus = "FAIL (AB)";
            res.passed = false;
        } else if (mark == null || mark.getEseMarks() == null) {
            res.letterGrade = "--";
            res.gradePoint = 0;
            res.resultStatus = "INCOMPLETE";
            res.passed = false;
        } else if (res.eseRawMarks < res.eseMinRequiredRaw) {
            // Failed ESE Minimum Threshold
            res.letterGrade = "RA";
            res.gradePoint = 0;
            res.resultStatus = "FAIL (ESE MIN)";
            res.passed = false;
        } else if (Math.round(res.totalMarks) < 50) {
            // Failed Overall Pass Minimum (50 Marks)
            res.letterGrade = "RA";
            res.gradePoint = 0;
            res.resultStatus = "FAIL (AGGREGATE)";
            res.passed = false;
        } else {
            // PASSED - Compute 10-Point Grade based on rounded aggregate
            long finalMarkRounded = Math.round(res.totalMarks);
            if (finalMarkRounded >= 91) {
                res.letterGrade = "O";
                res.gradePoint = 10;
            } else if (finalMarkRounded >= 81) {
                res.letterGrade = "A+";
                res.gradePoint = 9;
            } else if (finalMarkRounded >= 71) {
                res.letterGrade = "A";
                res.gradePoint = 8;
            } else if (finalMarkRounded >= 61) {
                res.letterGrade = "B+";
                res.gradePoint = 7;
            } else { // 50 to 60
                res.letterGrade = "B";
                res.gradePoint = 6;
            }
            res.resultStatus = "PASS";
            res.passed = true;
        }

        return res;
    }

    // Getters
    public String getRollNumber() { return rollNumber; }
    public String getStudentName() { return studentName; }
    public String getDepartment() { return department; }
    public int getSemester() { return semester; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getCourseType() { return courseType; }
    public int getCredits() { return credits; }
    public int getClassesHeld() { return classesHeld; }
    public int getClassesAttended() { return classesAttended; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public boolean isMedicalCondonation() { return medicalCondonation; }
    public boolean isEligibleForEse() { return eligibleForEse; }
    public Double getCia1() { return cia1; }
    public Double getCia2() { return cia2; }
    public Double getAssignment() { return assignment; }
    public double getCia100Scale() { return cia100Scale; }
    public double getCiaScaledMarks() { return ciaScaledMarks; }
    public double getCiaMaxScaled() { return ciaMaxScaled; }
    public Double getEseRawMarks() { return eseRawMarks; }
    public double getEseScaledMarks() { return eseScaledMarks; }
    public double getEseMaxScaled() { return eseMaxScaled; }
    public double getEseMinRequiredRaw() { return eseMinRequiredRaw; }
    public double getEseMinRequiredScaled() { return eseMinRequiredScaled; }
    public boolean isAbsentEse() { return isAbsentEse; }
    public double getTotalMarks() { return totalMarks; }
    public String getLetterGrade() { return letterGrade; }
    public int getGradePoint() { return gradePoint; }
    public String getResultStatus() { return resultStatus; }
    public boolean isPassed() { return passed; }
}

