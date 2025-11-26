package fr.uge.univ_eiffel;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


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

            File output = ImageUtils.bufferedToImage("bicubicInterpolation", destBicubic);
            PrintWriter outputHex = ImageUtils.bufferedToHexMatrix("hexmatrix", destBicubic);

            if (output == null || outputHex == null) {
                System.err.println("Failed to write image or hex to file");
            } else {
                System.out.println("Image and hex successfully converted and saved to: " + output.getAbsolutePath());
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
