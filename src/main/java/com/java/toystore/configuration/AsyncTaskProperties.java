package com.java.toystore.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "toy-store.async")
public class AsyncTaskProperties {

    private int corePoolSize = 2;
    private int maxPoolSize = 4;
    private int queueCapacity = 8;
    private String threadNamePrefix = "toy-store-async-";
    private long simulatedDelayMillis = 500;
    private long resultTimeoutMillis = 2_000;
}
