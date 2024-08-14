package fileresizer.service;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public interface FileResizerService {

    File resize(File imageFileToResize);

}
