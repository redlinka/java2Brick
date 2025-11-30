package fr.uge.univ_eiffel.downscalers;

import java.awt.image.BufferedImage;

public interface Downscaler {
    default int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }
    // decomposes a 32 bit number into the ARGB components of the pixel
    default int[] extractARGB(int argb) {
        return new int[] {
                (argb >> 24) & 0xFF,  // a
                (argb >> 16) & 0xFF,  // r
                (argb >> 8) & 0xFF,   // g
                argb & 0xFF           // b
        };
    }

    // recombines the argb values into a 32 bit integer
    default int combineARGB(int[] argb) {
        return (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
    }

    void downscale(BufferedImage source, BufferedImage destination);
}
