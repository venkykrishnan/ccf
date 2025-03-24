package ccf.api;

import akka.Done;
import akka.http.javadsl.model.DateTime;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.http.HttpResponses;
import ccf.application.StandardEntity;
import ccf.application.StandardsByFilterView;
import ccf.domain.standard.Standard;
import ccf.domain.standard.StandardDimension.DimensionRemove;
import ccf.domain.standard.StandardDimension.StandardDimensionCreate;
import ccf.domain.standard.StandardDimension.TaxonomyCreate;
import ccf.domain.standard.StandardDimension.TaxonomyRemove;
import ccf.domain.standard.StandardDimension.TaxonomyVersionCreate;
import ccf.domain.standard.StandardDimension.TaxonomyVersionDefault;
import ccf.domain.standard.StandardDimension.TaxonomyVersionPublish;
import ccf.domain.standard.StandardDimension.TaxonomyVersionRemove;
import ccf.domain.standard.StandardDomain;
import ccf.domain.standard.StandardDomain.DomainRemove;
import ccf.domain.standard.StandardRows;
import ccf.domain.standard.StandardVersion;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/standard")
public class StandardEndpoint {
    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(StandardEndpoint.class);

    public StandardEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/{standardId}")
    public CompletionStage<Standard> get(String standardId) {
        CCFLog.debug(logger, "Getting standard",
                Map.of("standardId", standardId));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::getStandard)
                .invokeAsync();
    }
    @Post("/{standardId}")
    public CompletionStage<HttpResponse> createStandard(String standardId, Standard.StandardCreate standard) {
        CCFLog.debug(logger, "Creating standard",
                Map.of("standardId", standardId, "standard", standard.toString()));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::createStandard)
                .invokeAsync(standard)
                .thenApply(createStandardResult ->
                    switch (createStandardResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

    @Post("/{standardId}/domain")
    public CompletionStage<HttpResponse> addDomain(String standardId, StandardDomain standardDomain) {
        CCFLog.debug(logger, "Adding domain to standard",
                Map.of("standardId", standardId, "domain", standardDomain.toString()));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::addDomain)
                .invokeAsync(standardDomain)
                .thenApply(addDomainResult ->
                    switch (addDomainResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

    @Delete("/{standardId}/domain/{domainName}")
    public CompletionStage<HttpResponse> removeDomain(String standardId, String domainName) {
                CCFLog.debug(logger, "Removing domain from standard",
                Map.of("standardId", standardId, "domainName", domainName));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::removeDomain)
                .invokeAsync(new DomainRemove(domainName))
                .thenApply(removeDomainResult ->
                    switch (removeDomainResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectCleanup e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    
    @Post("/{standardId}/domain/dimension")
    public CompletionStage<HttpResponse> addDimension(String standardId, StandardDimensionCreate standardDimension) {
        CCFLog.debug(logger, "Adding dimension",
                Map.of("standardId", standardId,"dimension", standardDimension.toString()));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::addDimension)
                .invokeAsync(standardDimension)
                .thenApply(addDimensionResult ->
                    switch (addDimensionResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

    @Delete("/{standardId}/dimension/{dimensionName}")
    public CompletionStage<HttpResponse> removeDimension(String standardId, String dimensionName) {
        CCFLog.debug(logger, "Removing dimension",
                Map.of("standardId", standardId,  "dimensionName", dimensionName));
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::removeDimension)
                .invokeAsync(new DimensionRemove(dimensionName))
                .thenApply(removeDimensionResult ->
                    switch (removeDimensionResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectCleanup e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

    @Post("/{standardId}/dimension/{dimensionName}/taxonomy")
    public CompletionStage<HttpResponse> addTaxonomy(String standardId, String dimensionName, TaxonomyCreate createTaxonomy) {
        CCFLog.debug(logger, "Adding taxonomy",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomy", createTaxonomy.toString()));
        if (createTaxonomy.dimensionName() != dimensionName) {
            return CompletableFuture.completedFuture(HttpResponses.badRequest("Dimension name must be the same as the taxonomy name"));
        }
        return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::addTaxonomy)
                .invokeAsync(createTaxonomy)
                .thenApply(addTaxonomyResult ->
                    switch (addTaxonomyResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
  }

  @Delete("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}")
  public CompletionStage<HttpResponse> removeTaxonomy(String standardId, String dimensionName, String taxonomyName) {
    CCFLog.debug(logger, "Removing taxonomy",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName));
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::removeTaxonomy)
                .invokeAsync(new TaxonomyRemove(dimensionName, taxonomyName))
                .thenApply(removeTaxonomyResult ->
                    switch (removeTaxonomyResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectCleanup e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
  }

  @Post("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}/version")
  public CompletionStage<HttpResponse> addTaxonomyVersion(String standardId, String dimensionName, String taxonomyName, TaxonomyVersionCreate createTaxonomyVersion) {
    CCFLog.debug(logger, "Adding taxonomy version",
                      Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName, "taxonomyVersion", createTaxonomyVersion.toString()));
    if (createTaxonomyVersion.dimensionName() != dimensionName || createTaxonomyVersion.taxonomyName() != taxonomyName) {
        return CompletableFuture.completedFuture(HttpResponses.badRequest("Dimension name and taxonomy name must be the same as the taxonomy version name"));
    }
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::addTaxonomyVersion)
                .invokeAsync(createTaxonomyVersion)
                .thenApply(addTaxonomyVersionResult ->
                    switch (addTaxonomyVersionResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    default -> HttpResponses.internalServerError();
                });
  }

  @Delete("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}/version/{taxonomyVersionName}")
  public CompletionStage<HttpResponse> removeTaxonomyVersion(String standardId, String dimensionName, String taxonomyName, String taxonomyVersionName) {
    CCFLog.debug(logger, "Removing taxonomy version",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName, "taxonomyVersionName", taxonomyVersionName));
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::removeTaxonomyVersion)
                .invokeAsync(new TaxonomyVersionRemove(dimensionName, taxonomyName, new StandardVersion(taxonomyVersionName)))
                .thenApply(removeTaxonomyVersionResult ->
                    switch (removeTaxonomyVersionResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    default -> HttpResponses.internalServerError();
                });
  }

  @Put("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}/default")
  public CompletionStage<HttpResponse> setTaxonomyDefaultVersion(String standardId, String dimensionName, String taxonomyName, StandardVersion standardVersion) {
    CCFLog.debug(logger, "Setting taxonomy default version",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName, "standardVersion", standardVersion.toString()));
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::setTaxonomyDefaultVersion)
                .invokeAsync(new TaxonomyVersionDefault(dimensionName, taxonomyName, standardVersion))
                .thenApply(setTaxonomyDefaultVersionResult ->
                    switch (setTaxonomyDefaultVersionResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectUpdate e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
  }

  @Put("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}/publish")
  public CompletionStage<HttpResponse> publishTaxonomy(String standardId, String dimensionName, String taxonomyName, StandardVersion standardVersion) {
    CCFLog.debug(logger, "Publishing taxonomy",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName, "standardVersion", standardVersion.toString()));
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::publishTaxonomy)
                .invokeAsync(new TaxonomyVersionPublish(dimensionName, taxonomyName, standardVersion, true))
                .thenApply(publishTaxonomyResult ->
                    switch (publishTaxonomyResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectUpdate e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
  }

  @Put("/{standardId}/dimension/{dimensionName}/taxonomy/{taxonomyName}/unpublish")
  public CompletionStage<HttpResponse> unpublishTaxonomy(String standardId, String dimensionName, String taxonomyName, StandardVersion standardVersion) {
    CCFLog.debug(logger, "Unpublishing taxonomy",
                Map.of("standardId", standardId, "dimensionName", dimensionName, "taxonomyName", taxonomyName, "standardVersion", standardVersion.toString()));
    return componentClient.forEventSourcedEntity(standardId)
                .method(StandardEntity::publishTaxonomy)
                .invokeAsync(new TaxonomyVersionPublish(dimensionName, taxonomyName, standardVersion, false))
                .thenApply(publishTaxonomyResult ->
                    switch (publishTaxonomyResult) {
                    case StandardEntity.StandardResult.Success success -> HttpResponses.ok();
                    case StandardEntity.StandardResult.IncorrectUpdate e -> HttpResponses.badRequest(
                        "Action: %s, Message: %s".formatted(e.inputType(), e.message()));
                    default -> HttpResponses.internalServerError();
                });
  }
  @Get("/all")
  public CompletionStage<StandardRows> getAllStandards() {
    CCFLog.debug(logger, "get standards", Map.of());
    return componentClient.forView()
                .method(StandardsByFilterView::getAllStandards)
                .invokeAsync();
  } 
}
