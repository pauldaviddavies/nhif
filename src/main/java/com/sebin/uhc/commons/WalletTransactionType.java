package com.sebin.uhc.commons;

public enum WalletTransactionType {
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String type;

    WalletTransactionType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
