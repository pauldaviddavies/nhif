package com.sebin.uhc.commons;

import lombok.Getter;

@Getter
public enum Criteria {
    ID_PASSPORT("ID"),
    MINI("MINI"),
    FULL("FULL"),
    MOBILE("Mobile");

    private final String criterion;
    Criteria(final String criterion) {
        this.criterion = criterion;
    }

}
