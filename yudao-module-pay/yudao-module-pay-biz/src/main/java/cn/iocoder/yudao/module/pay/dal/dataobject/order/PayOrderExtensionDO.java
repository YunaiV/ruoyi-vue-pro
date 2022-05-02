package cn.iocoder.yudao.module.pay.dal.dataobject.order;

import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.Map;

/**
 * 支付订单拓展 DO
 *
 *
 * @author 芋道源码
 */
@TableName(value = "pay_order_extension",autoResultMap = true)
@KeySequence("pay_order_extension_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderExtensionDO extends BaseDO {

    /**
     * 订单拓展编号，数据库自增
     */
    private Long id;
    /**
     * 支付订单号，根据规则生成
     * 调用支付渠道时，使用该字段作为对接的订单号。
     * 1. 调用微信支付 https://api.mch.weixin.qq.com/pay/unifiedorder 时，使用该字段作为 out_trade_no
     * 2. 调用支付宝 https://opendocs.alipay.com/apis 时，使用该字段作为 out_trade_no
     *
     * 例如说，P202110132239124200055
     */
    private String no;
    /**
     * 订单号
     *
     * 关联 {@link PayOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 渠道编号
     *
     * 关联 {@link PayChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 渠道编码
     */
    private String channelCode;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusEnum}
     * 注意，只包含上述枚举的 WAITING 和 SUCCESS
     */
    private Integer status;
    /**
     * 支付渠道的额外参数
     *
     * 参见 https://www.pingxx.com/api/支付渠道%20extra%20参数说明.html
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> channelExtras;
    /**
     * 支付渠道异步通知的内容
     *
     * 在支持成功后，会记录回调的数据
     */
    private String channelNotifyData;

}
