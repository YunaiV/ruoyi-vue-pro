package com.somle.eccang.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @author: Wqh
 * @date: 2024/11/29 15:48
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangShippingMethod {
    /**
     * id
     */
    private Integer smId;

    /**
     * 运输方式代码
     */
    private String smCode;

    /**
     * 运输方式英文名称
     */
    private String smName;

    /**
     * 运输方式中文名称
     */
    private String smNameCn;

    /**
     * 承运商英文名称
     */
    private String smCarrierName;

    /**
     * 承运商中文名称
     */
    private String smCarrierNameCn;

    /**
     * 服务商代码
     */
    private String spCode;

    /**
     * 服务商名称
     */
    private String spName;

    /**
     * 渠道代码
     */
    private String scCode;

    /**
     * 渠道对应的API服务产品代码
     */
    private String aspCode;

    /**
     * AP服务代码
     */
    private String asCode;

    /**
     * 状态, 0:停用, 1:可用
     */
    private Integer smStatus;
}
