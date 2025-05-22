package cn.iocoder.yudao.module.pay.api.wallet.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.Data;

/**
 * 钱包 Response DTO
 *
 * @author jason
 */
@Data
public class PayWalletRespDTO {

    /**
     * 编号
     */
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
