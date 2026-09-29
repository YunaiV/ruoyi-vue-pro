package cn.iocoder.yudao.module.ai1.tool.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.framework.ai.core.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatConversationService;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatMessageService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.provider.Ai1ProviderService;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import cn.iocoder.yudao.module.ai1.tool.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.tool.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.tool.rag.Ai1RagTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Lazy;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT;

/**
 * AI1 对话流工具：对话流的「投递 / 续传入口 + 生成 worker + 结果流 + SSE 转发」一体
 *
 * 【完整流程】
 * 1. 发送（请求侧，由 Ai1ChatMessageService 调用）：{@link #submit} 投递任务（XADD 任务队列）→ {@link #open} 打开 SSE 连接
 * 2. 生成（worker 侧，{@link #start} 拉起的消费线程）：{@link #consumeLoop} → {@link #handleTask} → {@link #generate}；
 *    增量写入结果流，结束后回填助手消息并 XACK
 * 3. 下发与续传（连接侧）：{@link #streamResult} 从结果流 XREAD 转发为 SSE；携带 lastEventId 时从断点之后重放，不重新生成
 *
 * 【关键约定】
 * 1. 助手消息编号同时作为结果流标识；生成与连接解耦，任意节点都可以转发、续传，worker 可独立扩容
 * 2. 任务携带 tenantId，worker 全程在 {@link TenantUtils#execute(Long, Runnable)} 中执行，缺少 tenantId 的任务直接拒绝；
 *    禁止使用 executeIgnore（关闭租户过滤会导致跨租户读取）
 * 3. SSE 事件的 data 统一为 JSON 字符串，避免换行、首字符空格在 SSE 协议中被截断
 * 4. 使用 Redisson 原生 RStream + StringCodec：字段按纯字符串存储（项目 RedisTemplate 的 JSON 序列化会破坏 Stream 字段）；
 *    不使用 Spring Data 的 opsForStream，因为 Redisson 适配层在阻塞读取无数据时会抛 ClassCastException，
 *    且阻塞时长超过命令超时会占满连接池
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1ChatStreamTool implements SmartLifecycle {

    /**
     * 事件：结果流标识（连接建立即下发，data 为助手消息编号）
     */
    public static final String EVENT_STREAM = "stream";
    /**
     * 事件：思考过程增量
     */
    public static final String EVENT_THINKING = "thinking";
    /**
     * 事件：回复内容增量
     */
    public static final String EVENT_MESSAGE = "message";
    /**
     * 事件：空闲心跳
     */
    public static final String EVENT_PING = "ping";
    /**
     * 事件：终态，生成结束
     */
    public static final String EVENT_DONE = "done";
    /**
     * 事件：终态，生成失败（data 为错误提示）
     */
    public static final String EVENT_ERROR = "error";

    // TODO @AI：这种，应该都放到 redis constant 里，按照项目的习惯噢；
    /**
     * 任务队列的 Stream key
     */
    private static final String TASK_STREAM_KEY = "ai1:chat:tasks";
    /**
     * 结果流 key 前缀，按助手消息编号命名
     */
    private static final String RESULT_STREAM_KEY_PREFIX = "ai1:chat:result:";
    /**
     * 任务队列的消费组
     */
    private static final String TASK_GROUP = "ai1-chat-workers";

    /**
     * 任务队列最大长度（近似裁剪）
     */
    private static final int TASK_STREAM_MAX_LENGTH = 5000;
    /**
     * XREAD 阻塞窗口，单位：毫秒；即空闲心跳间隔，需小于 Redis 超时时间
     */
    private static final long READ_BLOCK_MILLIS = 5000;
    /**
     * 结果流单次读取条数
     */
    private static final int READ_BATCH_SIZE = 50;
    /**
     * 超时任务认领间隔，单位：毫秒
     */
    private static final long RECLAIM_INTERVAL_MILLIS = 60_000;

    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_AGENT_ID = "agentId";
    private static final String FIELD_CONVERSATION_ID = "conversationId";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TENANT_ID = "tenantId";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_DATA = "data";

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private YudaoAi1Properties ai1Properties;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1ChatMessageService chatMessageService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1ChatConversationService chatConversationService;
    @Resource
    private Ai1AgentService agentService;
    @Resource
    private Ai1ProviderService providerService;
    @Resource
    private Ai1KnowledgeBaseService knowledgeBaseService;

    @Resource
    private Ai1LlmChatTool llmChatTool;
    @Resource
    private Ai1RagTool ragTool;

    @Resource
    private Ai1McpToolFactory mcpToolFactory;
    @Resource
    private Ai1SkillToolFactory skillToolFactory;

    /**
     * 结果流 TTL 上次续期时刻：助手消息编号 → 毫秒时间戳，用于 EXPIRE 节流
     */
    private final Map<Long, Long> resultExpireTimes = new ConcurrentHashMap<>();
    /**
     * 实例标识，消费者名按「实例 + 序号」隔离
     */
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
    // TODO @AI：变量注释
    private final List<Thread> workers = new ArrayList<>();
    // TODO @AI：变量注释
    private volatile boolean running = false;
    /**
     * SSE 转发线程池：单个转发阻塞于 XREAD 直到流结束，用有界池控制并发
     */
    private ThreadPoolTaskExecutor sseExecutor;

    // ==================== 生命周期 ====================

    // TODO @AI：方法有点长，是不是方法内注释加下？
    /**
     * 启动：建 SSE 转发线程池 → 初始化任务消费组 → 拉起 worker 消费线程
     *
     * 使用 SmartLifecycle 而非 @PostConstruct：worker 依赖其他 Bean 与 Redis，需在容器刷新完成后启动
     */
    @Override
    public void start() {
        // 队列容量必须为 0：否则线程池在队列未满前不会扩容到 maxPoolSize，且长连接排队同样不可接受
        int maxConnections = ai1Properties.getChat().getSse().getMaxConnections();
        // TODO @AI：hutool 里面有没工具类，可以进化下这个；
        sseExecutor = new ThreadPoolTaskExecutor();
        sseExecutor.setCorePoolSize(Math.max(1, maxConnections / 8));
        sseExecutor.setMaxPoolSize(maxConnections);
        sseExecutor.setQueueCapacity(0);
        // TODO @AI："ai1-chat-sse-" 变量注释；
        sseExecutor.setThreadNamePrefix("ai1-chat-sse-");
        sseExecutor.initialize();
        initTaskGroup();
        running = true;
        int workerCount = ai1Properties.getChat().getWorker().getCount();
        for (int i = 0; i < workerCount; i++) {
            String consumer = "worker-" + instanceId + "-" + i;
            Thread thread = new Thread(() -> consumeLoop(consumer), "ai1-chat-" + consumer);
            thread.setDaemon(true);
            workers.add(thread);
            thread.start();
        }
        log.info("[start][Chat worker 启动完成，实例({}) 数量({})]", instanceId, workerCount);
    }

    /**
     * 停止：置停止标志，等待各消费线程退出（每个最多 3s），最后关闭 SSE 转发线程池
     */
    @Override
    public void stop() {
        running = false;
        for (Thread worker : workers) {
            try {
                worker.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        workers.clear();
        if (sseExecutor != null) {
            sseExecutor.shutdown();
        }
        log.info("[stop][Chat worker 已停止，实例({})]", instanceId);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 初始化任务消费组（幂等）：Stream 不存在时一并创建（MKSTREAM），消费组已存在（BUSYGROUP）视为正常
     */
    private void initTaskGroup() {
        try {
            getTaskStream().createGroup(StreamCreateGroupArgs.name(TASK_GROUP).id(StreamMessageId.ALL).makeStream());
        } catch (Exception e) {
            if (!isBusyGroup(e)) {
                log.warn("[initTaskGroup][Chat 任务消费组初始化失败]", e);
            }
        }
    }

    private static boolean isBusyGroup(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (StrUtil.contains(cause.getMessage(), "BUSYGROUP")) {
                return true;
            }
        }
        return false;
    }

    // ==================== 投递 / 连接入口 ====================

    /**
     * 投递生成任务
     *
     * @param tenantId       租户编号，worker 以此恢复租户上下文
     * @param messageId      助手消息编号，即结果流标识
     * @param agentId        Agent 编号
     * @param conversationId 对话编号
     * @param content        用户提问内容
     */
    public void submit(Long tenantId, Long messageId, Long agentId, Long conversationId, String content) {
        // TODO @AI：方法注释？
        Map<String, String> fields = new LinkedHashMap<>();
        // TODO @AI：tenantId 是可选的噢
        fields.put(FIELD_TENANT_ID, String.valueOf(tenantId));
        fields.put(FIELD_MESSAGE_ID, String.valueOf(messageId));
        fields.put(FIELD_AGENT_ID, String.valueOf(agentId));
        // TODO @AI：貌似还有一个地方是 sesionId；是不是要统一下，都 conversationId 噢？
        fields.put(FIELD_CONVERSATION_ID, String.valueOf(conversationId));
        fields.put(FIELD_CONTENT, content);

        // TODO @AI：方法注释？
        getTaskStream().add(StreamAddArgs.entries(fields).trimNonStrict().maxLen(TASK_STREAM_MAX_LENGTH).noLimit());
    }

    /**
     * 打开 SSE 连接：从结果流 lastEventId 之后转发（为空则从头重放），生成由 worker 异步完成
     *
     * @param messageId   助手消息编号
     * @param lastEventId 已收到的最后一个事件编号，可为空
     * @return SSE 连接
     */
    public SseEmitter open(Long messageId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(ai1Properties.getChat().getStream().getTimeoutMs());
        try {
            sseExecutor.execute(() -> {
                try {
                    streamResult(emitter, messageId, lastEventId);
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("[open][助手消息({}) 对话流转发异常]", messageId, e);
                    sendErrorAndComplete(emitter, e.getMessage());
                }
            });
        } catch (TaskRejectedException e) {
            log.warn("[open][对话流转发并发已满，助手消息({})]", messageId);
            sendErrorAndComplete(emitter, "服务繁忙，请稍后重试");
        }
        return emitter;
    }

    /**
     * 返回只包含 error 终态的 SSE 连接，用于请求校验失败等场景
     *
     * @param message 错误提示
     * @return SSE 连接
     */
    public SseEmitter error(String message) {
        SseEmitter emitter = new SseEmitter(ai1Properties.getChat().getStream().getTimeoutMs());
        sendErrorAndComplete(emitter, message);
        return emitter;
    }

    // ==================== worker 消费 ====================

    /**
     * worker 主循环：周期性认领超时遗留任务，其余时间阻塞消费新任务；异常时退避 1s，保证长期稳定
     */
    // TODO @AI：方法内注释，更好的理解；
    private void consumeLoop(String consumer) {
        long lastReclaimTime = 0L;
        while (running) {
            try {
                long now = System.currentTimeMillis();
                if (now - lastReclaimTime >= RECLAIM_INTERVAL_MILLIS) {
                    lastReclaimTime = now;
                    reclaimTasks(consumer).forEach(this::handleTask);
                }
                Map<StreamMessageId, Map<String, String>> records = getTaskStream().readGroup(TASK_GROUP, consumer,
                        StreamReadGroupArgs.neverDelivered().count(1).timeout(Duration.ofMillis(READ_BLOCK_MILLIS)));
                if (records != null) {
                    records.forEach(this::handleTask);
                }
            } catch (Throwable e) {
                if (!running) {
                    return;
                }
                log.warn("[consumeLoop][Chat worker({}) 消费异常]", consumer, e);
                sleepQuietly(1000);
            }
        }
    }

    /**
     * 认领长时间未确认的任务（worker 宕机后由其他节点接管）；空闲阈值为生成最长时长 + 60s，不会误抢在途任务
     */
    // TODO @AI：方法内注释，更好的理解；
    // TODO @AI：60 000；100 是不是有个静态枚举？或者有个复用的？
    private Map<StreamMessageId, Map<String, String>> reclaimTasks(String consumer) {
        Duration minIdle = Duration.ofMillis(ai1Properties.getChat().getStream().getTimeoutMs() + 60_000);
        try {
            RStream<String, String> taskStream = getTaskStream();
            List<PendingEntry> pendingEntries = taskStream.listPending(StreamPendingRangeArgs.groupName(TASK_GROUP)
                    .startId(StreamMessageId.MIN).endId(StreamMessageId.MAX).count(100).idleTime(minIdle));
            if (CollUtil.isEmpty(pendingEntries)) {
                return Collections.emptyMap();
            }
            StreamMessageId[] ids = pendingEntries.stream().map(PendingEntry::getId).toArray(StreamMessageId[]::new);
            return taskStream.claim(TASK_GROUP, consumer, minIdle.toMillis(), TimeUnit.MILLISECONDS, ids);
        } catch (Exception e) {
            log.warn("[reclaimTasks][Chat 任务认领失败]", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 消费单条任务：解析 → 在任务租户下生成（增量写结果流）→ 回填助手消息 → 写终态 → 确认
     *
     * 成功回填完成状态；失败回填失败状态（保留已生成的部分内容）并写 error 终态；无论成败都写 done 终态并 XACK
     */
    void handleTask(StreamMessageId recordId, Map<String, String> fields) {
        // 1. 解析任务：没有消息编号的异常记录，直接确认丢弃
        Long messageId = parseLong(fields.get(FIELD_MESSAGE_ID));
        if (messageId == null) {
            ackTask(recordId);
            return;
        }
        Long tenantId = parseLong(fields.get(FIELD_TENANT_ID));
        if (tenantId == null) {
            // 缺少租户的任务无法安全读取配置，也无法回填消息，只结束结果流
            log.error("[handleTask][助手消息({}) 的任务缺少 tenantId，已拒绝]", messageId);
            appendResult(messageId, EVENT_ERROR, "任务缺少租户信息，已拒绝处理");
            appendResult(messageId, EVENT_DONE, "");
            ackTask(recordId);
            return;
        }
        Long agentId = parseLong(fields.get(FIELD_AGENT_ID));
        Long conversationId = parseLong(fields.get(FIELD_CONVERSATION_ID));
        String content = fields.get(FIELD_CONTENT);

        // 2. 在任务租户下生成并回填
        StringBuilder contentText = new StringBuilder();
        StringBuilder thinkingText = new StringBuilder();
        Consumer<String> onThinking = delta -> {
            thinkingText.append(delta);
            appendResult(messageId, EVENT_THINKING, delta);
        };
        Consumer<String> onContent = delta -> {
            contentText.append(delta);
            appendResult(messageId, EVENT_MESSAGE, delta);
        };
        try {
            TenantUtils.execute(tenantId, () -> {
                try {
                    Ai1LlmChatTool.ChatText chatText = generate(messageId, agentId, conversationId, content, onThinking, onContent);
                    saveAssistantMessage(messageId, conversationId, chatText, Ai1ChatMessageStatusEnum.SUCCESS.getStatus());
                } catch (Exception e) {
                    log.warn("[handleTask][助手消息({}) 生成失败]", messageId, e);
                    appendResult(messageId, EVENT_ERROR, StrUtil.blankToDefault(e.getMessage(), "生成失败"));
                    // 失败时回填已生成的部分内容，避免产出丢失
                    saveAssistantMessage(messageId, conversationId, new Ai1LlmChatTool.ChatText(contentText.toString(),
                            thinkingText.toString()), Ai1ChatMessageStatusEnum.FAILED.getStatus());
                }
            });
        } catch (Exception e) {
            log.error("[handleTask][助手消息({}) 回填失败]", messageId, e);
        } finally {
            appendResult(messageId, EVENT_DONE, "");
            ackTask(recordId);
        }
    }

    private void ackTask(StreamMessageId recordId) {
        getTaskStream().ack(TASK_GROUP, recordId);
    }

    // ==================== 生成编排 ====================

    /**
     * 执行一次生成：校验 Agent → 解析模型 → 装配历史、工具、RAG → 流式对话
     *
     * 历史只取当前助手占位之前最近 historyLimit 条，跳过未完成的助手占位，并移除末尾的当前提问（由 LLM 工具另行追加）
     */
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private Ai1LlmChatTool.ChatText generate(Long messageId, Long agentId, Long conversationId, String content,
                                             Consumer<String> onThinking, Consumer<String> onContent) {
        // 1. 生成可能发生在其他节点，重新校验 Agent 与模型；后台对话不要求 Agent 已发布
        Ai1AgentDO agent = agentService.validateAgentExists(agentId);
        Ai1ProviderRuntime runtime = providerService.getProviderRuntime(agent.getProviderId(), agent.getModelId());
        if (!Ai1ModelTypeEnum.isChat(runtime.getModelType())) {
            throw exception(MODEL_TYPE_NOT_CHAT);
        }

        // 2. 历史消息：按编号倒序取最近 N 条，再升序还原为对话顺序
        List<Ai1ChatMessageDO> recentMessages = new ArrayList<>(chatMessageService.getChatMessageListByConversationIdAndIdLessThan(
                conversationId, messageId, ai1Properties.getChat().getHistory().getLimit()));
        recentMessages.sort(Comparator.comparing(Ai1ChatMessageDO::getId));
        List<String[]> histories = new ArrayList<>();
        for (Ai1ChatMessageDO message : recentMessages) {
            // 跳过未完成的助手占位（如异常残留），避免把半截回复带入上下文
            if (Ai1ChatMessageRoleEnum.isAssistant(message.getRole())
                    && Ai1ChatMessageStatusEnum.isGenerating(message.getStatus())) {
                continue;
            }
            histories.add(new String[]{message.getRole(), message.getContent()});
        }
        // 末尾为本次刚落库的当前提问，移除以免重复注入
        String[] lastHistory = CollUtil.getLast(histories);
        if (lastHistory != null && Ai1ChatMessageRoleEnum.isUser(lastHistory[0])) {
            histories.remove(histories.size() - 1);
        }

        // 3. 系统指令：未配置时按名称默认引导
        // TODO @AI：system prompt 是不是必须配置的？如果是，则是不是去掉这块的逻辑么？
        String systemPrompt = StrUtil.isNotBlank(agent.getSystemPrompt()) ? agent.getSystemPrompt().trim()
                : "你是 " + StrUtil.blankToDefault(agent.getName(), "AI 助手") + " 的智能助手。";

        // 4. 工具（MCP + SKILL）与 RAG Advisor；知识库不可用时降级跳过，并把提示写入思考流
        List<Object> tools = new ArrayList<>(mcpToolFactory.buildTools(agent));
        tools.addAll(skillToolFactory.buildTools(agent));
        StringBuilder noticeText = new StringBuilder();
        List<Advisor> advisors = new ArrayList<>();
        for (Ai1KnowledgeBaseDO knowledgeBase : knowledgeBaseService.getKnowledgeBaseList(agent.getKnowledgeBaseIds())) {
            if (CommonStatusEnum.isDisable(knowledgeBase.getStatus())) {
                continue;
            }
            try {
                advisors.add(ragTool.buildAdvisor(knowledgeBase));
            } catch (Exception e) {
                log.warn("[generate][Agent({}) 知识库({}) RAG 装配失败，已降级跳过]", agent.getId(), knowledgeBase.getId(), e);
                String notice = "【知识库降级】「" + knowledgeBase.getName() + "」检索服务不可用，本次对话已跳过 RAG 上下文。\n";
                onThinking.accept(notice);
                noticeText.append(notice);
            }
        }

        // 5. 流式对话；降级提示并入最终思考文本，保证刷新后内容一致
        Ai1LlmChatTool.ChatText chatText = llmChatTool.chat(runtime, systemPrompt, histories, content, tools, advisors,
                "ai1-conversation-" + conversationId, onThinking, onContent);
        if (!noticeText.isEmpty()) {
            chatText.setThinking(noticeText + StrUtil.nullToEmpty(chatText.getThinking()));
        }
        return chatText;
    }

    /**
     * 回填助手消息（占位记录）的内容、思考过程与生成状态，并刷新对话活跃时间
     */
    private void saveAssistantMessage(Long messageId, Long conversationId, Ai1LlmChatTool.ChatText chatText, Integer status) {
        chatMessageService.updateChatMessage(new Ai1ChatMessageDO().setId(messageId)
                .setContent(StrUtil.nullToEmpty(chatText.getContent()))
                .setReasoning(StrUtil.emptyToNull(chatText.getThinking()))
                .setStatus(status));
        chatConversationService.touchChatConversation(conversationId);
    }

    // ==================== 结果流 ====================

    /**
     * 追加结果流条目并续期 TTL：生成中按半个 TTL 节流续期；终态续期一次完整 TTL 并清理节流状态
     */
    // TODO @AI：方法内注释，有点太长了。。。
    private void appendResult(Long messageId, String type, String data) {
        RStream<String, String> resultStream = getResultStream(messageId);
        resultStream.add(StreamAddArgs.entries(FIELD_TYPE, type, FIELD_DATA, StrUtil.nullToEmpty(data)));
        Duration ttl = Duration.ofSeconds(ai1Properties.getChat().getStream().getTtlSeconds());
        if (EVENT_DONE.equals(type) || EVENT_ERROR.equals(type)) {
            resultStream.expire(ttl);
            resultExpireTimes.remove(messageId);
            return;
        }
        long now = System.currentTimeMillis();
        Long lastExpireTime = resultExpireTimes.get(messageId);
        if (lastExpireTime == null || now - lastExpireTime >= ttl.toMillis() / 2) {
            resultStream.expire(ttl);
            resultExpireTimes.put(messageId, now);
        }
    }

    // ==================== SSE 转发 ====================

    /**
     * SSE 转发主循环：先下发 stream 事件告知结果流标识；随后按条目实时下发，事件编号为结果流条目编号；
     * 空闲时下发 ping 心跳；遇到终态、超时、客户端断开、Redis 连续读取失败时退出
     */
    // TODO @AI：方法内注释，有点太长了。。。
    private void streamResult(SseEmitter emitter, Long messageId, String lastEventId) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        emitter.onCompletion(() -> cancelled.set(true));
        emitter.onTimeout(() -> cancelled.set(true));
        emitter.onError(e -> cancelled.set(true));

        StreamMessageId fromId = parseStreamMessageId(lastEventId);
        long deadline = System.currentTimeMillis() + ai1Properties.getChat().getStream().getTimeoutMs();
        int errorCount = 0;
        sendEvent(emitter, EVENT_STREAM, String.valueOf(messageId), null, cancelled);
        while (!cancelled.get()) {
            if (System.currentTimeMillis() > deadline) {
                log.warn("[streamResult][助手消息({}) 对话流转发超时]", messageId);
                return;
            }
            Map<StreamMessageId, Map<String, String>> records;
            try {
                records = getResultStream(messageId).read(StreamReadArgs.greaterThan(fromId)
                        .count(READ_BATCH_SIZE).timeout(Duration.ofMillis(READ_BLOCK_MILLIS)));
            } catch (Exception e) {
                // Redis 抖动容错：连续失败达到阈值才中断，客户端可再续传
                log.warn("[streamResult][助手消息({}) 对话流读取异常]", messageId, e);
                if (++errorCount >= 3) {
                    return;
                }
                sleepQuietly(500);
                continue;
            }
            errorCount = 0;
            if (MapUtil.isEmpty(records)) {
                // 空闲心跳，防止网关、浏览器因长时间无数据断开
                sendEvent(emitter, EVENT_PING, "", null, cancelled);
                continue;
            }
            // Redisson 返回的 Map 按条目编号有序
            for (Map.Entry<StreamMessageId, Map<String, String>> record : records.entrySet()) {
                fromId = record.getKey();
                String type = record.getValue().get(FIELD_TYPE);
                sendEvent(emitter, type, record.getValue().get(FIELD_DATA), fromId.toString(), cancelled);
                if (EVENT_DONE.equals(type) || EVENT_ERROR.equals(type)) {
                    return;
                }
            }
        }
    }

    /**
     * 发送单个 SSE 事件；发送异常说明客户端已断开，置取消标志以终止转发
     */
    private static void sendEvent(SseEmitter emitter, String event, String data, String id, AtomicBoolean cancelled) {
        if (cancelled.get()) {
            return;
        }
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event().name(event).data(JsonUtils.toJsonString(StrUtil.nullToEmpty(data)));
            if (StrUtil.isNotBlank(id)) {
                builder.id(id);
            }
            emitter.send(builder);
        } catch (Exception e) {
            cancelled.set(true);
        }
    }

    private static void sendErrorAndComplete(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name(EVENT_ERROR).data(JsonUtils.toJsonString(StrUtil.blankToDefault(message, "生成失败"))));
        } catch (Exception ignored) {
            // 客户端已断开，忽略
        }
        emitter.complete();
    }

    // ==================== 通用方法 ====================

    private RStream<String, String> getTaskStream() {
        return redissonClient.getStream(TASK_STREAM_KEY, StringCodec.INSTANCE);
    }

    private RStream<String, String> getResultStream(Long messageId) {
        return redissonClient.getStream(RESULT_STREAM_KEY_PREFIX + messageId, StringCodec.INSTANCE);
    }

    /**
     * 解析结果流条目编号（格式为 毫秒时间戳-序号）；为空或非法时从头读取
     */
    static StreamMessageId parseStreamMessageId(String id) {
        if (StrUtil.isBlank(id)) {
            return new StreamMessageId(0, 0);
        }
        List<String> parts = StrUtil.split(id.trim(), '-');
        if (parts.size() != 2 || !NumberUtil.isLong(parts.get(0)) || !NumberUtil.isLong(parts.get(1))) {
            return new StreamMessageId(0, 0);
        }
        return new StreamMessageId(Long.parseLong(parts.get(0)), Long.parseLong(parts.get(1)));
    }

    // TODO @AI：是不是 hutool 有可复用的方法？
    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // TODO @AI：这种方法，是不是封装到全局里？
    private static Long parseLong(Object value) {
        String text = StrUtil.toStringOrNull(value);
        return NumberUtil.isLong(text) && Long.parseLong(text) > 0 ? Long.parseLong(text) : null;
    }

}
