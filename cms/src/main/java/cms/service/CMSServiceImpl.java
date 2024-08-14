package cms.service;

import cms.model.FileMetadata;
import cms.model.FileStatus;
import cms.repository.FileMetadataRepository;
import cms.service.exception.InvalidFileException;
import cms.service.exception.InvalidFileTypeException;
import fileserver.service.RemoteFile;
import fileserver.service.NFSFileservice;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class CMSServiceImpl implements CMSService {

    @Value("${spring.servlet.multipart.max-request-size}")
    private int maxRequestSize;



    @Value("${file.extensions.images}")
    private String imageExtensions;

    @Value("${file.extensions.executable}")
    private String executableExtensions;

    private Set<String> IMAGE_EXTENSIONS;
    private Set<String> EXECUTABLE_EXTENSIONS;

    @PostConstruct
    public void init() {
        IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(imageExtensions.split(",")));
        EXECUTABLE_EXTENSIONS = new HashSet<>(Arrays.asList(executableExtensions.split(",")));
    }
    @Autowired
   private NFSFileservice nfsFileservice;


    @Autowired
    private FileMetadataRepository fileMetadataRepository;


    @Override
    public RemoteFile storeFile(MultipartFile file) throws IOException {
        return nfsFileservice.save(file);
    }

    @Override
    public File lookUpForFile(String locationId) {
        try {
            return nfsFileservice.retrieveFile(locationId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FileMetadata> getFiles() {
        // currently return all the files but later we have to return the files back which the token from the security context has the access to
        return fileMetadataRepository.findAll();
    }

    @Override
    public FileMetadata persistMetadata(FileMetadata metadata) {
        return fileMetadataRepository.save(metadata);
    }

    @Override
    public FileMetadata updateMetaData(FileMetadata updatedMetaData) {
        return null;
    }

    @Override
    public List<FileMetadata> fetchUnprocessedFiles() {
        return fileMetadataRepository.findByStatus(FileStatus.STORED);
    }

    public boolean isValidImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty.");
        }

        checkFileSize(file.getSize());
        String fileExtension = getFileExtension(file.getOriginalFilename());
        validateFileType(fileExtension);

        return true;
    }

    private void checkFileSize(long fileSize) {
        if (fileSize > maxRequestSize) {
            throw new InvalidFileException("File size exceeds the 50MB limit.");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new InvalidFileException("File name is missing or invalid.");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private void validateFileType(String fileExtension) {
        if (!IMAGE_EXTENSIONS.contains(fileExtension)) {
            throw new InvalidFileException("Invalid file type: " + fileExtension);
        }
        if (EXECUTABLE_EXTENSIONS.contains(fileExtension)) {
            throw new InvalidFileException("Executable files are not allowed: " + fileExtension);
        }
    }
    public ResponseEntity<InputStreamResource> getFileStreamById(UUID fileId) {
        Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findById(fileId);

        if (fileMetadataOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        FileMetadata fileMetadata = fileMetadataOpt.get();
        String location = fileMetadata.getLocation();

        try {
            // Retrieve the File object
            File file = nfsFileservice.retrieveFile(location);

            // Convert File to InputStream
            InputStream fileStream = new FileInputStream(file);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileMetadata.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving file", e);
        }
    }
}
