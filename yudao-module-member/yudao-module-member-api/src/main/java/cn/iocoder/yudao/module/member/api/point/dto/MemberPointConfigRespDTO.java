package cn.iocoder.yudao.module.member.api.point.dto;

import lombok.Data;

/**
 * 用户信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class MemberPointConfigRespDTO {

    /**
     * 积分抵扣开关
     */
    private Boolean tradeDeductEnable;
    /**
     * 积分抵扣，单位：分
     * <p>
     * 1 积分抵扣多少分
     */
    private Integer tradeDeductUnitPrice;
    /**
     * 积分抵扣最大值
     */
    private Integer tradeDeductMaxPrice;
    /**
     * 1 元赠送多少分
     */
    private Integer tradeGivePoint;

}
