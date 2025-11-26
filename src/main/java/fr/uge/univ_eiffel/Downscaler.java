package fr.uge.univ_eiffel;

import java.awt.image.BufferedImage;

public class Downscaler {

    /**
     * here is a rescaling method that uses the pixel from the source that is the closest to the one in the destination
     * result can sometimes result to a noisy or grainy result
     */

    public static void nearestNeighbour(BufferedImage source, BufferedImage destination) {
        double widthRatio = (double) destination.getWidth() / source.getWidth();
        double heightRatio = (double) destination.getHeight() / source.getHeight();
        for (int x = 0; x < destination.getWidth(); x++) {
            for (int y = 0; y < destination.getHeight(); y++) {
                destination.setRGB(x, y, source.getRGB((int) (x / widthRatio), (int) (y / heightRatio)));
            }
        }
    }

    // decomposes a 32 bit number into the ARGB components of the pixel
    private static int[] extractARGB(int argb) {
        return new int[] {
                (argb >> 24) & 0xFF,  // a
                (argb >> 16) & 0xFF,  // r
                (argb >> 8) & 0xFF,   // g
                argb & 0xFF           // b
        };
    }

    // clamps a value between 0 and max
    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    // uses the interpolation formula to calculate the values of the destination pixel
    private static int biLinearInterpolate(int c00, int c10, int c01, int c11, double dx, double dy) {
        double interpolated =
                (1 - dx) * (1 - dy) * c00
                + dx * (1 - dy) * c10
                + (1 - dx) * dy * c01
                + dx * dy * c11;
        // making sure the final value is between 0 and 255 to get a valid value
        return clamp((int) interpolated, 255);
    }

    // recombines the argb values into a 32 bit integer
    private static int combineARGB(int[] argb) {
        return (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
    }

    /**
     * bilinear interplation gives a smoother result on average,
     * it takes into account the 4 neighboring pixels to the destination one and averages with coefficients their color values.
     */

    public static void biLinearInterpolation(BufferedImage source, BufferedImage destination) {
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

    // uses the bicubic interpolation formula to determine the best ARGB value of a given destination pixel during rescaling
    private static double biCubicInterpolate(int c0,int c1, int c2, int c3, double d) {
        double slope1 = (c2 - c0)/2;
        double slope2 = (c3 - c1)/2;
        return (2 * Math.pow(d, 3) - 3 * Math.pow(d, 2) + 1) * c1
                + (Math.pow(d, 3) - 2 * Math.pow(d, 2) + d) * slope1
                + (-2 * Math.pow(d, 3) + 3 * Math.pow(d, 2)) * c2
                + (Math.pow(d, 3) - Math.pow(d, 2)) * slope2;
    }

    // implementation of bicubic interpolation rescaling, the difference with linear interpolation is really noceable at smaller or bigger scales
    // it tends to become very slow at bigger scales
    public static void biCubicInterpolation(BufferedImage source, BufferedImage destination) {
        double widthRatio = (double) destination.getWidth() / source.getWidth();
        double heightRatio = (double) destination.getHeight() / source.getHeight();
        
        for (int x = 0; x < destination.getWidth(); x++) {
            for (int y = 0; y < destination.getHeight(); y++) {

                double srcX = x / widthRatio;
                double srcY = y / heightRatio;

                int[] xPoints = new int[4];
                int[] yPoints = new int[4];
                for (int i = -1; i <= 2; i++) {
                    int index = i + 1;
                    xPoints[index] = clamp((int) Math.floor(srcX) + i, source.getWidth() - 1);
                    yPoints[index] = clamp((int) Math.floor(srcY) + i, source.getHeight() - 1);
                }

                double dx = srcX - xPoints[1]; // value btw 0 and 1 indicating the coords of the dest pixel in comparison to its neighbors
                double dy = srcY - yPoints[1]; // value btw 0 and 1 indicating the coords of the dest pixel in comparison to its neighbors

                int[][][] colors = new int[4][4][4];
                // we get the ARGB values of the 16 neighbours and put them in a neat 3D matrix (4 * 4 * nARGB)
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        colors[i][j] = extractARGB(source.getRGB(xPoints[j], yPoints[i]));
                    }
                }

                // we do the 1D interpolation twice to get the 2D version
                int[] passY = new int[4];
                for (int j = 0; j < 4; j++) {
                    int[] passX = new int[4];
                    for (int i = 0; i < 4; i++) {
                        passX[i] = (int) biCubicInterpolate(colors[i][0][j], colors[i][1][j], colors[i][2][j], colors[i][3][j], dx);
                    }
                    passY[j] = clamp((int) biCubicInterpolate(passX[0], passX[1], passX[2], passX[3], dy), 255);
                }
                destination.setRGB(x, y, combineARGB(passY));
            }
        }
    }
}
