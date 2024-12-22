package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.Companies;
import ccf.domain.CompanyEvent;
import ccf.domain.CompanyRow;
import ccf.domain.PublishPeriodRequest;
import ccf.util.serializer.CustomInstantDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;

@ComponentId("companies_by_filter")
public class CompaniesByFilterView extends View {

    @Consume.FromEventSourcedEntity(CompanyEntity.class)
    public static class CompaniesByFilter extends TableUpdater<CompanyRow> { // <2>
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

    @Query("SELECT * AS companies FROM companies_by_filter")
    public QueryEffect<Companies> getAllCompanies() {
        return queryResult();
    }

    //    @Query("SELECT * AS companies FROM companies_by_filter WHERE :offsetMonth = companyId")
    @Query("SELECT * AS companies FROM companies_by_filter where  publishedPeriod < :offsetMonth")
    public QueryEffect<Companies> getPublishPeriodOffBy(
            Instant offsetMonth) {
        return queryResult();
    }

}
