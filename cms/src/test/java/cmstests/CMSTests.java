package cmstests;

import cms.controller.CMSController;
import cms.model.FileMetadata;
import cms.model.FileStatus;
import cms.repository.FileMetadataRepository;
import cms.service.CMSService;
import fileserver.service.RemoteFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
public class CMSTests {

    @InjectMocks
    private CMSController cmsController;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private CMSService cmsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private FileMetadata createFileMetadata(UUID fileId, String location, String fileType) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(fileId);
        fileMetadata.setFilename("test.jpg");
        fileMetadata.setLocation(location);
        fileMetadata.setThumbnailLocation("/content/image_thumbnail.jpg");
        fileMetadata.setFileType(fileType);
        fileMetadata.setStatus(FileStatus.STORED);
        return fileMetadata;
    }

    private ResponseEntity<String> assertResponseStatusAndBody(ResponseEntity<String> response, HttpStatus status, String expectedBody) {
        assertEquals(status, response.getStatusCode());
        assertTrue(response.getBody().contains(expectedBody));
        return response;
    }

    private void verifyFileMetadataPage(Page<FileMetadata> pageFiles, int expectedSize, int expectedTotalPages, int expectedCurrentPage) {
        assertEquals(expectedSize, pageFiles.getContent().size());
        assertEquals(expectedTotalPages, pageFiles.getTotalPages());
        assertEquals(expectedCurrentPage, pageFiles.getNumber());
    }

    @Test
    public void testGetHealth() {
        String response = cmsController.getHealth();

        assertEquals("The system is now up", response);
    }



    @Test
    public void testGetImages() throws IOException {
        UUID fileId = UUID.randomUUID();

        // Load the test.png file from resources
        ClassPathResource resource = new ClassPathResource("test.png");
        File file = resource.getFile();
        byte[] imageBytes = Files.readAllBytes(file.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Prepare file metadata
        FileMetadata fileMetadata = createFileMetadata(fileId, "/content/file.JPG", "image/JPG");

        // Mock the file retrieval
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(fileMetadata));
        when(cmsService.lookUpForFile("/content/file.JPG")).thenReturn(file);

        // Invoke the controller method
        ResponseEntity<InputStreamResource> response = cmsController.getImages(fileId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType()); //

        // Read and verify the image content
        InputStreamResource inputStreamResource = response.getBody();
        assertNotNull(inputStreamResource);

        try (InputStream inputStream = inputStreamResource.getInputStream()) {
            byte[] downloadedContent = inputStream.readAllBytes();
            assertArrayEquals(imageBytes, downloadedContent);
        }
    }


    @Test
    public void testUploadFile() throws IOException {
        // Load the test.png file from resources
        ClassPathResource resource = new ClassPathResource("test.png");
        File file = resource.getFile();
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Create a MockMultipartFile using the test.png file
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", fileContent);

        // Assume RemoteFile has a constructor that takes a String location
        RemoteFile remoteFile = new RemoteFile("/content/test.png");

        when(cmsService.isValidImage(any())).thenReturn(true);
        when(cmsService.storeFile(any())).thenReturn(remoteFile);

        FileMetadata fileMetadata = createFileMetadata(UUID.randomUUID(), "/content/test.png", "image/png");
        when(cmsService.persistMetadata(any(FileMetadata.class))).thenReturn(fileMetadata);

        ResponseEntity<String> response = cmsController.uploadFile(multipartFile);
        assertResponseStatusAndBody(response, HttpStatus.CREATED, "File uploaded successfully");
    }



}
