package com.somle.amazon.controller.vo;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class AmazonSpReportDocumentRespVO {
    private String reportDocumentId;
    private String url;
    private CompressionAlgorithm compressionAlgorithm;

    @Getter
    @RequiredArgsConstructor
    public enum CompressionAlgorithm {
        GZIP
    }
}