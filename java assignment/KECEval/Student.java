/**
 * Student entity representing a registered student at Kongu Engineering College.
 */
public class Student {
    private String rollNumber;
    private String name;
    private String department;
    private int currentSemester;

    public Student() {
    }

    public Student(String rollNumber, String name, String department, int currentSemester) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.currentSemester = currentSemester;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    @Override
    public String toString() {
        return String.format("%-12s | %-25s | %-8s | Sem: %d", rollNumber, name, department, currentSemester);
    }
}

