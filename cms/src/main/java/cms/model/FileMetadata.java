package cms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;


import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "FILE_METADATA",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_file_type", columnList = "file_type"),
                @Index(name = "idx_user_file_type", columnList = "user_id, file_type")
        })
public class  FileMetadata {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "user_id", nullable = false)
    private UUID userId;


    @Column(name = "location", nullable = false)
    private String location;


    @Column(name = "thumbnail_location")
    private String thumbnailLocation="";


    @Column(name = "file_type", nullable = false)
    private String fileType;

    public String getThumbnailLocation() {
        return thumbnailLocation;
    }

    public void setThumbnailLocation(String thumbnailLocation) {
        this.thumbnailLocation = thumbnailLocation;
    }

    public Boolean getThumbnailCreated() {
        return isThumbnailCreated;
    }

    public void setThumbnailCreated(Boolean thumbnailCreated) {
        isThumbnailCreated = thumbnailCreated;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_stored", nullable = false, updatable = false)
    private Date dateStored;

    public boolean isThumbnailCreated() {
        return isThumbnailCreated;
    }

    public void setThumbnailCreated(boolean thumbnailCreated) {
        isThumbnailCreated = thumbnailCreated;
    }

    @JsonIgnore
    @Column(name = "is_thumbnail_created", nullable = false)
    private Boolean isThumbnailCreated = Boolean.FALSE;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public Date getDateStored() {
        return dateStored;
    }

    public void setDateStored(Date dateStored) {
        this.dateStored = dateStored;
    }

    // Constructors
    public FileMetadata() {
    }

    public FileMetadata(String filename, UUID userId, String location, String fileType, FileStatus status) {
        this.filename = filename;
        this.userId = userId;
        this.location = location;
        this.fileType = fileType;
        this.status = status;
    }
}
