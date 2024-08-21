package com.somle.kingdee.model;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.PropertyNamingStrategy;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeAuxInfoDetail {
    private String id;
    private String createTime;
    private String creatorId;
    private String creatorName;
    private String enable;
    private String groupId;
    private String groupName;
    private String groupNumber;
    private boolean isImport;
    private String isSystem;
    private String modifierId;
    private String modifierName;
    private String modifyTime;
    private String name;
    private String number;
    private String remark;
}
