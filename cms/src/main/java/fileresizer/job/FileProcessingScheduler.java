package fileresizer.job;

import cms.model.FileMetadata;
import cms.model.FileStatus;
import cms.repository.FileMetadataRepository;
import fileresizer.service.FileResizerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Arrays;

@Component
public class FileProcessingScheduler {

    private static final Logger log = LogManager.getLogger(FileProcessingScheduler.class);

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileResizerService fileResizerService;

    @Value("${image.resize.to.pixel}")
    private int newWidth;

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("gif", "jpeg", "png", "jpg"));

    @Scheduled(fixedRateString = "${image.resize.interval}")
    public void processStoredFiles() {
        List<FileMetadata> storedFiles = getStoredFiles();

        if (storedFiles.isEmpty()) {
            log.info("No files with status STORED to process.");
            return;
        }

        for (FileMetadata fileMetadata : storedFiles) {
            if (isImageFile(fileMetadata)) {
                processFile(fileMetadata);
            } else {
                log.info("File {} is not an image file and will be skipped.", fileMetadata.getFilename());
            }
        }
    }

    private List<FileMetadata> getStoredFiles() {
        return fileMetadataRepository.findByStatus(FileStatus.STORED);
    }

    private boolean isImageFile(FileMetadata fileMetadata) {
        String fileExtension = getFileExtension(fileMetadata.getFilename()).toLowerCase();
        return IMAGE_EXTENSIONS.contains(fileExtension);
    }

    private void processFile(FileMetadata fileMetadata) {
        UUID fileId = fileMetadata.getId();
        boolean statusUpdated = updateStatusToBeingProcessed(fileId, FileStatus.STORED, FileStatus.IS_BEING_PROCESSED);

        if (statusUpdated) {
            try {
                File originalFile = new File(fileMetadata.getLocation());
                File resizedFile = resizeFile(originalFile);

                if (resizedFile != null && resizedFile.exists()) {
                    updateMetadataAndCleanUp(fileMetadata, resizedFile, originalFile);
                }
            } catch (Exception e) {
                handleProcessingFailure(fileMetadata, e);
            }
        } else {
            log.info("File {} is already being processed by another thread.", fileId);
        }
    }

    private File resizeFile(File originalFile) throws Exception {
        return fileResizerService.resize(originalFile);
    }

    @Transactional
    private void updateMetadataAndCleanUp(FileMetadata fileMetadata, File resizedFile, File originalFile) {
        fileMetadata.setLocation(resizedFile.getAbsolutePath());
        fileMetadata.setStatus(FileStatus.PROCESSED);
        fileMetadataRepository.save(fileMetadata);

        if (!originalFile.delete()) {
            log.error("Failed to delete original file: {}", originalFile.getAbsolutePath());
        }
    }

    @Transactional
    private void handleProcessingFailure(FileMetadata fileMetadata, Exception e) {
        log.error("Failed to process file = {}", fileMetadata.getFilename(), e);

        fileMetadata.setStatus(FileStatus.STORED);
        fileMetadataRepository.save(fileMetadata);
    }

    @Transactional
    private boolean updateStatusToBeingProcessed(UUID fileId, FileStatus currentStatus, FileStatus newStatus) {
        int rowsUpdated = fileMetadataRepository.updateStatus(fileId, currentStatus, newStatus);
        return rowsUpdated > 0;
    }

    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "jpg"; // Default to jpg if no extension is found
        }

        return filename.substring(lastDotIndex + 1);
    }
}
