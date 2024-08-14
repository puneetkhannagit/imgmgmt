package fileserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@Service
public interface FileService {

    RemoteFile save(MultipartFile file) throws IOException;



    File retrieveFile(String location) throws IOException;

}
