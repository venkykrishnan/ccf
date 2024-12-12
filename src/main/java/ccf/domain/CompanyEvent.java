package ccf.domain;

import akka.javasdk.annotations.TypeName;

import java.time.LocalDate;

public sealed interface CompanyEvent {
    @TypeName("company-created")
    record CompanyCreated(Company.CompanyMetadata metadata) implements CompanyEvent {
    }
    @TypeName("company-user-added")
    record CompanyUserAdded(String userId) implements CompanyEvent {
    }
    @TypeName("company-published-period-changed")
    record CompanyPublishedPeriodChanged(LocalDate publishedPeriod) implements CompanyEvent {
    }
}
