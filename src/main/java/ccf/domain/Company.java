package ccf.domain;

import ccf.util.period.FiscalYears;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record Company(String companyId,
                      CompanyMetadata metadata,
                      FiscalYears fiscalYears,
                      CompanyInstanceType instanceType,
                      List<String> users, Instant publishedPeriod,
                      CompanyStatus status,
                      Instant creationTimestamp, Instant modificationTimestamp
                      ) {
    private static final Logger logger = LoggerFactory.getLogger(Company.class);

    public record NewUser(String userId) {
    }

//    public record PublishedPeriod(Instant publishedPeriod) {
//        public PublishedPeriod {
//            if (publishedPeriod == null) {
//                throw new IllegalArgumentException("publishedPeriod must not be null");
//            }
//        }
//    }

    // First time company record. This is also used in views
    public record CompanyCreateInfo(String companyId,
                                    CompanyMetadata metadata,
                                    CompanyInstanceType instanceType,
                                    List<String> users, Instant publishedPeriod,
                                    CompanyStatus status,
                                    Instant creationTimestamp, Instant modificationTimestamp) {

    }

    public record CompanyMetadata(Integer naicsCode, URL url,
                                  FiscalInfo fiscalInfo, String bankId) {
    }

    public record FiscalInfo(Year startYear, Integer numberOfYears, Month startMonth) {
        public FiscalInfo {
            if (startYear == null || numberOfYears == null || startMonth == null) {
                throw new IllegalArgumentException("FiscalInfo must have startYear, numberOfYears and startMonth");
            }
            if (startYear.isBefore(Year.of(CompanyMinMaxStartYear.MIN_START_YEAR.getValue())) ||
                    startYear.isAfter(Year.of(CompanyMinMaxStartYear.MAX_START_YEAR.getValue()))) {
                throw new IllegalArgumentException("startYear not within range min %d max %d".formatted(
                        CompanyMinMaxStartYear.MIN_START_YEAR.getValue(),
                        CompanyMinMaxStartYear.MAX_START_YEAR.getValue()));
            }
            if (numberOfYears < CompanyMinMaxNumberOfYears.MIN_NUMBER_OF_YEARS.getValue() ||
                    numberOfYears > CompanyMinMaxNumberOfYears.MAX_NUMBER_OF_YEARS.getValue()) {
                throw new IllegalArgumentException("numberOfYears not within range min %d max %d".formatted(
                        CompanyMinMaxNumberOfYears.MIN_NUMBER_OF_YEARS.getValue(),
                        CompanyMinMaxNumberOfYears.MAX_NUMBER_OF_YEARS.getValue()));
            }

        }
    }

    public Company onCompanyCreated(CompanyEvent.CompanyCreated companyCreated) {
        CompanyCreateInfo createInfo = companyCreated.createInfo();
        FiscalYears fiscalYears = new FiscalYears(createInfo.metadata().fiscalInfo().startYear(),
                createInfo.metadata().fiscalInfo().startMonth(), createInfo.metadata().fiscalInfo().numberOfYears());
        return new Company(createInfo.companyId, createInfo.metadata(),
                fiscalYears,
                createInfo.instanceType, createInfo.users,
                createInfo.publishedPeriod, createInfo.status,
                createInfo.creationTimestamp, createInfo.modificationTimestamp);
    }

    public Company onCompanyUserAdded(CompanyEvent.CompanyUserAdded companyUserAdded) {
        logger.info("Adding user to company id={} metadata= {}, userId={}", companyId, metadata,companyUserAdded.userId());
        List<String> updatedUsers = Stream.concat(
                Optional.ofNullable(users).stream().flatMap(List::stream),
                Stream.of(companyUserAdded.userId())
        ).toList();

        return new Company(companyId, metadata, fiscalYears, instanceType, updatedUsers, publishedPeriod,
                CompanyStatus.COMPANY_INITIALIZED, creationTimestamp, Instant.now());
    }

    public Company onCompanyPublishedPeriodChanged(CompanyEvent.CompanyPublishedPeriodChanged companyPublishedPeriodChanged) {
        logger.info("Changing published period for company id={} metadata={}, publishedPeriod={}",
                companyId, metadata, companyPublishedPeriodChanged.publishedPeriod());
        // TODO: check if the published period is within the fiscal periods
//        if (metadata.fiscalInfo().startYear().plusYears(metadata.fiscalInfo().numberOfYears()).isBefore(Year.of(companyPublishedPeriodChanged.publishedPeriod().getYear()))
//                || metadata.fiscalInfo().startYear().isAfter(Year.of(companyPublishedPeriodChanged.publishedPeriod().getYear()))) {
//            throw new IllegalArgumentException("publishedPeriod is not within the fiscal period");
//        }
        return new Company(companyId, metadata, fiscalYears, instanceType, users,
                companyPublishedPeriodChanged.publishedPeriod(), status, creationTimestamp, Instant.now());
    }
}
