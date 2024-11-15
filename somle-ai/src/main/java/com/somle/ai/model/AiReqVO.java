package com.somle.ai.model;



import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiReqVO {
    private LocalDateTime createTimeBefore;
    private LocalDateTime createTimeAfter;
    private LocalDateTime updateTimeBefore;
    private LocalDateTime updateTimeAfter;
    private Integer limit;
}
