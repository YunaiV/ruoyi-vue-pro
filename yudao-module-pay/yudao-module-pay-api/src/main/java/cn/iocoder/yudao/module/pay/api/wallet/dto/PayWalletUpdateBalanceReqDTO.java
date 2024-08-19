package cn.iocoder.yudao.module.pay.api.wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// TODO @puhui999：不在 MemberUserController 提供接口，而是 PayWalletController 增加。不然 member 耦合 pay 拉。
/**
 * 钱包余额更新 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class PayWalletUpdateBalanceReqDTO {

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 变动余额，正数为增加，负数为减少
     */
    @NotNull(message = "变动余额不能为空")
    private Integer balance;

}
