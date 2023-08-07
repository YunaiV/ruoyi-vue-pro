package cn.iocoder.yudao.module.promotion.api.bargain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

// TODO @芋艿：这块要在看看

/**
 * 砍价记录的创建 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class BargainRecordCreateReqDTO {

    /**
     * 砍价活动编号
     */
    @NotNull(message = "砍价活动编号不能为空")
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
     * 砍价商品单价
     */
    @NotNull(message = "砍价底价不能为空")
    private Integer bargainPrice;
    /**
     * 商品原价，单位分
     */
    @NotNull(message = "商品原价不能为空")
    private Integer price;
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
     * 开团状态：进行中 砍价成功 砍价失败
     */
    @NotNull(message = "开团状态不能为空")
    private Integer status;

}
