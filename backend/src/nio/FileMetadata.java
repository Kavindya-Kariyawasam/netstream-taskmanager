package nio;

public class FileMetadata {
    private final String fieldId;
    private final String originalName;
    private final String storedPath;
    private final long size;
    private final String taskId;
    private final String description;

    public FileMetadata(String fieldId, String originalName, String storedPath, long size, String taskId, String description) {
        this.fieldId = fieldId;
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.size = size;
        this.taskId = taskId;
        this.description = description;
    }

    public String getFieldId() {return fieldId;}
    public String getOriginalName() {return originalName;}
    public String getStoredPath() {return storedPath;}
    public long getSize() {return size;}
    public String getTaskId() {return taskId;}
    public String getDescription() {return description;}


}
