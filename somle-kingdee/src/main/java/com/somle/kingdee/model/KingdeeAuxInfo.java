package com.somle.kingdee.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeAuxInfo {
    private String id;
    private String createTime;
    private String enable;
    private String group;
    private String modifyTime;
    private String name;
    private String number;
}
