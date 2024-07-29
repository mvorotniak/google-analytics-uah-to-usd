package com.demo.googleanalyticsuahtousd.service;

import com.demo.googleanalyticsuahtousd.domain.ExchangeRateValueEventParam;
import com.demo.googleanalyticsuahtousd.domain.GoogleAnalyticsEvent;
import com.demo.googleanalyticsuahtousd.domain.GoogleAnalyticsEvents;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.demo.googleanalyticsuahtousd.constants.BankGovApiConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeRateService {

    @Value("${bank.gov.ua.url}")
    private String bankGovUaUrl;

    @Value("${google.analytics.host}")
    private String googleAnalyticsHost;

    @Value("${google.api.secret}")
    private String googleApiSecret;

    @Value("${google.measurement.id}")
    private String googleMeasurementId;

    @Value("${google.analytics.event}")
    private String googleEvent;

    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void sendUahToUsdGoogleAnalyticsEvent() {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(googleAnalyticsHost)
                .addPathSegment("mp")
                .addPathSegment("collect")
                .addQueryParameter("measurement_id", googleMeasurementId)
                .addQueryParameter("api_secret", googleApiSecret)
                .build();

        GoogleAnalyticsEvents events = createUahToUsdGoogleAnalyticsEvent();
        String json = objectMapper.writeValueAsString(events);

        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            log.info("Successfully sent Google Analytics event. Obtained response: [{}]", response);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected issue occurred calling google-analytics: ", e);
        }
    }
    
    private GoogleAnalyticsEvents createUahToUsdGoogleAnalyticsEvent() {
        String userId = UUID.randomUUID().toString();
        
        BigDecimal uahToUsd = this.getUahToUsdExchangeRate();
        ExchangeRateValueEventParam param = new ExchangeRateValueEventParam(uahToUsd);
        
        GoogleAnalyticsEvent event = new GoogleAnalyticsEvent(googleEvent, param);
        
        return new GoogleAnalyticsEvents(userId, List.of(event));
    }

    public BigDecimal getUahToUsdExchangeRate() {
        log.info("Performing GET request to [{}]", bankGovUaUrl);

        Request request = new Request.Builder()
                .url(bankGovUaUrl)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String stringJsonResponse = response.body().string();

            BigDecimal uahToUsd = extractUahToUsdCurrenceExchange(stringJsonResponse);

            log.info("Obtained UAH-USD exchange rate: [{}]", uahToUsd);
            return uahToUsd;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected issue occurred calling bank.gov.ua: ", e);
        }
    }

    @SneakyThrows
    private BigDecimal extractUahToUsdCurrenceExchange(String stringJsonResponse) {
        JsonNode jsonArray = objectMapper.readTree(stringJsonResponse);

        for (JsonNode jsonNode : jsonArray) {
            if (jsonNode.get(CURRENCY_CODE).asText().equals(USD)) {
                return jsonNode.get(AMOUNT).decimalValue();
            }
        }

        return null;
    }
}
