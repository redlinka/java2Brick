package fr.uge.univ_eiffel;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;


public class Main
{
    public static void main(String[] args) throws IOException {

        double scaleFactorX = 0.20;
        double scaleFactorY = 0.20;

        try {

            File input = new File("src/main/java/fr/uge/univ_eiffel/chest.png");

            BufferedImage source = ImageUtils.imageToBuffered(input);
            BufferedImage destNeigh = new BufferedImage(
                    (int)(source.getWidth() * scaleFactorX),
                    (int)(source.getHeight() * scaleFactorY),
                    BufferedImage.TYPE_INT_ARGB
            );
            BufferedImage destBili = new BufferedImage(
                    (int)(source.getWidth() * scaleFactorX),
                    (int)(source.getHeight() * scaleFactorY),
                    BufferedImage.TYPE_INT_ARGB
            );
            BufferedImage destBicubic = new BufferedImage(
                    (int)(source.getWidth() * scaleFactorX),
                    (int)(source.getHeight() * scaleFactorY),
                    BufferedImage.TYPE_INT_ARGB
            );

            //EVERYTHING HAPPENS HERE//

            Downscaler.nearestNeighbour(source,destNeigh);
            Downscaler.biLinearInterpolation(source,destBili);
            Downscaler.biCubicInterpolation(source,destBicubic);

            //EVERYTHING HAPPENED HERE//

            File output3 = ImageUtils.bufferedToImage("bicubicInterpolation", destBicubic);
            ImageUtils.bufferedToHexMatrix("hexmatrix", destBicubic);

            if (output3 == null) {
                System.err.println("Failed to write image to file");
            } else {
                System.out.println("Image successfully converted and saved to: " + output3.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // load logins from config.properties
        FactoryClient client = FactoryClient.fromProperties("config.properties");

        // test the connection
        System.out.println("Ping:" + client.ping());

        // get catalog
        System.out.println("Catalog:" + client.catalog());
    }
}
