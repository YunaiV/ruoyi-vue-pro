package cn.iocoder.yudao.module.pay.dal.dataobject.wallet;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.enums.member.WalletBizTypeEnum;
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
@TableName(value ="pay_wallet_transaction")
@KeySequence("pay_wallet_transaction_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayWalletTransactionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 会员钱包 id
     *
     * 关联 {@link PayWalletDO#getId()}
     */
    private Long walletId;

    /**
     * 用户 id
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 关联业务
     *
     * 枚举 {@link WalletBizTypeEnum#getBizType()}
     */
    private Integer bizType;

    /**
     * 关联业务编号
     */
    private Long bizId;

    /**
     * 流水号
     */
    private String no;

    /**
     * 附加说明
     */
    private String description;

    /**
     * 交易金额, 单位分
     * 正值表示余额增加,负值表示余额减少
     */
    private Integer amount;

    /**
     * 交易后余额,单位分
     */
    private Integer balance;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;
}
