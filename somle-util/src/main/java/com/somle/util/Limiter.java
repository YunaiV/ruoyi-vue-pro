package com.somle.util;

import lombok.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Limiter {

    private Integer counter = 0;
    private Integer limit = Integer.MAX_VALUE;
    private Integer deduct;
    private Timer timer;

    // Constructor to set custom limit and calculate deduct value
    public Limiter(Integer limit) {
        this.limit = limit;
        this.deduct = calculateDeduct(limit);
        startTimer();
    }

    // Method to calculate deduct value based on the limit
    private Integer calculateDeduct(Integer limit) {
        // Ensure that the counter decrements limit times per minute
        // 60,000 milliseconds in a minute divided by limit gives the interval
        return 60000 / limit;
    }

    // Method to increment the counter by 1
    public synchronized void incrementCounter() {
        counter++;
        notifyAll();  // Notify waiting threads after incrementing
    }

    // Method to check if counter exceeds limit
    public synchronized boolean isLimitExceeded() {
        return counter > limit;
    }

    // Method to wait until counter is below the limit
    public synchronized void waitForCounterBelowLimit() {
        while (counter >= limit) {
            try {
                wait();  // Wait until counter is below limit
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
                return;  // Exit the method if interrupted
            }
        }
    }

    // Method to wrap a code block and return the result
    public <T> T executeWithLimiter(Supplier<T> codeBlock) {
        waitForCounterBelowLimit();
//        log.debug(counter.toString());
        incrementCounter();
        return codeBlock.get();
    }

    // Method that decreases the counter by 1 every calculated interval
    private synchronized void decrementCounter() {
        if (counter > 0) {
            counter--;
            notifyAll();  // Notify waiting threads after decrementing
        }
    }

    // Method to start the timer for scheduled decrement
    private void startTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                decrementCounter();
            }
        }, deduct, deduct);
    }

    // Method to stop the timer when it's no longer needed
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
