package fr.uge.univ_eiffel;

import fr.uge.univ_eiffel.downscalers.BicubicInterpolator;
import fr.uge.univ_eiffel.downscalers.BilinearInterpolator;
import fr.uge.univ_eiffel.downscalers.NearestNeighbour;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;


public class Main
{
    public static void main(String[] args) throws Exception {

        int pixelW = 500;
        int pixelH = 500;
        NearestNeighbour nearest = new NearestNeighbour();
        BilinearInterpolator bilinear = new BilinearInterpolator();
        BicubicInterpolator bicubic = new BicubicInterpolator();

        try {

            File input = new File("test_imgs/original-image.jpg");

            BufferedImage source = ImageUtils.imageToBuffered(input);

            BufferedImage destNeigh = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_ARGB);
            BufferedImage destBili = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_ARGB);
            BufferedImage destBicubic = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_ARGB);

            //EVERYTHING HAPPENS HERE//

            nearest.downscale(source,destNeigh);
            bilinear.downscale(source,destBili);
            bicubic.downscale(source,destBicubic);

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
        FactoryClient client = FactoryClient.makeFromProps("config.properties");

        // test the connection
        System.out.println("Ping:" + client.ping());

        // get catalog
        client.catalog();

        // test inventoryManager
        InventoryManager inventoryManager = InventoryManager.makeFromProps("config.properties");
        //inventoryManager.updateCatalog(client);
        inventoryManager.exportCatalog("catalog");
        inventoryManager.close();
    }
}
