package com.sebin.uhc.models.responses.onboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Response <T> {
    private Header header = new Header();
    private T body;

    public Response(final Header header) {
        this.header = header;
    }
}
