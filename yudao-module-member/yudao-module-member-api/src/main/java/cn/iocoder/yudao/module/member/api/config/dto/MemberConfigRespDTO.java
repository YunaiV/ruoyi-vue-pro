package cn.iocoder.yudao.module.member.api.config.dto;

import lombok.Data;

/**
 * 用户信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class MemberConfigRespDTO {

    /**
     * 积分抵扣开关
     */
    private Boolean pointTradeDeductEnable;
    /**
     * 积分抵扣，单位：分
     * <p>
     * 1 积分抵扣多少分
     */
    private Integer pointTradeDeductUnitPrice;
    /**
     * 积分抵扣最大值
     */
    private Integer pointTradeDeductMaxPrice;
    /**
     * 1 元赠送多少分
     */
    private Integer pointTradeGivePoint;

}
