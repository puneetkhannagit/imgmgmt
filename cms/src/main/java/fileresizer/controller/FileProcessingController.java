package fileresizer.controller;

import fileresizer.job.FileProcessingScheduler;
import fileresizer.job.ThumbnailScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resize")
public class FileProcessingController {

    @Autowired
    private FileProcessingScheduler fileProcessingScheduler;

    @Autowired
    private ThumbnailScheduler thumbnailScheduler;

    @GetMapping("/images")
    public String processFilesForResizingImages() {
        fileProcessingScheduler.processStoredFiles();
        return "File processing triggered.";
    }

    @GetMapping("/thumbnails")
    public String processFilesForThumbnails() {
        thumbnailScheduler.generateThumbnails();
        return "File processing triggered.";
    }
}
