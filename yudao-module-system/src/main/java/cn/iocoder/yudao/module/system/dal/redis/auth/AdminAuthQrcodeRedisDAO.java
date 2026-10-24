package cn.iocoder.yudao.module.system.dal.redis.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.enums.auth.AuthQrcodeStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.AUTH_QRCODE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_QRCODE_ALREADY_CONFIRMED;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_QRCODE_INVALID;

/**
 * 扫码登录 RedisDAO，一个 Hash 保存状态、用户编号和领取凭证摘要
 *
 * @author 芋道源码
 */
@Repository
public class AdminAuthQrcodeRedisDAO {

    public static final Duration EXPIRE_TIME = Duration.ofSeconds(180);

    /**
     * 创建等待确认的二维码，并原子设置过期时间；已有编号不覆盖。
     * KEYS[1] 为二维码 key；ARGV 依次为等待状态、凭证摘要、有效秒数。
     */
    private static final DefaultRedisScript<Boolean> CREATE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then return 0 end
            redis.call('HSET', KEYS[1], 'status', ARGV[1], 'secretHash', ARGV[2])
            redis.call('EXPIRE', KEYS[1], ARGV[3])
            return 1
            """, Boolean.class);

    /**
     * 等待确认时绑定用户；同一用户重复确认直接返回，其他用户不能覆盖。
     * KEYS[1] 为二维码 key；ARGV 依次为用户编号、等待状态、确认状态；不刷新 TTL。
     */
    private static final DefaultRedisScript<String> CONFIRM_SCRIPT = new DefaultRedisScript<>("""
            local status = redis.call('HGET', KEYS[1], 'status')
            if not status then return nil end
            if status == ARGV[3] then return redis.call('HGET', KEYS[1], 'userId') end
            if status ~= ARGV[2] then return nil end
            redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'status', ARGV[3])
            return ARGV[1]
            """, String.class);

    /**
     * 校验领取凭证，返回状态和用户；已确认时原子删除，保证只能领取一次。
     * KEYS[1] 为二维码 key；ARGV 依次为凭证摘要、等待状态、确认状态。
     */
    @SuppressWarnings("unchecked") // Redis 要求 List.class；Java 无法直接表示 List<?>.class。
    private static final DefaultRedisScript<List<?>> POLL_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'secretHash') ~= ARGV[1] then return {} end
            local status = redis.call('HGET', KEYS[1], 'status')
            if status == ARGV[2] then return {status} end
            if status ~= ARGV[3] then return {} end
            local userId = redis.call('HGET', KEYS[1], 'userId')
            if not userId then return {} end
            redis.call('DEL', KEYS[1])
            return {status, userId}
            """, (Class<List<?>>) (Class<?>) List.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void create(String qrcodeId, String secretHash) {
        Boolean created = stringRedisTemplate.execute(CREATE_SCRIPT, Collections.singletonList(formatKey(qrcodeId)),
                String.valueOf(AuthQrcodeStatusEnum.WAITING.getStatus()), secretHash, String.valueOf(EXPIRE_TIME.toSeconds()));
        if (!Boolean.TRUE.equals(created)) {
            throw exception(AUTH_QRCODE_INVALID);
        }
    }

    public void confirm(String qrcodeId, Long userId) {
        String confirmedUserId = stringRedisTemplate.execute(CONFIRM_SCRIPT, Collections.singletonList(formatKey(qrcodeId)),
                String.valueOf(userId), String.valueOf(AuthQrcodeStatusEnum.WAITING.getStatus()),
                String.valueOf(AuthQrcodeStatusEnum.CONFIRMED.getStatus()));
        // 二维码过期或已被领取时，Lua 返回 nil，对应 Java 的 null。
        if (confirmedUserId == null) {
            throw exception(AUTH_QRCODE_INVALID);
        }
        if (!Objects.equals(confirmedUserId, String.valueOf(userId))) {
            throw exception(AUTH_QRCODE_ALREADY_CONFIRMED);
        }
    }

    public Pair<AuthQrcodeStatusEnum, Long> poll(String qrcodeId, String secretHash) {
        List<?> result = stringRedisTemplate.execute(POLL_SCRIPT, Collections.singletonList(formatKey(qrcodeId)),
                secretHash, String.valueOf(AuthQrcodeStatusEnum.WAITING.getStatus()),
                String.valueOf(AuthQrcodeStatusEnum.CONFIRMED.getStatus()));
        // 二维码过期、已领取或凭证不正确时，Lua 返回空列表。
        if (CollUtil.isEmpty(result)) {
            throw exception(AUTH_QRCODE_INVALID);
        }
        if (Objects.equals(String.valueOf(result.get(0)), String.valueOf(AuthQrcodeStatusEnum.WAITING.getStatus()))) {
            return Pair.of(AuthQrcodeStatusEnum.WAITING, null);
        }
        return Pair.of(AuthQrcodeStatusEnum.CONFIRMED, Long.valueOf(String.valueOf(result.get(1))));
    }

    private static String formatKey(String qrcodeId) {
        String key = String.format(AUTH_QRCODE, qrcodeId);
        Long tenantId = TenantContextHolder.getTenantId();
        // 与项目租户缓存一致，使用租户后缀隔离；关闭多租户时沿用普通 key。
        return tenantId != null ? key + ":" + tenantId : key;
    }

}
