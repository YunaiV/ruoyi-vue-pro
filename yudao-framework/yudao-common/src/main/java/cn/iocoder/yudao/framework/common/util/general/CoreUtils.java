package cn.iocoder.yudao.framework.common.util.general;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.net.SocketTimeoutException;
import java.util.Map;

public class CoreUtils {

    private static final RetryTemplate retryTemplate;

    static {
        var retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setPolicyMap(Map.of(
            HttpClientErrorException.class, new SimpleRetryPolicy(10),
            SocketTimeoutException.class, new SimpleRetryPolicy(10)
        ));

        var exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(2000);
        exponentialBackOffPolicy.setMultiplier(2);
        exponentialBackOffPolicy.setMaxInterval(180000);

        retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
    }

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
