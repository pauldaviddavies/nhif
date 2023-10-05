package com.sebin.uhc.commons;

import com.sebin.uhc.entities.payments.Token;
import com.sebin.uhc.models.TokenTypes;
import com.sebin.uhc.repositories.payments.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccessTokenLoader {
    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    Configs configs;

    public String getToken(TokenTypes tokenType) {
        Optional<Token> tokenRecord = tokenRepository.findByTokenTypeDesc(tokenType.name());
            int validMinutes = tokenType.name() == TokenTypes.STKPUSH.name() ?  configs.getStkpush_token_advance_request_minutes() : configs.getFT_token_advance_request_minutes();
            if (tokenRecord.isPresent()) {
                if (LocalDateTime.now().plusMinutes(validMinutes).isBefore(tokenRecord.get().getExpiryDate())) {
                    return "Bearer "+tokenRecord.get().getAccessToken();
                }
            }
        return null;
    }
}
