package cn.iocoder.yudao.module.deepay.service.memory;

import cn.iocoder.yudao.module.deepay.dal.mongodb.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;

/**
 * AI 聊天记忆服务。
 *
 * <h3>职责</h3>
 * <ol>
 *   <li>维护 {@code ai_chat_session}（会话元信息 + 摘要）</li>
 *   <li>维护 {@code ai_chat_message}（消息明细）</li>
 *   <li>维护 {@code ai_memory_item}（结构化长久记忆）</li>
 *   <li>提供合规删除接口（按 customerId 全量删除）</li>
 * </ol>
 *
 * <h3>写入时机</h3>
 * <ul>
 *   <li>用户发出消息后立即落 user message</li>
 *   <li>AI 完成后落 assistant 最终消息（合并）</li>
 *   <li>summary：每 {@value #SUMMARY_EVERY_N_ROUNDS} 轮或会话结束时异步更新</li>
 *   <li>memory：仅抽取允许字段并按 module 规则写入</li>
 * </ul>
 */
@Slf4j
@Service
public class AiMemoryService {

    /** 每隔多少轮对话自动生成摘要 */
    private static final int SUMMARY_EVERY_N_ROUNDS = 6;

    /** 允许落库的记忆字段（按 module） */
    private static final Map<String, Set<String>> ALLOWED_FACTS = new LinkedHashMap<>();
    static {
        ALLOWED_FACTS.put("design",  new LinkedHashSet<>(Arrays.asList(
                "stylePreference", "sizePreference", "taboos", "colorPreference", "fabricPreference")));
        ALLOWED_FACTS.put("sales",   new LinkedHashSet<>(Arrays.asList(
                "buyingMotivation", "objections", "communicationStyle", "decisionSpeed", "keyInfluencers")));
        ALLOWED_FACTS.put("finance", new LinkedHashSet<>(Arrays.asList(
                "paymentPreference", "budgetRange", "invoiceRequired", "preferredCurrency")));
        ALLOWED_FACTS.put("order",   new LinkedHashSet<>(Arrays.asList(
                "shippingPreference", "deliveryNotes", "returnPolicy", "packagingPreference")));
    }

    @Resource private AiChatSessionRepository sessionRepo;
    @Resource private AiChatMessageRepository messageRepo;
    @Resource private AiMemoryItemRepository  memoryRepo;

    // =========================================================================
    // Session
    // =========================================================================

    /**
     * 获取或创建会话。
     *
     * @param sessionId   已有会话 ID（null 时自动创建）
     * @param tenantId    租户 ID
     * @param customerId  客户 ID
     * @param module      板块
     * @return 最终使用的 sessionId
     */
    public String getOrCreateSession(String sessionId, Long tenantId, Long customerId, String module) {
        if (StringUtils.hasText(sessionId)) {
            sessionRepo.findById(sessionId).ifPresent(s -> {
                s.setLastActiveAt(Instant.now());
                sessionRepo.save(s);
            });
            return sessionId;
        }
        AiChatSessionDocument session = AiChatSessionDocument.builder()
                .tenantId(tenantId)
                .customerId(customerId)
                .module(module)
                .messageCount(0)
                .memoryEnabled(true)
                .createdAt(Instant.now())
                .lastActiveAt(Instant.now())
                .build();
        return sessionRepo.save(session).getId();
    }

    /**
     * 切换记忆开关。
     */
    public void setMemoryEnabled(String sessionId, boolean enabled) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setMemoryEnabled(enabled);
            sessionRepo.save(s);
        });
    }

    /**
     * 分页查询某客户的会话列表。
     */
    public Page<AiChatSessionDocument> listSessions(Long tenantId, Long customerId, int page, int size) {
        return sessionRepo.findByTenantIdAndCustomerIdOrderByLastActiveAtDesc(
                tenantId, customerId, PageRequest.of(page, size));
    }

    // =========================================================================
    // Message
    // =========================================================================

    /**
     * 保存 user 消息（用户发出后立即调用）。
     */
    public void saveUserMessage(String sessionId, Long tenantId, Long customerId,
                                String module, String content) {
        saveMessage(sessionId, tenantId, customerId, module, "user", content);
    }

    /**
     * 保存 assistant 最终消息（SSE 完成后调用，非流式 token）。
     */
    public void saveAssistantMessage(String sessionId, Long tenantId, Long customerId,
                                     String module, String content) {
        saveMessage(sessionId, tenantId, customerId, module, "assistant", content);
        // 更新 session 的消息计数与最后活跃时间，并判断是否触发摘要
        sessionRepo.findById(sessionId).ifPresent(s -> {
            int newCount = s.getMessageCount() + 2; // user + assistant
            s.setMessageCount(newCount);
            s.setLastActiveAt(Instant.now());
            sessionRepo.save(s);
            // 每 SUMMARY_EVERY_N_ROUNDS 轮（1轮 = user + assistant 各1条 = 2条）触发摘要
            int rounds = newCount / 2;
            if (s.isMemoryEnabled() && rounds > 0 && rounds % SUMMARY_EVERY_N_ROUNDS == 0) {
                triggerSummaryAsync(sessionId);
            }
        });
    }

    /**
     * 按会话分页查询消息。
     */
    public List<AiChatMessageDocument> listMessages(String sessionId) {
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // =========================================================================
    // Memory
    // =========================================================================

    /**
     * 更新结构化记忆（仅写入允许的字段）。
     *
     * @param tenantId   租户 ID
     * @param customerId 客户 ID
     * @param module     板块
     * @param rawFacts   原始 key-value 记忆（由 AI 抽取）
     */
    public void upsertMemory(Long tenantId, Long customerId, String module, Map<String, Object> rawFacts) {
        if (tenantId == null || customerId == null || !StringUtils.hasText(module) || rawFacts == null) {
            return;
        }
        Set<String> allowed = ALLOWED_FACTS.get(module);
        if (allowed == null) {
            log.warn("[AiMemory] 未知 module={}，跳过记忆写入", module);
            return;
        }
        // 过滤只保留允许字段
        Map<String, Object> filteredFacts = new LinkedHashMap<>();
        rawFacts.forEach((k, v) -> {
            if (allowed.contains(k) && v != null) {
                filteredFacts.put(k, v);
            }
        });
        if (filteredFacts.isEmpty()) {
            return;
        }
        AiMemoryItemDocument existing = memoryRepo
                .findByTenantIdAndCustomerIdAndModule(tenantId, customerId, module)
                .orElse(null);
        if (existing != null) {
            if (existing.getFacts() == null) {
                existing.setFacts(new LinkedHashMap<>());
            }
            existing.getFacts().putAll(filteredFacts);
            existing.setUpdatedAt(Instant.now());
            memoryRepo.save(existing);
        } else {
            AiMemoryItemDocument item = AiMemoryItemDocument.builder()
                    .tenantId(tenantId)
                    .customerId(customerId)
                    .module(module)
                    .facts(filteredFacts)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            memoryRepo.save(item);
        }
        log.debug("[AiMemory] 记忆更新 tenantId={} customerId={} module={} keys={}",
                tenantId, customerId, module, filteredFacts.keySet());
    }

    /**
     * 查询某客户所有记忆。
     */
    public List<AiMemoryItemDocument> listMemory(Long tenantId, Long customerId) {
        return memoryRepo.findByTenantIdAndCustomerId(tenantId, customerId);
    }

    /**
     * 查询某客户某板块的记忆（用于注入上下文）。
     */
    public Optional<AiMemoryItemDocument> getMemory(Long tenantId, Long customerId, String module) {
        return memoryRepo.findByTenantIdAndCustomerIdAndModule(tenantId, customerId, module);
    }

    /**
     * 删除某客户某板块的记忆。
     */
    public void deleteMemoryByModule(Long tenantId, Long customerId, String module) {
        memoryRepo.deleteByTenantIdAndCustomerIdAndModule(tenantId, customerId, module);
        log.info("[AiMemory] 删除记忆 tenantId={} customerId={} module={}", tenantId, customerId, module);
    }

    // =========================================================================
    // 合规：按 customerId 全量删除
    // =========================================================================

    /**
     * 合规删除：删除某客户在某租户下的所有聊天记录与记忆。
     *
     * @param tenantId   租户 ID
     * @param customerId 客户 ID
     */
    public void deleteAll(Long tenantId, Long customerId) {
        messageRepo.deleteByTenantIdAndCustomerId(tenantId, customerId);
        sessionRepo.deleteByTenantIdAndCustomerId(tenantId, customerId);
        memoryRepo.deleteByTenantIdAndCustomerId(tenantId, customerId);
        log.info("[AiMemory] 合规删除 tenantId={} customerId={} 全部聊天与记忆", tenantId, customerId);
    }

    /**
     * 删除会话及其消息。
     */
    public void deleteSession(String sessionId, Long tenantId) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            if (!s.getTenantId().equals(tenantId)) {
                log.warn("[AiMemory] 越权删除 sessionId={} tenantId={}", sessionId, tenantId);
                return;
            }
            messageRepo.deleteBySessionId(sessionId);
            sessionRepo.deleteById(sessionId);
            log.info("[AiMemory] 删除会话 sessionId={}", sessionId);
        });
    }

    // =========================================================================
    // 内部工具
    // =========================================================================

    private void saveMessage(String sessionId, Long tenantId, Long customerId,
                             String module, String role, String content) {
        AiChatMessageDocument msg = AiChatMessageDocument.builder()
                .sessionId(sessionId)
                .tenantId(tenantId)
                .customerId(customerId)
                .module(module)
                .role(role)
                .content(content)
                .createdAt(Instant.now())
                .build();
        messageRepo.save(msg);
    }

    /**
     * 异步生成会话摘要（简版：取最近 N 条消息拼接作为摘要）。
     * 生产环境可替换为 LLM 摘要接口。
     */
    @Async("deepayAsyncExecutor")
    public void triggerSummaryAsync(String sessionId) {
        try {
            List<AiChatMessageDocument> msgs = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
            if (msgs.isEmpty()) {
                return;
            }
            // 取最后 12 条拼接简要摘要
            int start = Math.max(0, msgs.size() - 12);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < msgs.size(); i++) {
                AiChatMessageDocument m = msgs.get(i);
                sb.append(m.getRole().equals("user") ? "用户：" : "AI：");
                String c = m.getContent();
                sb.append(c.length() > 80 ? c.substring(0, 80) + "…" : c);
                sb.append("\n");
            }
            sessionRepo.findById(sessionId).ifPresent(s -> {
                s.setSummary(sb.toString());
                sessionRepo.save(s);
            });
            log.debug("[AiMemory] 摘要更新 sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("[AiMemory] 摘要生成失败 sessionId={}", sessionId, e);
        }
    }

}
