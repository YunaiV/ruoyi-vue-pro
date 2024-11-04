package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @className: KingdeeDepartmentDetail
 * @author: Wqh
 * @date: 2024/11/4 8:44
 * @Version: 1.0
 * @description:
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeDepartmentDetail {
    // id
    private String id;

    // 可用状态, 1可用, 0不可用
    private String enable;

    // 备注
    private String fComment;

    // 长名称
    private String fullName;

    // 级次
    private String level;

    // 长编码
    private String longNumber;

    // 名称
    private String name;

    // 编码
    private String number;

    // 上级id
    private String parentId;

    // 上级名称
    private String parentName;

    // 上级编码
    private String parentNumber;
}
