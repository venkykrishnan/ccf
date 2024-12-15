package ccf.domain;

import ccf.application.CompanyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Company(String companyId,
                      CompanyMetadata metadata,
                      CompanyInstanceType instanceType,
                      List<String> users, LocalDate publishedPeriod,
                      CompanyStatus status,
                      LocalDateTime creationTimestamp, LocalDateTime modificationTimestamp
                      ) {
    private static final Logger logger = LoggerFactory.getLogger(Company.class);

    public record NewUser(String userId) {
    }

    public record PublishedPeriod(LocalDate publishedPeriod) {
        public PublishedPeriod {
            if (publishedPeriod == null) {
                throw new IllegalArgumentException("publishedPeriod must not be null");
            }
            // check if the publishedPeriod is the last date of the month and if it is not, throw an exception
            if (publishedPeriod.getDayOfMonth() != publishedPeriod.lengthOfMonth()) {
                throw new IllegalArgumentException("publishedPeriod must be the last date of the month");
            }
        }
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
        return new Company(companyId, companyCreated.metadata(), instanceType, users, publishedPeriod,
                CompanyStatus.COMPANY_INITIALIZED_NO_USERS, creationTimestamp, LocalDateTime.now());
    }
    public Company onCompanyUserAdded(CompanyEvent.CompanyUserAdded companyUserAdded) {
        logger.info("Adding user to company id={} metadata= {}, userId={}", companyId, metadata,companyUserAdded.userId());
        List<String> updatedUsers = Stream.concat(
                Optional.ofNullable(users).stream().flatMap(List::stream),
                Stream.of(companyUserAdded.userId())
        ).toList();

        return new Company(companyId, metadata, instanceType, updatedUsers, publishedPeriod,
                CompanyStatus.COMPANY_INITIALIZED, creationTimestamp, LocalDateTime.now());
    }
    public Company onCompanyPublishedPeriodChanged(CompanyEvent.CompanyPublishedPeriodChanged companyPublishedPeriodChanged) {
        logger.info("Changing published period for company id={} metadata={}, publishedPeriod={}",
                companyId, metadata, companyPublishedPeriodChanged.publishedPeriod());
        // check if the published period is within the fiscal periods
        if (metadata.fiscalInfo().startYear().plusYears(metadata.fiscalInfo().numberOfYears()).isBefore(Year.of(companyPublishedPeriodChanged.publishedPeriod().getYear()))
                || metadata.fiscalInfo().startYear().isAfter(Year.of(companyPublishedPeriodChanged.publishedPeriod().getYear()))) {
            throw new IllegalArgumentException("publishedPeriod is not within the fiscal period");
        }
        return new Company(companyId, metadata, instanceType, users,
                companyPublishedPeriodChanged.publishedPeriod(), status, creationTimestamp, LocalDateTime.now());
    }
}
