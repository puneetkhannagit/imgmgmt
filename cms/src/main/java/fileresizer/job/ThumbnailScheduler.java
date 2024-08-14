package fileresizer.job;

import cms.model.FileMetadata;
import cms.repository.FileMetadataRepository;
import fileresizer.service.FileResizerServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ThumbnailScheduler {

    private static final Logger log = LogManager.getLogger(ThumbnailScheduler.class);

    @Autowired
    private final FileResizerServiceImpl fileResizerService;

    @Autowired
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${thumbnail.size.pixel:100}")
    private int thumbnailSize;

    public ThumbnailScheduler(FileResizerServiceImpl fileResizerService, FileMetadataRepository fileMetadataRepository) {
        this.fileResizerService = fileResizerService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void generateThumbnails() {
        List<FileMetadata> filesToProcess = fetchFilesWithoutThumbnails();
        for (FileMetadata fileMetadata : filesToProcess) {
            processFileForThumbnail(fileMetadata);
        }
    }

    private List<FileMetadata> fetchFilesWithoutThumbnails() {
        return fileMetadataRepository.findByIsThumbnailCreatedFalse();
    }

    private void processFileForThumbnail(FileMetadata fileMetadata) {
        try {
            File imageFile = new File(fileMetadata.getLocation());
            if (imageFile.exists()) {
                File thumbnailFile = createThumbnail(imageFile);
                updateFileMetadataWithThumbnail(fileMetadata, thumbnailFile);
            } else {
                log.warn("File does not exist: {}", imageFile.getAbsolutePath());
            }
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private File createThumbnail(File inputFile) throws IOException {
        BufferedImage originalImage = loadImage(inputFile);
        BufferedImage thumbnailImage = resizeImageToThumbnail(originalImage);
        return saveThumbnailImage(inputFile, thumbnailImage);
    }

    private BufferedImage loadImage(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Could not read image file: " + inputFile);
        }
        return image;
    }

    private BufferedImage resizeImageToThumbnail(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double aspectRatio = (double) width / height;

        int thumbnailWidth = thumbnailSize;
        int thumbnailHeight = (int) (thumbnailSize / aspectRatio);

        if (thumbnailHeight > thumbnailSize) {
            thumbnailHeight = thumbnailSize;
            thumbnailWidth = (int) (thumbnailSize * aspectRatio);
        }

        Image scaledImage = originalImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnailImage = new BufferedImage(thumbnailSize, thumbnailSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnailImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, thumbnailSize, thumbnailSize, null);
        g2d.dispose();

        return thumbnailImage;
    }

    private File saveThumbnailImage(File inputFile, BufferedImage thumbnailImage) throws IOException {
        String thumbnailFilename = generateThumbnailFilename(inputFile);
        File thumbnailFile = new File(inputFile.getParentFile(), thumbnailFilename);
        String fileExtension = getFileExtension(inputFile.getName());

        ImageIO.write(thumbnailImage, fileExtension, thumbnailFile);

        log.info("Thumbnail created: {}", thumbnailFile.getAbsolutePath());
        return thumbnailFile;
    }

    private String generateThumbnailFilename(File inputFile) {
        String originalFilename = inputFile.getName();
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = getFileExtension(originalFilename);
        return baseName + "_thumbnail." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "jpg" : filename.substring(lastDotIndex + 1);
    }

    private void updateFileMetadataWithThumbnail(FileMetadata fileMetadata, File thumbnailFile) {
        fileMetadata.setThumbnailCreated(true);
        fileMetadata.setThumbnailLocation(thumbnailFile.getAbsolutePath());
        fileMetadataRepository.save(fileMetadata);
    }

    private void handleIOException(IOException e) {
        log.error("IOException occurred while processing thumbnail", e);
    }
}
