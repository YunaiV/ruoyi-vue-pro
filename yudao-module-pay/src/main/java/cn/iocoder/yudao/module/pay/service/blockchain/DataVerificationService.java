package cn.iocoder.yudao.module.pay.service.blockchain;

import java.util.Map;

/**
 * 数据验证服务
 * <p>
 * 对应 Python DataVerificationService：
 * - verifyOrder：比对链上存证哈希与提供的数据哈希
 * - generateVerificationLink：生成带 Redis Token 的可分享验证链接
 *
 * @author deepay
 */
public interface DataVerificationService {

    /**
     * 验证订单数据真实性
     *
     * @param orderId      订单号
     * @param providedData 待验证的数据（key-value）
     * @return 验证结果
     */
    VerificationResultDTO verifyOrder(String orderId, Map<String, Object> providedData);

    /**
     * 为订单生成可分享的验证链接（Redis Token，1 小时有效）
     *
     * @param orderId 订单号
     * @return 验证链接
     */
    String generateVerificationLink(String orderId);

    /**
     * 通过 Token 获取对应订单号（验证 Token 合法性）
     *
     * @param token 验证令牌
     * @return 订单号；token 不存在或已过期则返回 null
     */
    String resolveVerificationToken(String token);

    // ===== 内部 DTO =====

    /**
     * 验证结果 DTO
     */
    class VerificationResultDTO {
        /** 是否验证通过 */
        public boolean valid;
        /** 原因（失败时） */
        public String reason;
        /** 存储的哈希 */
        public String storedHash;
        /** 提供数据的哈希 */
        public String providedHash;
        /** 链上存证 txHash */
        public String txHash;
        /** 链类型 */
        public String chainType;
        /** 验证时间（ISO） */
        public String verificationTime;
    }

}
