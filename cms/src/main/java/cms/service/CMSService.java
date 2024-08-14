package cms.service;

import cms.model.FileMetadata;
import fileserver.service.RemoteFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
@Service
public interface CMSService {

    // customer facing
    RemoteFile storeFile(MultipartFile file) throws IOException;

    File lookUpForFile(String locationId);

    List<FileMetadata> getFiles();

    FileMetadata persistMetadata(FileMetadata metadata);
    // this is not customer facing but is used by internal services to process and update files
    FileMetadata updateMetaData(FileMetadata updatedMetaData);
    // this is not customer facing but is used by internal services to process,push to cdn etc
    List<FileMetadata> fetchUnprocessedFiles();
    // make sure if the user is not trying to upload some executables but then if the file types change to any in future, we need to skip this method
    boolean isValidImage(MultipartFile file);
}
