package ccf.domain;

import akka.http.javadsl.model.DateTime;
import akka.javasdk.annotations.TypeName;

import java.time.Instant;
import java.time.LocalDate;

public sealed interface CompanyEvent {
    @TypeName("company-created")
    record CompanyCreated(Company.CompanyCreateInfo createInfo) implements CompanyEvent {
    }
//    record CompanyCreated(Company.CompanyMetadata metadata) implements CompanyEvent {
    @TypeName("company-user-added")
    record CompanyUserAdded(String userId) implements CompanyEvent {
    }
    @TypeName("company-published-period-changed")
    record CompanyPublishedPeriodChanged(Instant publishedPeriod) implements CompanyEvent {
    }
}
