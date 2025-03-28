package ccf.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.http.HttpResponses;
import ccf.application.TaxonomyEntity;
import ccf.domain.standard.Taxonomy;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/taxonomy")
public class TaxonomyEndpoint {
    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(TaxonomyEndpoint.class);

    public TaxonomyEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/{taxonomyId}")
    public CompletionStage<Taxonomy> get(String taxonomyId) {
        CCFLog.debug(logger, "Getting taxonomy",
                Map.of("taxonomyId", taxonomyId));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::getTaxonomy)
                .invokeAsync();
    }

    @Post("/{taxonomyId}")
    public CompletionStage<HttpResponse> createTaxonomy(String taxonomyId, Taxonomy.TaxonomyCreate taxonomy) {
        CCFLog.debug(logger, "Creating taxonomy",
                Map.of("taxonomyId", taxonomyId, "taxonomy", taxonomy.toString()));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::createTaxonomy)
                .invokeAsync(taxonomy)
                .thenApply(createTaxonomyResult ->
                    switch (createTaxonomyResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.CreateFailed e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

  public CompletionStage<HttpResponse> removeTaxonomy(String taxonomyId) {
    CCFLog.debug(logger, "Removing taxonomy",
                Map.of("taxonomyId", taxonomyId));
    return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::removeTaxonomy)
                .invokeAsync()
                .thenApply(removeTaxonomyResult ->
                    switch (removeTaxonomyResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.RemoveFailed e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));  
                    default -> HttpResponses.internalServerError();
                });
  }

}
