package com.somle.eccang.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @className: ErpProductBoxes
 * @author: Wqh
 * @date: 2024/11/1 9:56
 * @Version: 1.0
 * @description: 箱规实体
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangProductBoxes {
    // 箱子Id: 新增时为空值，编辑时有值
    private Integer boxId;

    // 箱子中文名称
    private String boxName;

    // 箱子英文名称
    private String boxNameEn;

    // 箱长
    private String boxLength;

    // 箱宽
    private String boxWidth;

    // 箱高
    private String boxHeight;

    // 箱重
    private String boxWeight;

    // 运输方式: 0代表全部
    private String smCode;

    // 箱子是否可用: 0-不可用, 1-可用，默认为0
    private Integer boxStatus;
}
