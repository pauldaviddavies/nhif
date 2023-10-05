package com.sebin.uhc.commons;

public enum Statuses {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SENT("Sent"),
    PENDING("Pending"),
    REMOVED("Removed"),
    SUCCESS("Success"),
    FAIL("Fail"),
    RESPONSE("Response");

    private final String status;

    Statuses(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
