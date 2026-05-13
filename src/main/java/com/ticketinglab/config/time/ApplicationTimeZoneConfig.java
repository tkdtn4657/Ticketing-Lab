package com.ticketinglab.config.time;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class ApplicationTimeZoneConfig {

    @Value("${app.time-zone:Asia/Seoul}")
    private String timeZone;

    @PostConstruct
    public void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timeZone)));
    }
}
