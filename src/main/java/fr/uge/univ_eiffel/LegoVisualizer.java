package fr.uge.univ_eiffel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class LegoVisualizer {

    public static void main(String[] args) throws IOException {

        String filename = "tiled_quadtree_image.txt";
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        int maxX = 0;
        int maxY = 0;

        java.util.List<String[]> squareBricks = new java.util.ArrayList<>();

        while ((line = reader.readLine()) != null) {

            if (line.isBlank()){
                continue;
            }
            String[] parts = line.split(",");
            String[] brickName = parts[0].split("/");
            String[] dims = brickName[0].split("-");

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int w = Integer.parseInt(dims[0]);
            int h = Integer.parseInt(dims[1]);

            maxX = Math.max(maxX, x + w);
            maxY = Math.max(maxY, y + h);
            squareBricks.add(new String[]{dims[0],dims[1],brickName[1], parts[1], parts[2]});
        }
        reader.close();

        int targetWidth = 5000;
        int scale = Math.max(1, targetWidth / maxX);
        BufferedImage image = new BufferedImage(maxX * scale, maxY * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // draw
        for (String[] brick : squareBricks) {
            int w = Integer.parseInt(brick[0]);
            int h = Integer.parseInt(brick[1]);
            int x = Integer.parseInt(brick[3]);
            int y = Integer.parseInt(brick[4]);
            Color brickColor = Color.decode("#" + brick[2]);
            Color brickEdge = new Color(0, 0, 0, 127);
            Color studColor = new Color(0, 0, 0, 25);
            Color studEdge = new Color(0, 0, 0, 50);


            g.setColor(brickColor);
            g.fillRect(x * scale, y * scale, w * scale, h * scale);
            g.setColor(brickEdge);
            g.drawRect(x * scale, y * scale, w * scale, h * scale);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {

                    int studX = (x + i) * scale;
                    int studY = (y + j) * scale;

                    int padding = scale / 5;

                    g.setColor(studColor);
                    g.fillOval(studX + padding,
                            studY + padding,
                            scale - (padding * 2),
                            scale - (padding * 2)
                    );
                    g.setColor(studColor);
                    g.fillOval(studX + padding,
                            studY + padding,
                            scale - (padding * 2),
                            scale - (padding * 2)
                    );
                    g.setColor(studEdge);
                    g.drawOval(studX + padding,
                            studY + padding,
                            scale - (padding * 2),
                            scale - (padding * 2)
                    );
                }
            }
        }

        g.dispose();

        // save file
        ImageIO.write(image, "png", new File("visualized.png"));
        System.out.println("output succesfully saved at visualized.png");
    }
}
