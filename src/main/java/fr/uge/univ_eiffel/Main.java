package fr.uge.univ_eiffel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Main
{
    public static void main(String[] args) throws IOException {

        try {

            File input = new File("src/main/java/fr/uge/univ_eiffel/chest.png");

            BufferedImage source = ImageUtils.imageToBuffered(input);
            BufferedImage destination = new BufferedImage(source.getWidth()/5, source.getHeight()/5, BufferedImage.TYPE_INT_ARGB);

            //EVERYTHING HAPPENS HERE//

            Downscaler.basicConvert(source,destination);

            //EVERYTHING HAPPENED HERE//

            File output = ImageUtils.bufferedToImage("wow", destination);

            if (output == null) {
                System.err.println("Failed to write image to file");
            } else {
                System.out.println("Image successfully converted and saved to: " + output.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
