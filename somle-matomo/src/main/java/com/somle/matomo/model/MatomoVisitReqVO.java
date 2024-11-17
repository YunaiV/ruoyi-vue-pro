package com.somle.matomo.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatomoVisitReqVO {
    @JsonProperty("idSite")
    private String idSite;

    @JsonProperty("format")
    private String format;

    @JsonProperty("period")
    private String period;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("filter_limit")
    private int filterLimit;

    @JsonProperty("filter_offset")
    private int filterOffset;
}