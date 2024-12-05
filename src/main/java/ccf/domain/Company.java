package ccf.domain;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.List;

public record Company(String companyId, Integer naicsCode, URL url,
                      FiscalInfo fiscalInfo, String bankId,
                      CompanyInstanceType instanceType,
                      List<String> users, LocalDate publishedPeriod,
                      CompanyStatus status,
                      LocalDateTime creationTimestamp, LocalDateTime modificationTimestamp
                      ) {
    public Company(String companyId, Integer naicsCode, URL url, FiscalInfo fiscalInfo, String bankId) {
        this(companyId, naicsCode, url, fiscalInfo, bankId, CompanyInstanceType.ACTUAL, null, LocalDate.now(),
                CompanyStatus.COMPANY_INITIALIZED, LocalDateTime.now(), LocalDateTime.now());
    }

    public record CompanyExperiment(Integer naicsCode, URL url,
                                    FiscalInfo fiscalInfo, String bankId) {
    }

    public record CompanyMetadata(Integer naicsCode, String urlString,
                                  FiscalInfo fiscalInfo, String bankId) {
    }

    public record FiscalInfo(Year startYear, Integer numberOfYears, Month startMonth) {
//        public FiscalInfo(Year startYear, Integer numberOfYears, String startMonthString) {
//            this(startYear, numberOfYears, Month.valueOf(startMonthString));
//        }
    }

    public Company onCompanyCreated(CompanyEvent.CompanyCreated companyCreated) {
        return new Company(companyId, companyCreated.naicsCode(), companyCreated.url(),
                companyCreated.fiscalInfo(), companyCreated.bankId(), instanceType, users, publishedPeriod,
                CompanyStatus.COMPANY_INITIALIZED, creationTimestamp, LocalDateTime.now());
    }
    public Company onCompanyUserAdded(CompanyEvent.CompanyUserAdded companyUserAdded) {
        var updatedUsers = users;
        updatedUsers.add(companyUserAdded.userId());
        return new Company(companyId, naicsCode, url, fiscalInfo, bankId, instanceType, updatedUsers, publishedPeriod,
                status, creationTimestamp, LocalDateTime.now());
    }
    public Company onCompanyPublishedPeriodChanged(CompanyEvent.CompanyPublishedPeriodChanged companyPublishedPeriodChanged) {
        return new Company(companyId, naicsCode, url, fiscalInfo, bankId, instanceType, users,
                companyPublishedPeriodChanged.publishedPeriod(), status, creationTimestamp, LocalDateTime.now());
    }
}
