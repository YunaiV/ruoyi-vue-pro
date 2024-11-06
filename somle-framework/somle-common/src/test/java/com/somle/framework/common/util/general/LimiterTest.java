package com.somle.framework.common.util.general;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class LimiterTest {
    @Test
    @SneakyThrows
    void limit() {
        var limiter = new Limiter(30);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger executionCount = new AtomicInteger(0);

        // Define a supplier that increments the execution count
        Supplier<String> supplier = () -> {
            System.out.println(executionCount.incrementAndGet());
            try {
                Thread.sleep(100); // Simulate some processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Test Result";
        };

        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.MINUTES.toMillis(1);

        // Submit tasks every second for 1 minute
        while (System.currentTimeMillis() < endTime) {
            executorService.submit(() -> {
                limiter.executeWithLimiter(supplier);
            });
            Thread.sleep(500); // Wait for 1 second before submitting the next task
        }

        // Shut down the executor and wait for all tasks to complete
        executorService.shutdownNow();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Assert that the supplier was executed less than 30 times
        assertTrue(executionCount.get() < 30, "Supplier executed more than 30 times");
        System.out.println("Total executions: " + executionCount.get());
    }
}