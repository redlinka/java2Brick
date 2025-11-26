package fr.uge.univ_eiffel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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

    public static void bufferedToHexMatrix(String name, BufferedImage img) throws IOException {
        try (PrintWriter writer = new PrintWriter(name + ".txt", "UTF-8")) {
            int width = img.getWidth();
            int height = img.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y) & 0xFFFFFF; // keep only RGB
                    writer.printf("%06X", rgb);

                    if (x < width - 1) writer.print(" ");
                }
                writer.println();
            }
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
