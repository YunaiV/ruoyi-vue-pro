package com.somle.amazon.controller.vo;

import com.somle.amazon.model.enums.ProcessingStatuses;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AmazonSpReportVO {
    private List<String> reportTypes; // Optional: Min count 1, Max count 10
    private List<ProcessingStatuses> processingStatuses; // Optional: Min count 1
    private List<String> marketplaceIds; // Optional: Min count 1, Max count 10
    private Integer pageSize = 10; // Optional: Minimum 1, Maximum 100, Default 10
    private LocalDateTime createdSince; // Optional: ISO 8601 date-time format, Default 90 days ago
    private LocalDateTime createdUntil; // Optional: ISO 8601 date-time format, Default now
    private String nextToken; // Optional: Used to fetch next page of results
}
