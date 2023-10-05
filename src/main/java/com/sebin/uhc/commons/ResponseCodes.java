package com.sebin.uhc.commons;

import lombok.Getter;

import javax.management.loading.MLetContent;

@Getter
public enum ResponseCodes {
    SUCCESS("000"), // Success
    FAIL("1000"), // Failure
    NULL_REQUEST("1001"), // Null or empty request
    NULL_REQUEST_ID("1002"), // Null or empty request Id
    BASIC_DETAILS("1003"), // Missing basic details
    MOBILE("1004"),// Missing mobile number
    MOBILE_NUMBER_FORMAT("1005"), //Wrong format of mobile or phone
    ID_PASSPORT("1006"), // National Id or passport missing
    FIRST_NAME("1007"), //First name is missing
    SPONSOR("1012"),//Missing sponsor
    SUBSCRIBED("1008"), //Already subscribed with the provided mobile, Id or passport number.
    BENEFICIARY_SUBSCRIBED("1009"),//Beneficiary already added
    BENE_SPONSOR("1010"), //Beneficiary and/or sponsor missing.
    BENEFICIARY_ID("1011"),//Beneficiary Id missing
    PIN("1012"),//PIN length must be greater than four characters without spaces
    PIN_EXISTS("1013"),//PIN EXISTS
    PIN_INCORRECT("1014"),//INCORRECT PIN
    PIN_MISMATCH("1015"),//New Pin and Confirm New PIN must match
    DUPLICATE("1016"),//Duplicate
    MEMBER_NUMBER("1017"), // Missing member number
    NOT_FOUND("1018"),//Not found
    AMOUNT("1019"),//Amount Not found
    PIN_MISSING("1020"),//PIN_MISSING found
    SIMILAR_REQUEST_PENDING("1021"),//PIN_MISSING found
    INVALID_AMOUNT("1022"),//PIN_MISSING found
    DATE_OF_BIRTH("1023"),//DATE_OF_BIRTH missing
    INVALID_DATE_OF_BIRTH("1024"),//INVALID_DATE_OF_BIRTH
    GENERAL("2000"), // General failure response code
    CONSTRAINT_VIOLATION("3000");

    private final String code;

    ResponseCodes(final String code) {
        this.code = code;
    }

}
