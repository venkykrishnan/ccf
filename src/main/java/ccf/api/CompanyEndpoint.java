package ccf.api;

import akka.Done;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.http.HttpResponses;
import ccf.application.CompanyEntity;
import ccf.domain.Company;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/companies")
public class CompanyEndpoint {
    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(CompanyEndpoint.class);

    public CompanyEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/{companyId}")
    public CompletionStage<Company> get(String companyId) {
        logger.info("Get company id {}", companyId);
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::getCompany)
                .invokeAsync();
    }
    @Post("/{companyId}/user")
    public CompletionStage<HttpResponse> addUser(String companyId, Company.NewUser userId) {
        logger.info("Adding user to company id={} userId={}", companyId, userId);
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::addUser)
                .invokeAsync(userId)
                .thenApply(addUserResult ->
                    switch (addUserResult) {
                    case CompanyEntity.CompanyResult.Success success -> HttpResponses.ok();
                    case CompanyEntity.CompanyResult.IncorrectUserId e -> HttpResponses.badRequest(e.message());
                });
    }
    @Put("/{companyId}/publish-period")
    public CompletionStage<HttpResponse> changePublishedPeriod(String companyId, Company.PublishedPeriod publishedPeriod) {
        logger.info("Changing published period for company id={} publishedPeriod={}", companyId, publishedPeriod);
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::changePublishedPeriod)
                .invokeAsync(publishedPeriod)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Post("/{companyId}/create")
    public CompletionStage<HttpResponse> createCompany(String companyId,
                                                       Company.CompanyMetadata metadata
    ) {
        logger.info("Actual Creating company id={} metadata={}", companyId, metadata);
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::createCompany)
                .invokeAsync(metadata)
                .thenApply(__ -> HttpResponses.ok());
    }
}
