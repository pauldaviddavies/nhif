package com.sebin.uhc.commons;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Slf4j(topic = ":: GENERAL ::CLASS-HELPER :::")
public class General {

    public static DateFormat refFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    public static String getDump(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }
    public  static  String charset = "UTF-8";
    public  static  HttpURLConnection con;
    public  static  URL urlObj;
    public  static  StringBuilder result;

    public static String getReference(String prefix) {
        return prefix+refFormat.format(new Date())+new Random().nextInt((999999 - 99) + 1) + 99;
    }

    public static String send_request(String requestId, String url, String paramsJSON, String Content_Type, String auth)
    {
        try {
            urlObj = new URL(url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", Content_Type);

            if(auth != null)
                con.setRequestProperty("Authorization", auth);

            con.setDoOutput(true);
            con.setReadTimeout(60000);
            con.setConnectTimeout(60000);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = paramsJSON.getBytes(charset);
                os.write(input, 0, input.length);
            }

            log.info("Response code is {} for reference {} at {}", con.getResponseCode(), requestId, new Date());
        } catch (Exception ex) {
            log.error("Exception {} at {} for {}", ex.getMessage(), new Date(), requestId);
            return "";
        }


        try {
            InputStream in = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line);

        } catch (IOException ex) {
            log.error("Response exception {} for request {} at {}", ex.getMessage(), requestId, new Date());
            return "";
        }
        catch (Exception ex) {
            log.error("Exception {} for request {} at {}", ex.getMessage(), requestId, new Date());
            return "";
        }

        try {
            con.disconnect();
        }
        catch (Exception ex) {
            System.out.println("Exception"+ex.toString());
            log.error("Exception {} at {} for {}", ex.getMessage(), new Date(), requestId);
            return "";
        }

        return result.toString();
    }



}

