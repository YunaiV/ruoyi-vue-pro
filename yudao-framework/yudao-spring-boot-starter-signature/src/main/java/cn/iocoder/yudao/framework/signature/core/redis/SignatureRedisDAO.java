package cn.iocoder.yudao.framework.signature.core.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 验签 Redis DAO
 *
 * @author zhougang
 */
@AllArgsConstructor
public class SignatureRedisDAO {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 验签随机数
     * <p>
     * KEY 格式：signature_nonce:%s // 参数为 随机数
     * VALUE 格式：String
     * 过期时间：不固定
     */
    private static final String SIGNATURE_NONCE = "signature_nonce:%s";

    public String getNonce(String nonce) {
        return stringRedisTemplate.opsForValue().get(formatNonceKey(nonce));
    }

    public void setNonce(String nonce, long time, TimeUnit timeUnit) {
        // 将 nonce 记入缓存，防止重复使用（重点二：此处需要将 ttl 设定为允许 timestamp 时间差的值 x 2 ）
        stringRedisTemplate.opsForValue().set(formatNonceKey(nonce), nonce, time * 2, timeUnit);
    }

    private static String formatNonceKey(String key) {
        return String.format(SIGNATURE_NONCE, key);
    }
}
