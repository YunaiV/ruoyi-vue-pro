package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO @芋艿：这块要在看看
/**
 * 拼团记录的创建 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationRecordCreateReqDTO {

    // TODO @puhui999：注释还是要的哈

    @NotNull(message = "拼团活动编号不能为空")
    private Long activityId;

    @NotNull(message = "spu 编号不能为空")
    private Long spuId;

    @NotNull(message = "sku 编号不能为空")
    private Long skuId;

    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @NotNull(message = "团长编号不能为空")
    private Long headId;

    @NotNull(message = "拼团商品单价不能为空")
    private Integer combinationPrice;

}
