package cn.iocoder.yudao.module.pay.dal.dataobject.member;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.enums.member.WalletOperateTypeEnum;
import cn.iocoder.yudao.module.pay.enums.member.WalletTransactionGategoryEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付-会员钱包明细 DO
 *
 * @author jason
 */
@TableName(value ="pay_member_wallet_transaction")
@KeySequence("pay_member_wallet_transaction_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class MemberWalletTransactionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 会员钱包 id
     *
     * 关联 {@link MemberWalletDO#getId()}
     */
    private Long walletId;

    /**
     * 用户 id
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 交易单号  @芋艿 这里是关联交易单号, 还是订单号 , 退款单号!  ??
     */
    private String tradeNo;

    /**
     * 交易分类
     *
     * 枚举 {@link WalletTransactionGategoryEnum#getCategory()}
     */
    private Integer category;

    /**
     * 操作分类
     *
     * 枚举 {@link WalletOperateTypeEnum#getType()}
     */
    private Integer operateType;

    /**
     * 操作详情
     */
    private String operateDesc;

    /**
     * 交易金额, 单位分
     */
    private Integer amount;

    /**
     * 余额, 单位分
     */
    private Integer balance;

    /**
     * 备注
     */
    private String mark;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;
}