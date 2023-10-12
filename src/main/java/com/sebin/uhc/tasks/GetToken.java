package com.sebin.uhc.tasks;

import com.google.gson.Gson;
import com.sebin.uhc.commons.AppConstants;
import com.sebin.uhc.commons.Configs;
import com.sebin.uhc.commons.General;
import com.sebin.uhc.entities.payments.Token;
import com.sebin.uhc.models.TokenTypes;
import com.sebin.uhc.models.responses.payments.AToken;
import com.sebin.uhc.repositories.payments.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Component
@EnableScheduling
@Slf4j
public class GetToken {

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    Configs configs;

    public String getSecret(String consumerKey, String consumerSecret)
    {
        String auth =  consumerKey+ ":" + consumerSecret;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private void updateSKTPUSHToken() {
        long tokenId = 0;
        Optional<Token> tokenRecord = tokenRepository.findByTokenTypeDesc(TokenTypes.STKPUSH.name());
        {
            if(tokenRecord.isPresent()) {
                if(LocalDateTime.now().plusMinutes(configs.getStkpush_token_advance_request_minutes()).isBefore(tokenRecord.get().getExpiryDate()))
                    return;

                tokenId = tokenRecord.get().getId();
            }
        }

        String token_resp = General.send_request("", configs.getStkpush_token_url(),"", AppConstants.APPLICATION_URL_ENCODED,getSecret(configs.getStkpush_token_consumer_key(),configs.getStkpush_token_consumer_secret()));
        if (token_resp.isBlank()) log.info("Null or empty token response");

        AToken aToken = new Gson().fromJson(token_resp, AToken.class);
        if (aToken == null || aToken.getAccess_token() == null) {
            log.info("Null or empty access token");
            return;
        }

        Token token = new Token(tokenId,aToken.getAccess_token(), TokenTypes.STKPUSH.name(),LocalDateTime.now(),LocalDateTime.now().plusSeconds(aToken.getExpires_in()));
        tokenRepository.save(token);
    }



    private void updateFundsTransferToken() {
        long tokenId = 0;
        Optional<Token> tokenRecord = tokenRepository.findByTokenTypeDesc(TokenTypes.FUNDSTRANSFER.name());
        {
            if(tokenRecord.isPresent()) {
                if(LocalDateTime.now().plusMinutes(configs.getFT_token_advance_request_minutes()).isBefore(tokenRecord.get().getExpiryDate()))
                    return;

                tokenId = tokenRecord.get().getId();
            }
        }

        String token_resp = General.send_request("", configs.getFT_token_url(),"", AppConstants.APPLICATION_URL_ENCODED,getSecret(configs.getFT_token_consumer_key(),configs.getFT_token_consumer_secret()));

        if (token_resp.isBlank()) log.info("Null or empty token response");


        AToken aToken = new Gson().fromJson(token_resp, AToken.class);
        if (aToken == null || aToken.getAccess_token() == null) {
            log.info("Could not get access token at {}", new Date());
            return;
        }

        Token token = new Token(tokenId,aToken.getAccess_token(), TokenTypes.FUNDSTRANSFER.name(),LocalDateTime.now(),LocalDateTime.now().plusSeconds(aToken.getExpires_in()));
        tokenRepository.save(token);
    }

    @Scheduled(fixedDelay = (2000)) //2s
    public void scheduleFixedDelayTask() {
         updateSKTPUSHToken();
        //updateFundsTransferToken();
    }

}
