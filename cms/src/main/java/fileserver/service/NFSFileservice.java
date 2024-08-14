package fileserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class NFSFileservice implements FileService {
    private static final String CONTENT_DIR = "/content";

    /*
     * We're using a temporary folder for file uploads to manage storage efficiently.
     * To avoid having too many files in a single directory, we should organize files
     * into separate folders. Ideally, each user would have their own dedicated folder
     * for their uploads.
     *
     * For better scalability, we could consider using separate NFS (Network File System)
     * servers for storing these files. This approach is similar to how cloud storage
     * solutions like S3 handle file storage, allowing us to reference files by their
     * unique IDs.
     *
     * However, if we're managing this ourselves, it's important to be aware of filesystem
     * limitations. For example:
     * - NTFS (New Technology File System) has a theoretical limit of around 32,767 folders
     *   per directory. In practice, performance and the maximum path length may impose
     *   lower limits.
     * - FAT32 (File Allocation Table 32) has a limit of 65,534 files per directory and a
     *   maximum path length of 260 characters.
     *
     * As the number of users and uploads increases, it's crucial to manage file storage
     * carefully to avoid hitting these filesystem limits and ensure optimal performance.
     */

    @Override
    public RemoteFile save(MultipartFile file) throws IOException {
        File savedFileReference = createDireTempDirectoryAndSaveTheFile(file);
        return new RemoteFile(savedFileReference.getAbsolutePath());

    }



    @Override
    public File retrieveFile(String location) throws IOException {

        File file = new File(location);
        if (!file.exists()) {
            throw new IOException("File not found: " + location);
        }

        return file;
    }





    public static File createDireTempDirectoryAndSaveTheFile(MultipartFile file) throws IOException {
        // Get the current timestamp
        String timestamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());

        // Define the folder name format
        String folderName = "file_" + timestamp;

        // Create the directory structure
        File userFolder = new File(CONTENT_DIR, folderName);

        // Create the folder if it does not exist
        if (!userFolder.exists()) {
            boolean created = userFolder.mkdirs();

        }
        File savedFile = new File(userFolder, file.getOriginalFilename());
        file.transferTo(savedFile);
        return savedFile;
    }
}

