package cn.iocoder.yudao.module.pay.service.wallet.bo;

import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import lombok.Data;

/**
 * 创建钱包流水 BO
 *
 * @author jason
 */
@Data
public class WalletTransactionCreateReqBO {

    // TODO @jason：bo 的话，最好加个参数校验哈；

    /**
     * 钱包编号
     *
     */
    private Long walletId;

    /**
     * 交易金额，单位分
     *
     * 正值表示余额增加，负值表示余额减少
     */
    private Integer price;

    /**
     * 交易后余额，单位分
     */
    private Integer balance;

    /**
     * 关联业务分类
     *
     * 枚举 {@link PayWalletBizTypeEnum#getType()}
     */
    private Integer bizType;

    /**
     * 关联业务编号
     */
    private String bizId;

    /**
     * 流水说明
     */
    private String title;
}
