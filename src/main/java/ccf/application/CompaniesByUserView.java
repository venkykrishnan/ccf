package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.Companies;
import ccf.domain.Company;
import ccf.domain.CompanyEvent;

@ComponentId("companies_by_user")
public class CompaniesByUserView extends View {

    @Consume.FromEventSourcedEntity(CompanyEntity.class)
    public static class CompaniesByUser extends TableUpdater<Company> { // <2>

        public Effect<Company> onEvent(CompanyEvent event) { // <3>
            return switch (event) {
                case CompanyEvent.CompanyCreated created ->
                        effects().updateRow(rowState().onCompanyCreated(created));
                case CompanyEvent.CompanyUserAdded userAdded ->
                        effects().updateRow(rowState().onCompanyUserAdded(userAdded));

                case CompanyEvent.CompanyPublishedPeriodChanged publishedPeriodChanged ->
                        effects().updateRow(rowState().onCompanyPublishedPeriodChanged(publishedPeriodChanged));
            };
        }
    }

    @Query("SELECT * AS companies FROM companies_by_user WHERE :user = ANY(users)")
    public View.QueryEffect<Companies> getCompanies(String user) {
        return queryResult();
    }
}
