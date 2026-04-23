package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Context 快照服务 — 将当前 {@link Context} 序列化为 JSON 并持久化到 deepay_style_chain。
 *
 * <p>每次 Agent 执行完成后调用，实现 AI 决策全程可回溯。</p>
 */
@Slf4j
@Service
public class ContextSnapshotService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    /**
     * 序列化 Context 并写入 deepay_style_chain.context_snapshot。
     * 失败不抛异常，仅打印警告，保证主流程不受影响。
     *
     * @param ctx       当前 Context
     * @param agentName 触发快照的 Agent 名称（用于日志）
     */
    public void save(Context ctx, String agentName) {
        if (ctx == null || ctx.chainCode == null) {
            return;
        }
        try {
            String json = MAPPER.writeValueAsString(ctx);
            deepayStyleChainMapper.updateContextSnapshot(ctx.chainCode, json);
            log.debug("[Snapshot] {} 快照已保存 chainCode={}", agentName, ctx.chainCode);
        } catch (JsonProcessingException e) {
            log.warn("[Snapshot] {} 序列化失败 chainCode={}", agentName, ctx.chainCode, e);
        } catch (Exception e) {
            log.warn("[Snapshot] {} 写库失败 chainCode={}", agentName, ctx.chainCode, e);
        }
    }

}
