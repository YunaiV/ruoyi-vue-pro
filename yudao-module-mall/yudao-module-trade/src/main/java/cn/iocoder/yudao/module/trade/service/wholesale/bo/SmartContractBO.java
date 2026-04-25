package cn.iocoder.yudao.module.trade.service.wholesale.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能合同 BO
 *
 * @author deepay
 */
@Data
public class SmartContractBO {

    /** 合同编号 */
    private String contractNo;

    /** Merkle Root 哈希（SHA-256） */
    private String hash;

    /** 批发商用户编号 */
    private String wholesalerId;

    /** 合同金额（分） */
    private Integer totalAmount;

    /** 账期条款 */
    private String paymentTerms;

    /** 生成时间 */
    private LocalDateTime generatedAt;

    /** 有效期至 */
    private LocalDateTime expiresAt;

    /** 状态：PENDING / SIGNED / EXPIRED */
    private String status;

}
