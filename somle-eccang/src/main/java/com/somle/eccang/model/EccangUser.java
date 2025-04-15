package com.somle.eccang.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangUser {
    private Integer userId;      // 用户ID
    private String userCode;     // 登录名称
    private String userName;     // 中文名
    private String userNameEn;   // 英文名
}
