package ccf.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ccf.domain.standard.Taxonomy.TaxonomyCreate;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public class TaxonomyClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public TaxonomyClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String createTaxonomy(String taxonomyId, Path jsonFilePath) throws IOException, InterruptedException {
        // Read the JSON file
        TaxonomyCreate taxonomyCreate = objectMapper.readValue(jsonFilePath.toFile(), TaxonomyCreate.class);
        
        // Convert the object back to JSON string for the request
        String requestBody = objectMapper.writeValueAsString(taxonomyCreate);

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/taxonomy/" + taxonomyId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send request and get response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to create taxonomy. Status: " + response.statusCode() + 
                                    ", Body: " + response.body());
        }

        return response.body();
    }

    public String getTaxonomy(String taxonomyId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/taxonomy/" + taxonomyId))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get taxonomy. Status: " + response.statusCode() + 
                                    ", Body: " + response.body());
        }

        return response.body();
    }
}
