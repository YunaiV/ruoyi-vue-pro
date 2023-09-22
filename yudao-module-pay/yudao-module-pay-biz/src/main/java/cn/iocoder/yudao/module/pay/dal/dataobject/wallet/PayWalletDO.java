package cn.iocoder.yudao.module.pay.dal.dataobject.wallet;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员钱包 DO
 *
 * @author jason
 */
@TableName(value ="pay_wallet")
@KeySequence("pay_wallet_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayWalletDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户 id
     *
     * 关联 MemberUserDO 的 id 编号
     * 关联 AdminUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 用户类型, 预留 多商户转帐可能需要用到
     *
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 余额，单位分
     */
    private Integer balance;

    /**
     * 冻结金额，单位分
     */
    private Integer freezePrice;

    /**
     * 累计支出，单位分
     */
    private Integer totalExpense;
    /**
     * 累计充值，单位分
     */
    private Integer totalRecharge;

}
