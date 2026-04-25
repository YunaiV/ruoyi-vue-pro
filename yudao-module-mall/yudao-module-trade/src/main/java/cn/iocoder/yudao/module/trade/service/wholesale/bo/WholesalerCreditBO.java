package cn.iocoder.yudao.module.trade.service.wholesale.bo;

import lombok.Data;

/**
 * 批发商信用评估结果 BO
 *
 * @author deepay
 */
@Data
public class WholesalerCreditBO {

    /**
     * 信用等级：A / B / C / D（由会员等级 discountPercent 映射）
     * A: discountPercent >= 30；B: >= 20；C: >= 10；D: 其余
     */
    private String level;

    /** 等级显示名称 */
    private String levelName;

    /** 是否允许账期支付 */
    private boolean allowCredit;

    /** 可用信用额度（分） */
    private Integer availableCredit;

    /** 账期天数：net_30 / net_60 */
    private String paymentTerms;

    /** 是否可走快速审核通道 */
    private boolean fastTrack;

}
