package cn.iocoder.yudao.module.pay.dal.dataobject.wallet;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员钱包流水 DO
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
     * 流水号
     */
    private String no;

    /**
     * 钱包编号
     *
     * 关联 {@link PayWalletDO#getId()}
     */
    private Long walletId;

    /**
     * 关联业务分类
     *
     * 枚举 {@link PayWalletBizTypeEnum#getType()}
     */
    private Integer bizType;
    // TODO @jason：使用 string；因为可能有业务是 string 接入哈。
    /**
     * 关联业务编号
     */
    private Long bizId;

    // TODO @jason：想了下，改成 title；流水标题；因为账户明细那，会看到这个；
    /**
     * 附加说明
     */
    private String description;

    // TODO @jason：使用 price 哈。项目里，金额都是用这个为主；
    /**
     * 交易金额，单位分
     *
     * 正值表示余额增加，负值表示余额减少
     */
    private Integer amount;
    /**
     * 交易后余额，单位分
     */
    private Integer balance;

    // TODO @jason：使用 createTime 就够啦
    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

}
