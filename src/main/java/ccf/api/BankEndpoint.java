package ccf.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import ccf.application.BankEntity;
import ccf.application.BanksByFilterView;
import ccf.domain.bank.Bank;
import ccf.domain.bank.Banks;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/banks")
public class BankEndpoint {
    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(BankEndpoint.class);

    public BankEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/{bankId}")
    public CompletionStage<Bank> get(String bankId) {
        CCFLog.debug(logger, "Getting bank",
                Map.of("bankId", bankId));
        return componentClient.forEventSourcedEntity(bankId)
                .method(BankEntity::getBank)
                .invokeAsync();
    }

    @Post("/{bankId}/user")
    public CompletionStage<HttpResponse> addUser(String bankId, Bank.NewUser userId) {
        CCFLog.debug(logger, "Adding user to bank",
                Map.of("bankId", bankId, "userId", userId.toString()));
        return componentClient.forEventSourcedEntity(bankId)
                .method(BankEntity::addUser)
                .invokeAsync(userId)
                .thenApply(addUserResult ->
                    switch (addUserResult) {
                    case BankEntity.BankResult.Success success -> HttpResponses.ok();
                    case BankEntity.BankResult.IncorrectUserId e -> HttpResponses.badRequest(e.message());
                });
    }

    @Post("/{bankId}/create")
    public CompletionStage<HttpResponse> createBank(String bankId,
                                                       Bank.BankMetadata metadata
    ) {
        CCFLog.debug(logger, "creating bank",
                Map.of("bankId", bankId, "metadata", metadata.toString()));
        return componentClient.forEventSourcedEntity(bankId)
                .method(BankEntity::createBank)
                .invokeAsync(metadata)
                .thenApply(__ -> HttpResponses.ok());
    }
    @Get("/all")
    public CompletionStage<Banks> banksAll() {
        CCFLog.debug(logger, "get banks", Map.of());
        return componentClient.forView()
                .method(BanksByFilterView::getAllBanks)
                .invokeAsync();
    }
}
