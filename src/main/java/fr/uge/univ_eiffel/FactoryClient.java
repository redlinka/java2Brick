package fr.uge.univ_eiffel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    private final Gson gson = new Gson();

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

    public String post(String endpoint, String jsonBody) throws IOException {
        var url = new URL(BASE_URL + endpoint);
        var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.addRequestProperty("X-Email", email);
        connection.addRequestProperty("X-Secret-Key", apiKey);
        connection.addRequestProperty("Content-Type", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            connection.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        if (status != 200) {
            String errorMsg = "POST " + endpoint + " failed with status " + status;
            try {
                String errContent = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                errorMsg += " Body: " + errContent;
            } catch (Exception e) {}
            throw new IOException(errorMsg);
        }

        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String ping() throws IOException {
        return get("/ping");
    }

    public JsonObject catalog() throws IOException {
        return JsonParser.parseString(get("/catalog")).getAsJsonObject();
    }

    public JsonObject production() throws IOException {
        return JsonParser.parseString(get("/production")).getAsJsonObject();
    }

    public String signaturePublicKey() throws IOException {
        return get("/signature-public-key");
    }

    public double balance() throws IOException {
        String json = get("/billing/balance");
        return JsonParser.parseString(json).getAsJsonObject().get("amount").getAsDouble();
    }

    public JsonObject billingChallenge() throws IOException {
        return JsonParser.parseString(get("/billing/challenge")).getAsJsonObject();
    }

    public boolean verify(String name, String serial, String certificate) {
        JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("serial", serial);
        payload.addProperty("certificate", certificate);

        try {
            post("/verify", gson.toJson(payload));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void billingChallengeAnswer(String dataPrefix, String hashPrefix, String answer) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("data_prefix", dataPrefix);
        payload.addProperty("hash_prefix", hashPrefix);
        payload.addProperty("answer", answer);

        post("/billing/challenge-answer", gson.toJson(payload));
    }

    public JsonObject requestQuote(JsonObject bricksRequest) throws IOException {
        String json = post("/ordering/quote-request", gson.toJson(bricksRequest));
        return JsonParser.parseString(json).getAsJsonObject();
    }
    public void confirmOrder(String quoteId) throws IOException {
        post("/ordering/order/" + quoteId, "");
    }

    public JsonObject deliver(String quoteId) throws IOException {
        String json = get("/ordering/deliver/" + quoteId);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public static FactoryClient makeFromProps(String fileName) {
        Properties props = new Properties();

        try (InputStream input = FactoryClient.class.getClassLoader()
                .getResourceAsStream(fileName)) {

            if (input == null) {
                throw new RuntimeException("Properties file '" + fileName + "' not found.");
            }

            props.load(input);

            String email = props.getProperty("USER_MAIL");
            String key = props.getProperty("API_KEY");

            if (email == null || key == null) {
                throw new RuntimeException("USER_MAIL or API_KEY missing in properties file.");
            }

            return new FactoryClient(email, key);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

