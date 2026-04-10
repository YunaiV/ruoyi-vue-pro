package cn.iocoder.yudao.module.promotion.api.point.dto;

import lombok.Data;

/**
 * 校验参与积分商城 Response DTO
 */
@Data
public class PointValidateJoinRespDTO {

    /**
     * 可兑换次数
     */
    private Integer count;
    /**
     * 所需兑换积分
     */
    private Integer point;
    /**
     * 所需兑换金额，单位：分
     */
    private Integer price;

}
