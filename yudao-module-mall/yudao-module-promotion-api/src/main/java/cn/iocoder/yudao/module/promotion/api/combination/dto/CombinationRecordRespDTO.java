package cn.iocoder.yudao.module.promotion.api.combination.dto;

import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import lombok.Data;

/**
 * 拼团记录 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationRecordRespDTO {

    /**
     * 拼团活动编号
     */
    private Long activityId;
    /**
     * SPU 编号
     */
    private Long spuId;
    /**
     * SKU 编号
     */
    private Long skuId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 开团状态
     *
     * 枚举 {@link CombinationRecordStatusEnum}
     */
    private Integer status;

}
