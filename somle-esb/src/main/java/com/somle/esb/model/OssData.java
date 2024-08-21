package com.somle.esb.model;

import java.sql.Timestamp;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class OssData {
    private String database;
    private String tableName;
    private String syncType;
    private LocalDate folderDate;
    private long requestTimestamp;
    // private Map<String, Object> pageParam;
    private Object content;
    private Object headers;
}
