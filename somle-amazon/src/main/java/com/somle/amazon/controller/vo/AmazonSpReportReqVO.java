package com.somle.amazon.controller.vo;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AmazonSpReportReqVO {
    private List<String> reportTypes; // Optional: Min count 1, Max count 10
    private List<ProcessingStatuses> processingStatuses; // Optional: Min count 1
    private List<String> marketplaceIds; // Optional: Min count 1, Max count 10
    private Integer pageSize = 10; // Optional: Minimum 1, Maximum 100, Default 10
    private LocalDateTime createdSince; // Optional: ISO 8601 date-time format, Default 90 days ago
    private LocalDateTime createdUntil; // Optional: ISO 8601 date-time format, Default now
    private String nextToken; // Optional: Used to fetch next page of results

    public enum ProcessingStatuses {

        CANCELLED("The report was cancelled. There are two ways a report can be cancelled: an explicit cancellation request before the report starts processing, or an automatic cancellation if there is no data to return."),
        DONE("The report has completed processing."),
        FATAL("The report was aborted due to a fatal error."),
        IN_PROGRESS("The report is being processed."),
        IN_QUEUE("The report has not yet started processing. It may be waiting for another IN_PROGRESS report.");

        private final String description;

        ProcessingStatuses(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
