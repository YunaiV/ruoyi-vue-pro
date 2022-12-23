package cn.iocoder.yudao.module.trade.dal.dataobject.refund;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.refund.TradeRefundTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易退款，用于处理 {@link TradeOrderDO} 交易订单的退货换流程
 */
// TODO 芋艿：需要调整下每个字段的命名；未完全实现；
@TableName(value = "trade_refund")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TradeRefundDO extends BaseDO {

    /**
     * 交易退款编号，主键自增
     */
    @Deprecated
    private Long id;
    /**
     * 退款流水号
     *
     * 例如说，1146347329394184195
     */
    private String sn;
    /**
     * 退款状态
     *
     * 枚举 {@link TradeOrderRefundStatusEnum}
     */
    private Integer status;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 用户手机
     */
    private String userMobile;
    /**
     * 申请类型
     *
     * 枚举 {@link TradeRefundTypeEnum}
     */
    private Integer type;
    /**
     * 用户售后说明
     */
    private String reasonMemo; // buyer_msg
    /**
     * 用户售后凭证图片的地址数组
     *
     * 数组，以逗号分隔
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> reasonPicUrls; // photo_files

    // ========== 商家相关 ==========

    /**
     * 商家处理时间
     */
    private LocalDateTime handleTime; // handel_time
    /**
     * 商家拒绝理由
     */
    private String rejectReasonMemo; // seller_msg

    // ========== 交易订单相关 ==========
    /**
     * 交易订单编号
     *
     * 外键 {@link TradeOrderDO#getId()}
     */
    private Long tradeOrderId;
    /**
     * 交易订单项编号
     *
     * 关联 {@link TradeOrderItemDO#getId()}
     * 如果全部退款，则该值设置为 0 即可
     */
    private Long tradeOrderItemId;
    /**
     * 商品 SKU 编号
     */
    @Deprecated
    private Integer skuId;
    /**
     * 退货商品数量
     */
    private Integer stock; // goods_num

    // ========== 退款相关 ==========
    /**
     * 退款金额，单位：分。
     */
    private Integer refundPrice; // refund_amount
    /**
     * 支付退款编号
     *
     * 对接 pay-module-biz 支付服务的退款订单编号，即 PayRefundDO 的 id 编号
     */
    private Long payRefundId;
    // TODO 芋艿：看看是否有必要冗余，order_number、order_amount、flow_trade_no、out_refund_no、pay_type、return_money_sts、refund_time

    // ========== 退货相关 ==========
    /**
     * 退货物流公司编号
     *
     * 关联 ExpressDO 的 id 编号
     */
    private Long returnExpressId; // express_name
    /**
     * 退货物流单号
     */
    private String returnExpressNo; // express_no
    /**
     * 退货时间
     */
    private LocalDateTime returnDate; // ship_time

    // ========== 收获相关 ==========

    /**
     * 收获备注
     */
    private String receiveMemo; // receive_message
    /**
     * 收货时间
     */
    private LocalDateTime receiveDate; // receive_time

}
