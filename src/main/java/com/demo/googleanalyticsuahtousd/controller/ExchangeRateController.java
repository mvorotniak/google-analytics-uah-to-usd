package com.demo.googleanalyticsuahtousd.controller;

import com.demo.googleanalyticsuahtousd.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
public class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping("/uah-to-usd")
    public ResponseEntity<BigDecimal> getUahToUsd() {
        BigDecimal uahToUsd = this.service.getUahToUsdExchangeRate();
        return ResponseEntity.ok(uahToUsd);
    }

}
