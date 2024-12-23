package ccf.api;

import akka.Done;
import akka.http.javadsl.model.DateTime;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.http.HttpResponses;
import ccf.application.CompaniesByFilterView;
import ccf.application.CompaniesByUserView;
import ccf.application.CompanyEntity;
import ccf.domain.Companies;
import ccf.domain.Company;

import ccf.domain.PublishPeriodRequest;
import ccf.util.CCFLog;
import ccf.util.period.FiscalAsOf;
import ccf.util.period.FiscalOperators;
import ccf.util.period.FiscalYearUtils;
import ccf.util.serializer.FiscalOperatorsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
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
        CCFLog.debug(logger, "Getting company",
                Map.of("companyId", companyId));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::getCompany)
                .invokeAsync();
    }

    @Get("/{companyId}/fiscal-dates/{method}/{asOf}")
    public CompletionStage<HttpResponse> fiscalDates(String companyId,
                                                     String method, String asOf) {
        CCFLog.debug(logger, "Getting fiscal years",
                Map.of("companyId", companyId, "method", method, "asOf", asOf));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::getPeriodOps)
                // FiscalOperators method, FiscalAsOf asOf, int additionalParam
                .invokeAsync(new FiscalYearUtils.GetPeriodOpsRequest(FiscalOperators.fromString(method),
                        FiscalAsOf.fromString(asOf), 0))
                .thenApply(getPeriodOpsResult ->
                        switch (getPeriodOpsResult) {
                            case CompanyEntity.CompanyPeriodResult.PeriodInstant instant -> HttpResponses.ok(instant);
                            case CompanyEntity.CompanyPeriodResult.PeriodInteger integer -> HttpResponses.ok(integer);
                            case CompanyEntity.CompanyPeriodResult.PeriodListInstant listInstant -> HttpResponses.ok(listInstant);
                            case CompanyEntity.CompanyPeriodResult.IncorrectPeriodOp e -> HttpResponses.badRequest(e.message());
                        });
    }

    @Post("/{companyId}/user")
    public CompletionStage<HttpResponse> addUser(String companyId, Company.NewUser userId) {
        CCFLog.debug(logger, "Adding user to company",
                Map.of("companyId", companyId, "userId", userId.toString()));
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
    public CompletionStage<HttpResponse> changePublishedPeriod(String companyId,
                                                               PublishPeriodRequest publishedPeriod) {
        CCFLog.debug(logger, "Changing published period",
                Map.of("companyId", companyId, "publishedPeriod", publishedPeriod.toString()));
        Instant period = publishedPeriod.publishedPeriod();
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::changePublishedPeriod)
                .invokeAsync(period)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Post("/{companyId}/create")
    public CompletionStage<HttpResponse> createCompany(String companyId,
                                                       Company.CompanyMetadata metadata
    ) {
        CCFLog.debug(logger, "creating company",
                Map.of("companyId", companyId, "metadata", metadata.toString()));
        return componentClient.forEventSourcedEntity(companyId)
                .method(CompanyEntity::createCompany)
                .invokeAsync(metadata)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Get("/by-user/{user}")
    public CompletionStage<Companies> companiesByUser(String user) {
        CCFLog.debug(logger, "get companies by user",
                Map.of("user", user));
        return componentClient.forView()
                .method(CompaniesByUserView::getCompanies)
                .invokeAsync(user);
    }
    @Get("/all")
    public CompletionStage<Companies> companiesAll() {
        CCFLog.debug(logger, "get companies", Map.of());
        return componentClient.forView()
                .method(CompaniesByFilterView::getAllCompanies)
                .invokeAsync();
    }
    @Get("/by-publish/offset/{offset}")
    public CompletionStage<Companies> companiesByPublishOffset(int offset) {
        if (offset < 1 || offset > 3) {
            throw new IllegalArgumentException("Offset must be greater than 0 or less than 4");
        }
        ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
        LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        Instant currentMonth = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant offsetMonth = currentMonth.minusSeconds(60 * 60 * 24 * 30 * offset);
        PublishPeriodRequest asOfPublishPeriodRequest = new PublishPeriodRequest(offsetMonth);
        CCFLog.debug(logger, "get companies by publish Offset",
                Map.of("offset", String.valueOf(offset), "offsetMonth", offsetMonth.toString(),
                        "asOfPublishPeriodRequest", asOfPublishPeriodRequest.toString()));
        return componentClient.forView()
                .method(CompaniesByFilterView::getPublishPeriodOffBy)
                .invokeAsync(asOfPublishPeriodRequest);
    }

}
