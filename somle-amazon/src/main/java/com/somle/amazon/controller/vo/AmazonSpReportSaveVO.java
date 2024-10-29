package com.somle.amazon.controller.vo;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AmazonSpReportSaveVO {
    private String reportType; // Optional: Min count 1, Max count 10
    private List<String> marketplaceIds; // Optional: Min count 1, Max count 10
    private ReportOptions reportOptions;
    private String dataStartTime;
    private String dataEndTime;

    @Data
    @Builder
    static public class ReportOptions {
        private String asinGranularity;
        private String dateGranularity;
    }
}
