package com.somle.kingdee.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeAuxInfoType {
    private String id;
    private String name;
    private String number;
    private String parentId;
    private String parentName;
    private String parentNumber;
}
