package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.bank.BankEvent;
import ccf.domain.bank.BankRow;
import ccf.domain.bank.Banks;

@ComponentId("banks_by_filter")
public class BanksByFilterView extends View {

    @Consume.FromEventSourcedEntity(BankEntity.class)
    public static class BanksByFilter extends TableUpdater<BankRow> { // <2>
        public Effect<BankRow> onEvent(BankEvent event) { // <3>
            var ret = switch (event) {
                case BankEvent.BankCreated created->
                    effects().updateRow(new BankRow(created.createInfo().bankId(),
                            created.createInfo().users(),
                            created.createInfo().creationTimestamp()));
                case BankEvent.BankUserAdded userAdded ->
                        effects().updateRow(rowState().onBankUserAdded(userAdded.userId()));
            };
            return ret;
        }
    }

    @Query("SELECT * AS banks FROM banks_by_filter")
    public QueryEffect<Banks> getAllBanks() {
        return queryResult();
    }
}
