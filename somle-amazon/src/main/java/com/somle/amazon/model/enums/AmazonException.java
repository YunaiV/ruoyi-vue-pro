package com.somle.amazon.model.enums;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/2/10$
 */
public class AmazonException {

    // Exception for no data found
    public static class ReportCancelledException extends RuntimeException {
        public ReportCancelledException(String message) {
            super(message);
        }
    }
}