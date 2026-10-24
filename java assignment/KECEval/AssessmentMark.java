/**
 * AssessmentMark entity representing incremental phased marks entry under KEC R2024.
 * Phase 1 (Mid-Semester): CIA Test 1 (0-100) and CIA Test 2 (0-100)
 * Phase 2 (End-Semester CIA): Assignments / Quizzes / Lab Evaluations (0-100)
 * Phase 3 (Post-Semester): End Semester Examination (ESE) Marks (0-100) and Absence flag
 */
public class AssessmentMark {
    private int markId;
    private int enrollmentId;
    private Double ciaTest1;
    private Double ciaTest2;
    private Double assignmentMarks;
    private Double eseMarks;
    private boolean isAbsentEse;

    public AssessmentMark() {
    }

    public AssessmentMark(int markId, int enrollmentId, Double ciaTest1, Double ciaTest2,
                          Double assignmentMarks, Double eseMarks, boolean isAbsentEse) {
        this.markId = markId;
        this.enrollmentId = enrollmentId;
        this.ciaTest1 = ciaTest1;
        this.ciaTest2 = ciaTest2;
        this.assignmentMarks = assignmentMarks;
        this.eseMarks = eseMarks;
        this.isAbsentEse = isAbsentEse;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getCiaTest1() {
        return ciaTest1;
    }

    public void setCiaTest1(Double ciaTest1) {
        this.ciaTest1 = ciaTest1;
    }

    public Double getCiaTest2() {
        return ciaTest2;
    }

    public void setCiaTest2(Double ciaTest2) {
        this.ciaTest2 = ciaTest2;
    }

    public Double getAssignmentMarks() {
        return assignmentMarks;
    }

    public void setAssignmentMarks(Double assignmentMarks) {
        this.assignmentMarks = assignmentMarks;
    }

    public Double getEseMarks() {
        return eseMarks;
    }

    public void setEseMarks(Double eseMarks) {
        this.eseMarks = eseMarks;
    }

    public boolean isAbsentEse() {
        return isAbsentEse;
    }

    public void setAbsentEse(boolean absentEse) {
        isAbsentEse = absentEse;
    }

    public boolean isPhase1Complete() {
        return ciaTest1 != null && ciaTest2 != null;
    }

    public boolean isPhase2Complete() {
        return assignmentMarks != null;
    }

    public boolean isPhase3Complete() {
        return isAbsentEse || eseMarks != null;
    }
}

