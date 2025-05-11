package cn.iocoder.yudao.module.trade.dal.dataobject.brokerage;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 佣金提现 DO
 *
 * @author 芋道源码
 */
@TableName("trade_brokerage_withdraw")
@KeySequence("trade_brokerage_withdraw_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageWithdrawDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 字段
     */
    private Long userId;

    /**
     * 提现金额，单位：分
     */
    private Integer price;
    /**
     * 提现手续费，单位：分
     */
    private Integer feePrice;
    /**
     * 当前总佣金，单位：分
     */
    private Integer totalPrice;
    /**
     * 提现类型
     * <p>
     * 枚举 {@link BrokerageWithdrawTypeEnum}
     */
    private Integer type;

    /**
     * 提现姓名
     * 1. {@link BrokerageWithdrawTypeEnum#BANK}：银行开户人
     * 2. {@link BrokerageWithdrawTypeEnum#WECHAT_API}：微信真名
     * 3. {@link BrokerageWithdrawTypeEnum#ALIPAY_API}：支付宝真名
     */
    private String userName;
    /**
     * 提现账号
     * 1. {@link BrokerageWithdrawTypeEnum#BANK}：银行账号
     * 2. {@link BrokerageWithdrawTypeEnum#WECHAT_API}：微信 openid
     * 3. {@link BrokerageWithdrawTypeEnum#ALIPAY_API}：支付宝账号
     */
    private String userAccount;

    /**
     * 收款码
     */
    private String qrCodeUrl;

    /**
     * 银行名称
     */
    private String bankName;
    /**
     * 开户地址
     */
    private String bankAddress;

    /**
     * 状态
     * <p>
     * 枚举 {@link BrokerageWithdrawStatusEnum}
     */
    private Integer status;
    /**
     * 审核驳回原因
     */
    private String auditReason;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 备注
     */
    private String remark;

    // ========== 转账相关字段 ==========

    /**
     * 转账单编号
     *
     * 关联 {@link cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO#getId()}
     */
    private Long payTransferId;
    /**
     * 转账渠道
     *
     * 枚举 {@link cn.iocoder.yudao.module.pay.enums.PayChannelEnum}
     */
    private String transferChannelCode;
    /**
     * 转账成功时间
     */
    private LocalDateTime transferTime;
    /**
     * 转账错误提示
     */
    private String transferErrorMsg;

}
