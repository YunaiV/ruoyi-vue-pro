package com.somle.amazon.controller.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class AmazonAdReportReqVO {
    private String name;
    private String startDate;
    private String endDate;
    private Configuration configuration;

    @Data
    @Builder
    public static class Configuration {
        private String adProduct;
        private List<String> groupBy;
        private List<String> columns;
        private String reportTypeId;
        private String timeUnit;
        private String format;
    }
}