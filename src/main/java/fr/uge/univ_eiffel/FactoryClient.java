package fr.uge.univ_eiffel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class FactoryClient {

    private static final String BASE_URL = "https://legofactory.plade.org";

    private final String email;
    private final String apiKey;

    private FactoryClient(String email, String apiKey) {
        this.email = email;
        this.apiKey = apiKey;
    }

    private String get(String endpoint) throws IOException {
        var url = new URL(BASE_URL + endpoint);
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.addRequestProperty("X-Email", email);
        connection.addRequestProperty("X-Secret-Key", apiKey);

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new IOException("GET " + endpoint + " failed with status " + status);
        }

        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String ping() throws IOException {
        return get("/ping");
    }

    public String catalog() throws IOException {
        return get("/catalog");
    }

    public static FactoryClient fromProperties(String fileName) {
        Properties props = new Properties();

        try (InputStream input = FactoryClient.class.getClassLoader()
                .getResourceAsStream(fileName)) {

            if (input == null) {
                throw new RuntimeException("Properties file '" + fileName + "' not found.");
            }

            props.load(input);

            String email = props.getProperty("USERMAIL");
            String key = props.getProperty("APIKEY");

            if (email == null || key == null) {
                throw new RuntimeException("USERMAIL or APIKEY missing in properties file.");
            }

            return new FactoryClient(email, key);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

