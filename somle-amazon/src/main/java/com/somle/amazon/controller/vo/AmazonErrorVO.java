package com.somle.amazon.controller.vo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AmazonErrorVO {
    private String errorIndex;
    private String errorDescription;
    private String error;
    private String requestId;
}


