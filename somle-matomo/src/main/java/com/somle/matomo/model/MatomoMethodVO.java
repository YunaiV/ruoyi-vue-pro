package com.somle.matomo.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatomoMethodVO {
    @JsonProperty("module")
    private String module;

    @JsonProperty("method")
    private String method;

}