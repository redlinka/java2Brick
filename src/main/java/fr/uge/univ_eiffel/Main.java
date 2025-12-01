package fr.uge.univ_eiffel;

import fr.uge.univ_eiffel.image_processing.downscalers.BicubicInterpolator;

public class Main {
    public static void main(String[] args) throws Exception {

        App app = App.initialize("config.properties");
        app.run("mcdo.png", new BicubicInterpolator(), "french-downscaled",256,192,2000, 1);
    }
}

