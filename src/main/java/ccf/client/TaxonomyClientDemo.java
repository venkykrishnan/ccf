package ccf.client;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TaxonomyClientDemo {
    public static void main(String[] args) {
        try {
            // Create client instance
            TaxonomyClient client = new TaxonomyClient("http://localhost:9000");

            // Path to your JSON file
            Path jsonPath = Paths.get("src/main/resources/taxonomy-create.json");

            // Create a new taxonomy
            String taxonomyId = "tax-001"; // You can make this dynamic or read from args
            String createResponse = client.createTaxonomy(taxonomyId, jsonPath);
            System.out.println("Create Response: " + createResponse);

            // Get the created taxonomy
            String getResponse = client.getTaxonomy(taxonomyId);
            System.out.println("Get Response: " + getResponse);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
