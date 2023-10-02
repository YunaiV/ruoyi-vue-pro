package cn.iocoder.yudao.module.trade.dal.dataobject.aftersale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleWayEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后订单，用于处理 {@link TradeOrderDO} 交易订单的退款退货流程
 *
 * @author 芋道源码
 */
@TableName(value = "trade_after_sale", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AfterSaleDO extends BaseDO {

    /**
     * 售后编号，主键自增
     */
    private Long id;
    /**
     * 售后单号
     *
     * 例如说，1146347329394184195
     */
    private String no;
    /**
     * 退款状态
     *
     * 枚举 {@link AfterSaleStatusEnum}
     */
    private Integer status;
    /**
     * 售后方式
     *
     * 枚举 {@link AfterSaleWayEnum}
     */
    private Integer way;
    /**
     * 售后类型
     *
     * 枚举 {@link AfterSaleTypeEnum}
     */
    private Integer type;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 申请原因
     *
     * type = 退款，对应 trade_after_sale_refund_reason 类型
     * type = 退货退款，对应 trade_after_sale_refund_and_return_reason 类型
     */
    private String applyReason;
    /**
     * 补充描述
     */
    private String applyDescription;
    /**
     * 补充凭证图片
     *
     * 数组，以逗号分隔
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> applyPicUrls;

    // ========== 交易订单相关 ==========
    /**
     * 交易订单编号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 订单流水号
     *
     * 冗余 {@link TradeOrderDO#getNo()}
     */
    private String orderNo;
    /**
     * 交易订单项编号
     *
     * 关联 {@link TradeOrderItemDO#getId()}
     */
    private Long orderItemId;
    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSpuDO 的 id 字段
     * 冗余 {@link TradeOrderItemDO#getSpuId()}
     */
    private Long spuId;
    /**
     * 商品 SPU 名称
     *
     * 关联 ProductSkuDO 的 name 字段
     * 冗余 {@link TradeOrderItemDO#getSpuName()}
     */
    private String spuName;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的编号
     */
    private Long skuId;
    /**
     * 属性数组，JSON 格式
     *
     * 冗余 {@link TradeOrderItemDO#getProperties()}
     */
    @TableField(typeHandler = TradeOrderItemDO.PropertyTypeHandler.class)
    private List<TradeOrderItemDO.Property> properties;
    /**
     * 商品图片
     *
     * 冗余 {@link TradeOrderItemDO#getPicUrl()}
     */
    private String picUrl;
    /**
     * 退货商品数量
     */
    private Integer count;

    // ========== 审批相关 ==========

    /**
     * 审批时间
     */
    private LocalDateTime auditTime;
    /**
     * 审批人
     *
     * 关联 AdminUserDO 的 id 编号
     */
    private Long auditUserId;
    /**
     * 审批备注
     *
     * 注意，只有审批不通过才会填写
     */
    private String auditReason;

    // ========== 退款相关 ==========
    /**
     * 退款金额，单位：分。
     */
    private Integer refundPrice;
    /**
     * 支付退款编号
     *
     * 对接 pay-module-biz 支付服务的退款订单编号，即 PayRefundDO 的 id 编号
     */
    private Long payRefundId;
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    // ========== 退货相关 ==========
    /**
     * 退货物流公司编号
     *
     * 关联 LogisticsDO 的 id 编号
     */
    private Long logisticsId;
    /**
     * 退货物流单号
     */
    private String logisticsNo;
    /**
     * 退货时间
     */
    private LocalDateTime deliveryTime;
    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;
    /**
     * 收货备注
     *
     * 注意，只有拒绝收货才会填写
     */
    private String receiveReason;

}
