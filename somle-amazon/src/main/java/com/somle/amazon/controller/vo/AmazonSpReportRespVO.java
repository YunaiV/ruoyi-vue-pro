package com.somle.amazon.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class AmazonSpReportRespVO {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    private String reportId;
    private String reportType;
    private String dataStartTime;
    private String dataEndTime;
    private String createdTime;
    private String processingStartTime;
    private String processingEndTime;
    private String processingStatus;
    private List<String> marketplaceIds;
    private String reportDocumentId;
}