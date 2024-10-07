package cn.iocoder.yudao.module.pay.api.wallet.dto;

import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayWalletCreateReqDto {

    /**
     * 钱包编号
     */
    @NotNull(message = "钱包编号不能为空")
    private Long walletId;

    /**
     * 关联业务分类
     */
    @NotNull(message = "关联业务分类不能为空")
    private PayWalletBizTypeEnum bizType;

    /**
     * 关联业务编号
     */
    @NotNull(message = "关联业务编号不能为空")
    private String bizId;

    /**
     * 流水说明
     */
    @NotNull(message = "流水说明不能为空")
    private String title;

    /**
     * 交易金额，单位分
     *
     * 正值表示余额增加，负值表示余额减少
     */
    @NotNull(message = "交易金额不能为空")
    private Integer price;

}
