package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 对话会话服务 — 以 sessionId 为 key 在 Redis 中持久化 {@link Context}。
 *
 * <p>TTL：30 分钟（每次读写自动续期）。</p>
 * <p>key 格式：{@code chat:ctx:{sessionId}}</p>
 */
@Slf4j
@Service
public class ChatSessionService {

    private static final String KEY_PREFIX = "chat:ctx:";
    private static final long   TTL_MINUTES = 30L;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 生成新的会话 ID（UUID）。 */
    public String newSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从 Redis 加载 Context。返回 null 表示会话不存在或已过期。
     *
     * @param sessionId 会话 ID
     * @return 反序列化的 Context，或 null
     */
    public Context load(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        String key = KEY_PREFIX + sessionId;
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            Context ctx = MAPPER.readValue(json, Context.class);
            // 续期
            stringRedisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("[ChatSession] 已加载 sessionId={}", sessionId);
            return ctx;
        } catch (Exception e) {
            log.warn("[ChatSession] 加载失败 sessionId={}", sessionId, e);
            return null;
        }
    }

    /**
     * 将 Context 保存到 Redis（覆盖写入，自动设置 TTL）。
     *
     * @param sessionId 会话 ID
     * @param ctx       待保存的 Context
     */
    public void save(String sessionId, Context ctx) {
        if (!StringUtils.hasText(sessionId) || ctx == null) {
            return;
        }
        String key = KEY_PREFIX + sessionId;
        try {
            String json = MAPPER.writeValueAsString(ctx);
            stringRedisTemplate.opsForValue().set(key, json, TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("[ChatSession] 已保存 sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("[ChatSession] 保存失败 sessionId={}", sessionId, e);
        }
    }

    /**
     * 删除会话（用户主动结束对话时调用）。
     *
     * @param sessionId 会话 ID
     */
    public void delete(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        stringRedisTemplate.delete(KEY_PREFIX + sessionId);
        log.debug("[ChatSession] 已删除 sessionId={}", sessionId);
    }

}
