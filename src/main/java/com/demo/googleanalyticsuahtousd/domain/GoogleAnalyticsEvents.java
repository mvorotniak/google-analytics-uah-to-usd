package com.demo.googleanalyticsuahtousd.domain;

import java.util.List;

public record GoogleAnalyticsEvents(String client_id, List<GoogleAnalyticsEvent> events) {
}
