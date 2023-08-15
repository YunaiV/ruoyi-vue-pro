package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

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
    @NotNull(message = "拼团活动编号不能为空")
    private Long activityId;
    /**
     * spu 编号
     */
    @NotNull(message = "spu 编号不能为空")
    private Long spuId;
    /**
     * sku 编号
     */
    @NotNull(message = "sku 编号不能为空")
    private Long skuId;
    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
    /**
     * 开团状态：正在开团 拼团成功 拼团失败
     */
    @NotNull(message = "开团状态不能为空")
    private Integer status;

}
