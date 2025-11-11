package fr.uge.univ_eiffel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static BufferedImage imageToBuffered(File input) throws IOException {

        if (!input.exists()) {
            throw new IOException("File does not exist: " + input.getAbsolutePath());
        }
        BufferedImage img = ImageIO.read(input);

        if (img == null) {
            throw new IOException("Failed to read image from file: " + input.getAbsolutePath());
        } else {
            return img;
        }

    }

    public static File bufferedToImage(String name, BufferedImage img) throws IOException {

        if (name == null || name.isEmpty() || name == "null") {
            throw new IllegalArgumentException("name cannot be empty or null");
        }
        File outputFile = new File(name + ".png");

        if (ImageIO.write(img,"png",outputFile)) {
            return outputFile;
        } else {
            throw new IOException("Failed to write image to file: " + outputFile.getAbsolutePath());
        }
    }
}
