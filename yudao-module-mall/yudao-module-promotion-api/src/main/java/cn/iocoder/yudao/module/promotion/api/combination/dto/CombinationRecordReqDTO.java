package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 拼团记录 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationRecordReqDTO {

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
     * 团长编号
     */
    @NotNull(message = "团长编号不能为空")
    private Long headId;
    /**
     * 商品名字
     */
    @NotEmpty(message = "商品名字不能为空")
    private String spuName;
    /**
     * 商品图片
     */
    @NotEmpty(message = "商品图片不能为空")
    private String picUrl;
    /**
     * 拼团商品单价
     */
    @NotNull(message = "拼团商品单价不能为空")
    private Integer combinationPrice;
    /**
     * 用户昵称
     */
    @NotEmpty(message = "用户昵称不能为空")
    private String nickname;
    /**
     * 用户头像
     */
    @NotEmpty(message = "用户头像不能为空")
    private String avatar;
    /**
     * 开团状态：正在开团 拼团成功 拼团失败 TODO 等待支付
     */
    @NotNull(message = "开团状态不能为空")
    private Integer status;

}
