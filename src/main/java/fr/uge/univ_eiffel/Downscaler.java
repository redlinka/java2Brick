package fr.uge.univ_eiffel;

import java.awt.image.BufferedImage;


public class Downscaler {

    public static void basicConvert(BufferedImage source, BufferedImage destination) {
        double widthRatio = (double) destination.getWidth() / source.getWidth();
        double heightRatio = (double) destination.getHeight() / source.getHeight();
        for (int x = 0; x < destination.getWidth(); x++)
            for (int y = 0; y < destination.getHeight(); y++)
                destination.setRGB(x, y, source.getRGB((int) (x / widthRatio), (int) (y / heightRatio)));
    }
}
