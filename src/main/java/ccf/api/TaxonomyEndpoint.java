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
import ccf.application.TaxonomyByFilterView;
import ccf.application.TaxonomyEntity;
import ccf.domain.standard.Taxonomy;
import ccf.domain.standard.TaxonomyRows;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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

    @Delete("/{taxonomyId}")
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

    @Put("/{taxonomyId}/publish")
    public CompletionStage<HttpResponse> publishTaxonomy(String taxonomyId) {
        CCFLog.debug(logger, "Publishing taxonomy",
                Map.of("taxonomyId", taxonomyId));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::publishTaxonomy)
                .invokeAsync(true)
                .thenApply(publishTaxonomyResult ->
                    switch (publishTaxonomyResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.PublishFailed e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    @Put("/{taxonomyId}/open")
    public CompletionStage<HttpResponse> openTaxonomy(String taxonomyId) {
        CCFLog.debug(logger, "Opening(unpublishing) taxonomy",
                Map.of("taxonomyId", taxonomyId));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::publishTaxonomy)
                .invokeAsync(false)
                .thenApply(publishTaxonomyResult ->
                    switch (publishTaxonomyResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.PublishFailed e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }

    @Post("/{taxonomyId}/taxrow")
    public CompletionStage<HttpResponse> addTaxRow(String taxonomyId, Taxonomy.TaxRowAdd taxRowAdd) {
        CCFLog.debug(logger, "Adding tax row",
                Map.of("taxonomyId", taxonomyId, "taxRowAdd", taxRowAdd.toString()));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::addTaxRow)
                .invokeAsync(taxRowAdd)
                .thenApply(addTaxRowResult ->
                    switch (addTaxRowResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));  
                    default -> HttpResponses.internalServerError();
                });
    }

    @Delete("/{taxonomyId}/taxrow/{rowId}")
    public CompletionStage<HttpResponse> removeTaxRow(String taxonomyId, String rowId) {
        CCFLog.debug(logger, "Removing tax row",
                Map.of("taxonomyId", taxonomyId, "rowowId", rowId));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::removeTaxRow)
                .invokeAsync(rowId)
                .thenApply(removeTaxRowResult ->
                    switch (removeTaxRowResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.IncorrectRemove e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    @Post("/{taxonomyId}/taxrows")
    public CompletionStage<HttpResponse> addTaxRows(String taxonomyId, Taxonomy.TaxRowsAdd taxRowsAdd) {
        CCFLog.debug(logger, "Adding tax rows",
                Map.of("taxonomyId", taxonomyId, "taxRowsAdd", taxRowsAdd.toString()));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::addTaxRows)
                .invokeAsync(taxRowsAdd)
                .thenApply(addTaxRowsResult ->
                    switch (addTaxRowsResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.IncorrectAdd e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    @Delete("/{taxonomyId}/taxrows")
    public CompletionStage<HttpResponse> removeTaxRows(String taxonomyId, Taxonomy.TaxRowsRemove taxRowsRemove) {
        CCFLog.debug(logger, "Removing tax rows",
                Map.of("taxonomyId", taxonomyId, "taxRowsRemove", taxRowsRemove.toString()));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::removeTaxRows)
                .invokeAsync(taxRowsRemove)
                .thenApply(removeTaxRowsResult ->
                    switch (removeTaxRowsResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.IncorrectRemove e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    @Put("/{taxonomyId}/taxrow/{rowId}")
    public CompletionStage<HttpResponse> updateTaxRow(String taxonomyId, String rowId, Taxonomy.TaxRowUpdate taxRowUpdate) {
        CCFLog.debug(logger, "Updating tax row",
                Map.of("taxonomyId", taxonomyId, "rowId", rowId, "taxRowUpdate", taxRowUpdate.toString()));
        return componentClient.forEventSourcedEntity(taxonomyId)
                .method(TaxonomyEntity::updateTaxRow)
                .invokeAsync(rowId, taxRowUpdate)
                .thenApply(updateTaxRowResult ->
                    switch (updateTaxRowResult) {
                    case TaxonomyEntity.TaxonomyResult.Success success -> HttpResponses.ok();
                    case TaxonomyEntity.TaxonomyResult.IncorrectUpate e -> HttpResponses.badRequest(
                        "Message: %s".formatted(e.message()));
                    default -> HttpResponses.internalServerError();
                });
    }
    @Get("/all")
    public CompletionStage<TaxonomyRows> getAllTaxonomies() {
        CCFLog.debug(logger, "get all taxonomies", Map.of());
        return componentClient.forView()
                .method(TaxonomyByFilterView::getAllTaxonomies)
                .invokeAsync();
    }
}
