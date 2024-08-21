package com.somle.eccang.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangWarehouse {
    private int warehouseId; // 仓库ID
    private String warehouseCode; // 仓库代码
    private String warehouseName; // 仓库名称
    private int warehouseType; // 类型， 1：自营仓，2三方仓，3中转仓
    private int warehouseStatus; // 状态， 0:不可用;1:可用;-1:已废弃
}
