package cn.iocoder.yudao.module.pay.dal.dataobject.member;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// TODO @jason：修改 MemberWalletDO 为 PayWalletDO
/**
 * 支付 - 会员钱包 DO
 *
 * @author jason
 */
@TableName(value ="pay_member_wallet")
@KeySequence("pay_member_wallet_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class MemberWalletDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    // TODO @jaosn：增加 userType 字段；
    /**
     * 用户 id
     *
     * 关联 MemberUserDO 的 id 编号
     * 关联 AdminUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 余额, 单位分
     */
    private Integer balance;

    /**
     * 累计支出, 单位分
     */
    private Integer totalSpending;

    /**
     * 累计充值, 单位分
     */
    private Integer totalTopUp;
}
