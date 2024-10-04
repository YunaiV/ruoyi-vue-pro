package com.somle.framework.common.util.general;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

public class CoreUtils {

    private static final RetryTemplate retryTemplate = RetryTemplate.builder()
        .maxAttempts(3)
        .fixedBackoff(2000) // 2000 ms between retries
        .retryOn(Exception.class) // specify the exception to retry on
        .build();

    public static <T, E extends Throwable> T retry(RetryCallback<T, E> retryCallback) throws E {
        return retryTemplate.execute(retryCallback);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
            // Further code after the delay
            System.out.println("Task performed after delay of " + millis + " milliseconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Operation was interrupted");
        }
    }
}
