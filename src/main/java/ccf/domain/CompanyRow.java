package ccf.domain;

import akka.http.javadsl.model.DateTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ccf.util.serializer.AkkaLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record CompanyRow(
        String companyId,
        List<String> users,
//        @JsonDeserialize(using = AkkaDateTimeDeserializer.class)
        Instant publishedPeriod,
        Instant creationTimestamp // TODO: probably not needed, Im trying out LocalDateTime
        ) {
    //    public CompanyRow onCompanyCreated(String companyId, String user, DateTime publishedPeriod) {
//        List<String> localUsers = Arrays.asList(user);
//        return new CompanyRow(companyId, localUsers, publishedPeriod);
//    }
    public CompanyRow onCompanyUserAdded(String user) {
        List<String> localUsers = new ArrayList<String>();
        localUsers.addAll(users);
        localUsers.add(user);
        return new CompanyRow(companyId, localUsers, publishedPeriod, creationTimestamp);
    }

    public CompanyRow onCompanyPublishedPeriodChanged(Instant publishedPeriod) {
        return new CompanyRow(companyId, users, publishedPeriod, creationTimestamp);
    }
}
