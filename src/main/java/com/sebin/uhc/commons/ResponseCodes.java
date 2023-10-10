package com.sebin.uhc.commons;

import lombok.Getter;

import javax.management.loading.MLetContent;

@Getter
public enum ResponseCodes {
    SUCCESS("000"), // Success
    FAIL("1000"), // Failure
    NULL_REQUEST("1001"), // Null or empty request
    NULL_REQUEST_ID("1002"), // Null or empty request Id
    BASIC_DETAILS_MISSING("1003"), // Missing basic details
    MOBILE_NUMBER_MISSING("1004"),// Missing mobile number
    INVALID_MOBILE_NUMBER_FORMAT("1005"), //Wrong format of mobile or phone
    ID_PASSPORT_MISSING("1006"), // National Id or passport missing
    FIRST_NAME_MISSING("1007"), //First name is missing
    SPONSOR_MISSING("1012"),//Missing sponsor
    SUBSCRIBER_ALREADY_EXISTS("1008"), //Already subscribed with the provided mobile, Id or passport number.
    BENEFICIARY_ALREADY_SUBSCRIBED("1009"),//Beneficiary already added
    BENEFICIARY_OR_SPONSOR_MISSING("1010"), //Beneficiary and/or sponsor missing.
    BENEFICIARY_ID_MISSING("1011"),//Beneficiary Id missing
    PIN_FORMAT_INVALID("1012"),//PIN length must be greater than four characters without spaces
    PIN_EXISTS("1013"),//PIN EXISTS
    PIN_INCORRECT("1014"),//INCORRECT PIN
    PIN_MISMATCH("1015"),//New Pin and Confirm New PIN must match
    MEMBER_NUMBER_MISSING("1017"), // Missing member number
    AMOUNT_MISSING("1019"),//Amount Not found
    PIN_MISSING("1020"),//PIN_MISSING found
    SIMILAR_REQUEST_PENDING("1021"),//PIN_MISSING found
    INVALID_AMOUNT("1022"),//INVALID_AMOUNT found
    DATE_OF_BIRTH_MISSING("1023"),//DATE_OF_BIRTH missing
    INVALID_DATE_OF_BIRTH("1024"),//INVALID_DATE_OF_BIRTH
    PAYMENT_PURPOSE_MISSING("1025"),//PAYMENT_PURPOSE_MISSING
    INVALID_PAYMENT_PUROSE("1026"),//INVALID_PAYMENT_PUROSE
    INSUFFICIENT_BALANCE("1027"),//INSUFFICIENT_BALANCE
    MISSING_GENDER("1028"),//MISSING_GENDER
    INVALID_GENDER("1029"),//INVALID_GENDER
    GENERAL_FAILURE("2000"), // General failure response code
    UNACCEPTABLE_REQUEST_FORMAT("3000");

    private final String code;

    ResponseCodes(final String code) {
        this.code = code;
    }

}
