package fr.uge.univ_eiffel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

public class InventoryManager {

    private Connection connection;

    private InventoryManager(String url, String user, String password) throws Exception {
        connection = DriverManager.getConnection(url, user, password);
    }


    /**
     * les prix unitaires sont calculés à partir de la formule du serveur Go :
     *     prix = UnitPrice * (PriceDecreaseFactor)^(log2(largeur*hauteur))
     *
     * C'est pour éviter des milliers d'appels API lents tout en fournissant au programme
     * C des prix cohérents. Meme si les constantes unitPrice changent côté serveur Go, la formule
     * reste valide car elle ne sert qu'a l'optimisation du prix dans la partie C.
     */
    public static double computeUnitPrice(int width, int height) {
        double unitPrice = 0.01;
        double factor = 0.9;
        int area = width * height;

        double price = unitPrice * Math.pow(factor, Math.log(area) / Math.log(2));
        return price;
    }

    //this function will setup the catalog of an empty database, or will update it to its latest version, its takes a few seconds to run
    public void updateCatalog(FactoryClient fc) throws Exception {

        JsonObject cat = fc.catalog();
        JsonArray blocks = cat.getAsJsonArray("blocks");
        JsonArray colors = cat.getAsJsonArray("colors");

        String query = "INSERT INTO catalog (width, height, holes, name, color_hex, unit_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement insertStmt = connection.prepareStatement(query)) {

            for (JsonElement dim : blocks) {
                String[] parts = dim.getAsString().split("-");
                int w = Integer.parseInt(parts[0]);
                int h = Integer.parseInt(parts[1]);
                String holes = "-1";
                if (parts.length == 3) {
                    holes = parts[2];
                }

                for (JsonElement c : colors) {
                    JsonObject color = c.getAsJsonObject();
                    String name = color.get("name").getAsString();
                    String hex = color.get("hex_code").getAsString();

                    try {
                        insertStmt.setInt(1, w);
                        insertStmt.setInt(2, h);
                        insertStmt.setString(3, holes);
                        insertStmt.setString(4, name);
                        insertStmt.setString(5, hex);
                        insertStmt.setDouble(6, computeUnitPrice(w, h));
                        insertStmt.executeUpdate();
                    } catch (SQLException e) {
                        // dups are ignored, let DB handle it because it's faster
                        if (e.getErrorCode() != 1062) {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportCatalog(String name) throws Exception {
        String query = "SELECT width, height, holes, color_hex, unit_price FROM catalog";
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(query);

        // we count the number of rows, which will be placed on top of the catalog txt file
        result.last();
        int rowCount = result.getRow();
        result.beforeFirst();

        try (PrintWriter writer = new PrintWriter(new PrintWriter(name + ".txt"))) {
            writer.println(rowCount); // first line: number of rows

            while (result.next()) {

                int width = result.getInt("width");
                int height = result.getInt("height");
                String holes = result.getString("holes");
                String hex = result.getString("color_hex");
                double price = result.getDouble("unit_price");
                int stock = 1000;

                writer.printf("%d,%d,%s,%s,%.5f,%d%n", width, height, holes, hex, price, stock);
            }
        }
        System.out.println("Catalog exported to " + name + ".txt");
    }
    // load logins from config.properties
    public static InventoryManager makeFromProps(String file) {
        Properties props = new Properties();

        try (InputStream input = InventoryManager.class.getClassLoader()
                .getResourceAsStream(file)) {

            if (input == null) {
                throw new RuntimeException("Properties file '" + file + "' not found.");
            }

            props.load(input);

            String url = props.getProperty("DB_URL");
            String user = props.getProperty("DB_USER");
            String password = props.getProperty("DB_PASSWORD");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("One of the logins is missing or incorrect in properties file.");
            }

            return new InventoryManager(url, user, password);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
}
