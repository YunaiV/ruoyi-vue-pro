package com.somle.eccang.model;


import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class EccangOrganization {
    private Integer id;              // 组织id
    private String nameEn;       // 英文名
    private String name;         // 中文名
    private Integer sort;            // 排序
    private Integer pid;             // 父级id
    private String level;        // 等级
    private String fatherName;   // 父级名称
}
