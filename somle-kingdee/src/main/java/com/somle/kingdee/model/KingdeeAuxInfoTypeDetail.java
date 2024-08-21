package com.somle.kingdee.model;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.PropertyNamingStrategy;

import lombok.Data;
@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class KingdeeAuxInfoTypeDetail {
    private String id;
    private String createTime;
    private String creatorId;
    private String creatorName;
    private String modifierId;
    private String modifierName;
    private String modifyTime;
    private String name;
    private String number;
    private String parentId;
    private String parentName;
    private String parentNumber;
}
