package com.sebin.uhc.models.requests.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Request<T> {
    private RequestHeader header;
    private T body;

    public Request(T body) {
        this.body = body;
    }
}
