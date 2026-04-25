package cn.iocoder.yudao.module.pay.service.blockchain;

import cn.iocoder.yudao.module.pay.dal.dataobject.blockchain.BlockchainTaskDO;
import cn.iocoder.yudao.module.pay.dal.mysql.blockchain.BlockchainTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 数据验证服务实现
 * <p>
 * 对应 Python DataVerificationService：
 * - verifyOrder：从 pay_blockchain_task 取 dataHash，比对 Merkle Root
 * - generateVerificationLink：写 Redisson Bucket（1 h TTL）
 *
 * @author deepay
 */
@Slf4j
@Service
public class DataVerificationServiceImpl implements DataVerificationService {

    private static final String TOKEN_KEY_PREFIX = "pay_verify:token:";

    @Resource
    private BlockchainTaskMapper blockchainTaskMapper;

    @Resource
    private RedissonClient redissonClient;

    @Value("${b2b.verification.base-url:https://b2b.deepay.io/verify}")
    private String verificationBaseUrl;

    @Override
    public VerificationResultDTO verifyOrder(String orderId, Map<String, Object> providedData) {
        VerificationResultDTO result = new VerificationResultDTO();
        result.verificationTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 1. 从数据库查最新的成功存证记录
        BlockchainTaskDO task = blockchainTaskMapper.selectOne(
                new LambdaQueryWrapper<BlockchainTaskDO>()
                        .eq(BlockchainTaskDO::getOrderId, orderId)
                        .eq(BlockchainTaskDO::getStatus, "SUCCESS")
                        .orderByDesc(BlockchainTaskDO::getId)
                        .last("LIMIT 1"));

        if (task == null || task.getDataHash() == null) {
            result.valid = false;
            result.reason = "订单存证记录不存在";
            return result;
        }
        result.storedHash = task.getDataHash();
        result.txHash = task.getTxHash();
        result.chainType = task.getChainType();

        // 2. 计算提供数据的 Merkle Root
        result.providedHash = MerkleTreeUtils.computeMerkleRoot(providedData);

        // 3. 比对
        if (result.storedHash.equals(result.providedHash)) {
            result.valid = true;
            result.reason = "验证通过";
        } else {
            result.valid = false;
            result.reason = "数据哈希不匹配，数据可能已被篡改";
        }
        return result;
    }

    @Override
    public String generateVerificationLink(String orderId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_KEY_PREFIX + token;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(orderId, 1, TimeUnit.HOURS);
        log.info("生成验证链接，orderId={}, token={}", orderId, token);
        return verificationBaseUrl + "/" + orderId + "?token=" + token;
    }

    @Override
    public String resolveVerificationToken(String token) {
        String key = TOKEN_KEY_PREFIX + token;
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

}
