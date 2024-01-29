package cn.iocoder.yudao.module.pay.dal.dataobject.demo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 示例订单
 *
 * 演示业务系统的订单，如何接入 pay 系统的支付与退款
 *
 * @author 芋道源码
 */
@TableName("pay_demo_order")
@KeySequence("pay_demo_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayDemoOrderDO extends BaseDO {

    /**
     * 订单编号，自增
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 商品编号
     */
    private Long spuId;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 价格，单位：分
     */
    private Integer price;

    // ========== 支付相关字段 ==========

    /**
     * 是否支付
     */
    private Boolean payStatus;
    /**
     * 支付订单编号
     *
     * 对接 pay-module-biz 支付服务的支付订单编号，即 PayOrderDO 的 id 编号
     */
    private Long payOrderId;
    /**
     * 付款时间
     */
    private LocalDateTime payTime;
    /**
     * 支付渠道
     *
     * 对应 PayChannelEnum 枚举
     */
    private String payChannelCode;

    // ========== 退款相关字段 ==========
    /**
     * 支付退款单号
     */
    private Long payRefundId;
    /**
     * 退款金额，单位：分
     */
    private Integer refundPrice;
    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

}
