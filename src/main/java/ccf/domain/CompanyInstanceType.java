package ccf.domain;
/*
enum CompanyInstanceType {
    COMPANY_ACTUAL = 0; // this company represents an actual company
    COMPANY_REPLICA = 1; // this company is a copy of a actual company, often used for use cases like diagnostics
    COMPANY_GENERATED = 2; // this company uses generated data
}

 */
public enum CompanyInstanceType {
    ACTUAL, // this company represents an actual company
    REPLICA,
    GENERATED
}
