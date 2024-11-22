package com.somle.wangdian.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WangdianRespVO {
    /**
     * 状态码
     * - 0: 表示成功
     * - 其他: 表示失败
     */
    private Integer code;

    /**
     * 错误描述
     */
    private String message;

    /**
     * 数据总条数
     * - 仅当 page_no = 0 时返回
     * - 用于分页
     */
    private Integer totalCount;
}
