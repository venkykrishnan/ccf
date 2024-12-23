package ccf.application;

import akka.http.javadsl.model.DateTime;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.*;

import java.util.List;

@ComponentId("companies_by_user")
public class CompaniesByUserView extends View {

    @Consume.FromEventSourcedEntity(CompanyEntity.class)
    public static class CompaniesByUser extends TableUpdater<CompanyRow> { // <2>
        public Effect<CompanyRow> onEvent(CompanyEvent event) { // <3>
            var ret = switch (event) {
                case CompanyEvent.CompanyCreated created->
                    effects().updateRow(new CompanyRow(created.createInfo().companyId(),
                            created.createInfo().users(), created.createInfo().publishedPeriod(),
                            created.createInfo().creationTimestamp()));
                case CompanyEvent.CompanyUserAdded userAdded ->
                        effects().updateRow(rowState().onCompanyUserAdded(userAdded.userId()));
                case CompanyEvent.CompanyPublishedPeriodChanged publishedPeriodChanged ->
                        effects().updateRow(rowState().onCompanyPublishedPeriodChanged(
                                        publishedPeriodChanged.publishedPeriod()
                        ));
            };
            return ret;
        }
    }
    @Query("SELECT * AS companies FROM companies_by_user WHERE :user = ANY(users)")
    public View.QueryEffect<Companies> getCompanies(String user) {
        return queryResult();
    }
}
