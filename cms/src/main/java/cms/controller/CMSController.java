package cms.controller;

import cms.model.FileMetadata;
import cms.model.FileStatus;
import cms.repository.FileMetadataRepository;
import cms.service.CMSService;
import fileserver.service.RemoteFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/cms")
public class CMSController {

    private static final Logger log = LogManager.getLogger(CMSController.class);

    private static final List<String> IMAGE_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/tiff"
    );

    private static final String CONTENT_DIR = "/content";

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private CMSService cmsService;

    @GetMapping("/health")
    public String getHealth() {
        log.info("Health check endpoint accessed.");
        return "The system is now up";
    }

    @GetMapping("/list/all")
    public ResponseEntity<Map<String, Object>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        log.info("Fetching files for page {} with size {}", page, size);

        UUID userId = UUID.fromString("5bf0a08e-5c71-4219-87f3-2e2f0d3c8df8");

        Pageable paging = PageRequest.of(page, size);

        Page<FileMetadata> pageFiles = fileMetadataRepository.findByUserIdAndFileTypeIn(userId, IMAGE_FILE_TYPES, paging);

        Map<String, Object> response = new HashMap<>();
        response.put("files", pageFiles.getContent());
        response.put("currentPage", pageFiles.getNumber());
        response.put("totalItems", pageFiles.getTotalElements());
        response.put("totalPages", pageFiles.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/thumbnail/{fileId}")
    public ResponseEntity<InputStreamResource> getThumbnail(@PathVariable UUID fileId) {
        try {
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findById(fileId);

            if (!fileMetadataOpt.isPresent()) {
                log.error("Thumbnail not found for fileId {}", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            FileMetadata fileMetadata = fileMetadataOpt.get();
            String thumbnailLocation = fileMetadata.getThumbnailLocation();

            if (thumbnailLocation == null || thumbnailLocation.isEmpty()) {
                log.error("Thumbnail location is null or empty for fileId {}", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            File file = cmsService.lookUpForFile(thumbnailLocation);

            if (file == null || !file.exists()) {
                log.error("Thumbnail file does not exist at location {}", thumbnailLocation);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            InputStream fileStream = new FileInputStream(file);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("inline", file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));
        } catch (IOException e) {
            log.error("An error occurred while processing the thumbnail.", e);
            String errorMessage = "An error occurred while processing the thumbnail.";
            ByteArrayInputStream errorStream = new ByteArrayInputStream(errorMessage.getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(errorStream));
        }
    }

    @GetMapping("/image/{fileId}")
    public ResponseEntity<InputStreamResource> getImages(@PathVariable UUID fileId) {
        try {
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findById(fileId);

            if (!fileMetadataOpt.isPresent()) {
                log.error("Image not found for fileId {}", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            FileMetadata fileMetadata = fileMetadataOpt.get();
            String location = fileMetadata.getLocation();

            if (location == null || location.isEmpty()) {
                log.error("Image location is null or empty for fileId {}", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            File file = cmsService.lookUpForFile(location);

            if (file == null || !file.exists()) {
                log.error("Image file does not exist at location {}", location);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            InputStream fileStream = new FileInputStream(file);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("inline", file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));
        } catch (IOException e) {
            log.error("An error occurred while processing the image.", e);
            String errorMessage = "An error occurred while processing the image.";
            ByteArrayInputStream errorStream = new ByteArrayInputStream(errorMessage.getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(errorStream));
        }
    }

    @GetMapping("/stream")
    public ResponseEntity<InputStreamResource> streamFile(@RequestHeader("Location-Id") String locationId) {
        try {
            File file = cmsService.lookUpForFile(locationId);

            if (file == null || !file.exists()) {
                log.error("File not found for locationId {}", locationId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));
            }

            InputStream fileStream = new FileInputStream(file);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));
        } catch (IOException e) {
            log.error("An error occurred while streaming the file.", e);
            String errorMessage = "An error occurred while streaming the file.";
            ByteArrayInputStream errorStream = new ByteArrayInputStream(errorMessage.getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(errorStream));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        if (!cmsService.isValidImage(file)) {
            log.error("Invalid file type or size. File: {}", file.getOriginalFilename());
            return ResponseEntity.badRequest()
                    .body("The content uploaded should only be an image file type and should be lesser than 50MB. Error code: 2001");
        }

        RemoteFile fileInfo = cmsService.storeFile(file);

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename(file.getOriginalFilename());
        fileMetadata.setUserId(UUID.fromString("5bf0a08e-5c71-4219-87f3-2e2f0d3c8df8"));
        fileMetadata.setLocation(fileInfo.getLocation());
        fileMetadata.setFileType(file.getContentType());
        fileMetadata.setStatus(FileStatus.STORED);
        FileMetadata fileMetadataSaved = cmsService.persistMetadata(fileMetadata);

        log.info("File uploaded successfully: {}", fileMetadataSaved.getStatus().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully: " + fileMetadataSaved.getStatus().toString());
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllImagesWithFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Fetching images and files for page {} with size {}", page, size);

        UUID userId = UUID.fromString("5bf0a08e-5c71-4219-87f3-2e2f0d3c8df8");

        Pageable paging = PageRequest.of(page, size);

        Page<FileMetadata> pageFiles = fileMetadataRepository.findByUserIdAndFileTypeIn(userId, IMAGE_FILE_TYPES, paging);

        List<Map<String, Object>> filesWithImages = new ArrayList<>();
        for (FileMetadata metadata : pageFiles.getContent()) {
            Map<String, Object> fileWithImage = new HashMap<>();
            fileWithImage.put("metadata", metadata);

            try {
                File imageFile = cmsService.lookUpForFile(metadata.getLocation());
                if (imageFile != null && imageFile.exists()) {
                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    fileWithImage.put("image", base64Image);
                } else {
                    fileWithImage.put("image", null);
                }
            } catch (IOException e) {
                log.error("An error occurred while reading the image file for fileId {}", metadata.getId(), e);
                fileWithImage.put("image", null);
            }

            filesWithImages.add(fileWithImage);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("files", filesWithImages);
        response.put("currentPage", pageFiles.getNumber());
        response.put("totalItems", pageFiles.getTotalElements());
        response.put("totalPages", pageFiles.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
