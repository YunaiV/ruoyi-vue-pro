package com.somle.framework.common.util.general;

public class CoreUtils {

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
