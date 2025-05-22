package cn.iocoder.yudao.module.promotion.api.combination.dto;

import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团记录 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class CombinationRecordRespDTO {

    /**
     * 编号，主键自增
     */
    private Long id;

    /**
     * 拼团活动编号
     *
     * 关联 CombinationActivityDO 的 id 字段
     */
    private Long activityId;
    /**
     * 拼团商品单价
     *
     * 冗余 CombinationProductDO 的 combinationPrice 字段
     */
    private Integer combinationPrice;
    /**
     * SPU 编号
     */
    private Long spuId;
    /**
     * 商品名字
     */
    private String spuName;
    /**
     * 商品图片
     */
    private String picUrl;
    /**
     * SKU 编号
     */
    private Long skuId;
    /**
     * 购买的商品数量
     */
    private Integer count;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 团长编号
     */
    private Long headId;
    /**
     * 开团状态
     *
     * 关联 {@link CombinationRecordStatusEnum}
     */
    private Integer status;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 开团需要人数
     *
     * 关联 CombinationActivityDO 的 userSize 字段
     */
    private Integer userSize;
    /**
     * 已加入拼团人数
     */
    private Integer userCount;
    /**
     * 是否虚拟成团
     */
    private Boolean virtualGroup;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 开始时间 (订单付款后开始的时间)
     */
    private LocalDateTime startTime;
    /**
     * 结束时间（成团时间/失败时间）
     */
    private LocalDateTime endTime;

}
