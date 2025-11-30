package fr.uge.univ_eiffel.downscalers;

import java.awt.image.BufferedImage;

public class BilinearInterpolator implements Downscaler {

    // uses the interpolation formula to calculate the values of the destination pixel
    private int biLinearInterpolate(int c00, int c10, int c01, int c11, double dx, double dy) {
        double interpolated =
                (1 - dx) * (1 - dy) * c00
                        + dx * (1 - dy) * c10
                        + (1 - dx) * dy * c01
                        + dx * dy * c11;
        // making sure the final value is between 0 and 255 to get a valid value
        return clamp((int) interpolated, 255);
    }

    /**
     * bilinear interplation gives a smoother result on average,
     * it takes into account the 4 neighboring pixels to the destination one and averages with coefficients their color values.
     */

    public void downscale(BufferedImage source, BufferedImage destination) {
        double widthRatio = (double) destination.getWidth() / source.getWidth();
        double heightRatio = (double) destination.getHeight() / source.getHeight();

        for (int x = 0; x < destination.getWidth(); x++) {
            for (int y = 0; y < destination.getHeight(); y++) {

                double srcX = x / widthRatio;
                double srcY = y / heightRatio;
                int x0 = clamp((int) Math.floor(srcX), source.getWidth() - 1); // top-left
                int y0 = clamp((int) Math.floor(srcY), source.getHeight() - 1); // top-right
                int x1 = clamp(x0 + 1, source.getWidth() - 1); // bottom-left
                int y1 = clamp( y0 + 1, source.getHeight() - 1); // bottom-right
                double dx = srcX - x0; // value btw 0 and 1 indicating the coords of the dest pixel in comparison to its neighbors
                double dy = srcY - y0; // value btw 0 and 1 indicating the coords of the dest pixel in comparison to its neighbors

                int[][] colors = new int[4][];
                // we get the ARGB values of the 4 neighbours
                colors[0] = extractARGB(source.getRGB(x0, y0)); // [a,r,g,b] top-left
                colors[1] = extractARGB(source.getRGB(x1, y0)); // [a,r,g,b] top-right
                colors[2] = extractARGB(source.getRGB(x0, y1)); // [a,r,g,b] bottom-left
                colors[3] = extractARGB(source.getRGB(x1, y1)); // [a,r,g,b] bottom-right

                int[] result = new int[4];
                for (int i = 0; i < 4; i++) {
                    // calculates the ARGB values of the destination pixel
                    result[i] = biLinearInterpolate(colors[0][i], colors[1][i], colors[2][i], colors[3][i], dx, dy);
                }
                destination.setRGB(x, y, combineARGB(result));
            }
        }
    }
}
