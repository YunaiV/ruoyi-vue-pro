package cn.iocoder.yudao.module.pay.api.wallet.dto;

import lombok.Data;

@Data
public class PayWalletRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 余额，单位分
     */
    private Integer balance;

}
