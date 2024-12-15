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
import ccf.application.CompaniesByUserView;
import ccf.application.CompanyEntity;
import ccf.domain.Companies;
import ccf.domain.Company;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//        CCFLogger.log(logger, "Getting company",
//                Map.of("companyId", companyId));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::getCompany)
                .invokeAsync();
    }
    @Post("/{companyId}/user")
    public CompletionStage<HttpResponse> addUser(String companyId, Company.NewUser userId) {
//        CCFLogger.log(logger, "Adding user to company",
//                Map.of("companyId", companyId, "userId", userId.toString()));
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
//        CCFLogger.log(logger, "Changing published period",
//                Map.of("companyId", companyId, "publishedPeriod", publishedPeriod.toString()));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::changePublishedPeriod)
                .invokeAsync(publishedPeriod)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Post("/{companyId}/create")
    public CompletionStage<HttpResponse> createCompany(String companyId,
                                                       Company.CompanyMetadata metadata
    ) {
//        CCFLogger.log(logger, "Actual Creating company",
//                Map.of("companyId", companyId, "metadata", metadata.toString()));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::createCompany)
                .invokeAsync(metadata)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Get("/by-user/{user}")
    public CompletionStage<Companies> companiesByUser(String user) {
        return componentClient.forView()
                .method(CompaniesByUserView::getCompanies)
                .invokeAsync(user);
    }

}
