package ccf.domain;

import akka.javasdk.annotations.TypeName;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;

public sealed interface CompanyEvent {
    @TypeName("company-created")
    record CompanyCreated(Integer naicsCode, URL url,
                          Company.FiscalInfo fiscalInfo, String bankId) implements CompanyEvent {
    }
    @TypeName("company-user-added")
    record CompanyUserAdded(String userId) implements CompanyEvent {
    }
    @TypeName("company-published-period-changed")
    record CompanyPublishedPeriodChanged(LocalDate publishedPeriod) implements CompanyEvent {
    }
}
