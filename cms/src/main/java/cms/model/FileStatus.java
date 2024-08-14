package cms.model;



public enum FileStatus {
    STORED,               // The file is stored and hasn't been processed yet
    PROCESSED,
    IS_BEING_PROCESSED, // The file has been in processed
    DELETED_LOGICALLY,    // The file has been marked as deleted but not physically removed
    PURGED_PERMANENTLY    // The file has been permanently removed from the system
}
