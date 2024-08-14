package fileresizer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class FileResizerServiceImpl implements FileResizerService {

    // we are reading from app.properties , this should be auto reflected without server restarts on chaneg in app.prop
    // use refresh scope and flush the change using actuator
    @Value("${image.resize.to.pixel}")
    private int newWidth;

    public int getNewWidth() {
        return newWidth;
    }

    @Override
    public File resize(File imageFileToResize) {
        if (imageFileToResize == null || !imageFileToResize.exists()) {
            return null;
        }

        try {
            BufferedImage originalImage = ImageIO.read(imageFileToResize);
            if (originalImage == null) {

                return null;
            }

            int newHeight = calculateNewHeight(originalImage.getWidth(), originalImage.getHeight());

            BufferedImage resizedImage = createResizedImage(originalImage, newWidth, newHeight);

            File resizedFile = saveResizedImage(imageFileToResize, resizedImage);

            return resizedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateNewHeight(int originalWidth, int originalHeight) {
        return (originalHeight * newWidth) / originalWidth;
    }

    private BufferedImage createResizedImage(BufferedImage originalImage, int width, int height) {
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    private File saveResizedImage(File originalFile, BufferedImage resizedImage) throws IOException {
        String originalFilename = originalFile.getName();
        String fileExtension = getFileExtension(originalFilename);

        File resizedFile = new File(originalFile.getParent(), "resized_" + originalFilename);
        ImageIO.write(resizedImage, fileExtension, resizedFile);

        return resizedFile;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }
}
