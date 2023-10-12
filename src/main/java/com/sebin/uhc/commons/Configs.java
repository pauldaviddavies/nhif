package com.sebin.uhc.commons;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Data
public class Configs {

    //STK PUSH
    @Value("${stkpush.token.consumer.key}")
    private String stkpush_token_consumer_key;

    @Value("${stkpush.token.consumer.secret}")
    private String stkpush_token_consumer_secret;

    @Value("${stkpush.token.url}")
    private String stkpush_token_url;

    @Value("${stkpush.token.advance.request.minutes}")
    private int stkpush_token_advance_request_minutes;


    //FUNDS TRANSFER
    @Value("${FT.token.consumer.key}")
    private String FT_token_consumer_key;

    @Value("${FT.token.consumer.secret}")
    private String FT_token_consumer_secret;

    @Value("${FT.token.url}")
    private String FT_token_url;

    @Value("${FT.token.advance.request.minutes}")
    private int FT_token_advance_request_minutes;

    @Value("${FT.companyCode}")
    private String FT_companyCode;

    @Value("${FT.transactionType}")
    private String FT_transactionType;

    @Value("${FT.debitAccountNumber}")
    private String FT_debitAccountNumber;

    @Value("${FT.creditAccountNumber}")
    private String FT_creditAccountNumber;

    @Value("${FT.currency}")
    private String FT_currency;

    @Value("${FT.beneficiaryBankCode}")
    private String FT_beneficiaryBankCode;

    //KCB ENDPOINTS
    @Value("${vooma.opt.in.url}")
    private String vooma_opt_in_url;

    @Value("${mpesa.stk.push.url}")
    private String mpesa_stk_push_url;

    @Value("${FT.request.url}")
    private String FT_request_url;

    //SEBIN ENDPOINTS

    @Value("${sebin.mpesa.notification.url}")
    private String mpesa_payment_notification_url;

    @Value("${sebin.load.to.vooma.notification.url}")
    private String load_to_vooma_notification_url;

    //AUTHENTICATION
    @Value("${apis.consumer.key}")
    private String apis_consumer_key;

    @Value("${apis.consumer.secret}")
    private String apis_consumer_secret;


    //CONFIGS
    @Value("${mpesa.sharedShortCode}")
    private String mpesa_sharedShortCode;

    @Value("${mpesa.orgShortCode}")
    private String mpesa_orgShortCode;

    @Value("${mpesa.orgPassKey}")
    private String mpesa_orgPassKey;

    @Value("${mpesa.invoice.prefix}")
    private String mpesa_invoice_prefix;

    @Value("${tilil.sms.url}")
    private String tilil_sms_url;

    @Value("${tilil.sms.api.key}")
    private String tilil_sms_api_key;





}
