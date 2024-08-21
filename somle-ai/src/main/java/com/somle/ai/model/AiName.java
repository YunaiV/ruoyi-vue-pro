package com.somle.ai.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class AiName {
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer birthYear;
    private String language;
}
