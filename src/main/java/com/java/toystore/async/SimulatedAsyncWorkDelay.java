package com.java.toystore.async;

import com.java.toystore.configuration.AsyncTaskProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulatedAsyncWorkDelay implements AsyncWorkDelay {

    private final AsyncTaskProperties properties;

    @Override
    public void waitForSlowOperation(String operationName) {
        try {
            Thread.sleep(properties.getSimulatedDelayMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Async operation was interrupted: " + operationName, exception);
        }
    }
}
