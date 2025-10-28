package shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private String id;
    private String title;
    private String assignee;
    private String status; // "pending", "in-progress", "completed"
    private String deadline;
    private String priority; // "low", "medium", "high"
    private String createdAt;
    private String updatedAt;

    // Constructor
    public Task(String id, String title, String assignee, String deadline, String priority) {
        this.id = id;
        this.title = title;
        this.assignee = assignee;
        this.status = "pending";
        this.deadline = deadline;
        this.priority = priority;
        this.createdAt = getCurrentTimestamp();
        this.updatedAt = getCurrentTimestamp();
    }

    // Default constructor for JSON deserialization
    public Task() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getStatus() {
        return status;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = getCurrentTimestamp();
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
        this.updatedAt = getCurrentTimestamp();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = getCurrentTimestamp();
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
        this.updatedAt = getCurrentTimestamp();
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.updatedAt = getCurrentTimestamp();
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", assignee='" + assignee + '\'' +
                ", status='" + status + '\'' +
                ", deadline='" + deadline + '\'' +
                ", priority='" + priority + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}