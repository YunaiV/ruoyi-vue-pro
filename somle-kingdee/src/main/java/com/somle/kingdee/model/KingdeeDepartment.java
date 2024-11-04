package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @className: KingdeeDepartment
 * @author: Wqh
 * @date: 2024/11/1 17:38
 * @Version: 1.0
 * @description: 金蝶部门信息
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeDepartment {

    // 名称
    private String name;

    // 编码
    private String number;

    // 上级部门id
    private String parentOrgId;

    // 描述
    private String fComment;

    // 新增不填，更新时必填（id为空时代表新增单据）
    private String id;

    // 是否忽略告警信息(如：单价为0)保存，true:忽略，默认false
    private Boolean ignoreAlarm;

    // 上级部门编码
    private String parentOrgNumber;
}
