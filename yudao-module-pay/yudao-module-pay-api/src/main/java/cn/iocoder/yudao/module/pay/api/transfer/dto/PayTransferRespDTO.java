package cn.iocoder.yudao.module.pay.api.transfer.dto;

import lombok.Data;

@Data
public class PayTransferRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 转账单号
     *
     */
    private String no;

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 转账状态
     *
     * 枚举 {@link PayTransferStatusRespEnum}
     */
    private Integer status;


}
