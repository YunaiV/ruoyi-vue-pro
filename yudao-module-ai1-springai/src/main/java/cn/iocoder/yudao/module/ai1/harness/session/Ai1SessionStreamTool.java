package cn.iocoder.yudao.module.ai1.harness.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.redis.Ai1RedisKeyConstants;
import cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionStreamEventEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.memory.Ai1MemoryTool;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillShellTracker;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionMessageService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Lazy;
import org.redisson.api.RBucket;
import org.redisson.api.RStream;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * AI1 会话流工具：会话流的「投递 / 续传入口 + 生成 worker + 结果流 + SSE 转发」一体
 *
 * 【完整流程】
 * 1. 发送（请求侧，由 Ai1SessionMessageService 调用）：{@link #submit} 投递任务（XADD 任务队列）→ {@link #open} 打开 SSE 连接
 * 2. 生成（worker 侧，{@link #start} 拉起的消费线程）：{@link #consumeLoop} → {@link #handleTask} → {@link #generate}；
 *    增量写入结果流，结束后回填助手消息并 XACK
 * 3. 下发与续传（连接侧）：{@link #streamResult} 从结果流 XREAD 转发为 SSE；携带 lastEventId 时从断点之后重放，不重新生成
 * 4. 停止（请求侧）：{@link #cancel} 按助手消息写入停止意图并广播，执行节点中断生成；排队中的任务直接收尾
 *
 * 【关键约定】
 * 1. 助手消息编号同时作为结果流标识；生成与连接解耦，任意节点都可以转发、续传，worker 可独立扩容
 * 2. 投递时存在租户则携带 tenantId，worker 统一在 {@link TenantUtils#execute(Long, Runnable)} 中执行；关闭多租户时不携带，按空租户上下文执行；
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
public class Ai1SessionStreamTool implements SmartLifecycle {

    /**
     * SSE 转发线程名前缀
     */
    private static final String SSE_THREAD_NAME_PREFIX = "ai1-session-sse-";
    /**
     * 生成 worker 线程名前缀，完整线程名为「前缀 + 消费者名」
     */
    private static final String WORKER_THREAD_NAME_PREFIX = "ai1-session-";
    /**
     * 停止意图检查线程名
     */
    private static final String STOP_THREAD_NAME = "ai1-session-stop-";
    /**
     * Shell 进程回收线程名前缀，完整线程名为「前缀 + worker 线程名」
     */
    private static final String REAPER_THREAD_NAME_PREFIX = "ai1-session-reap-";

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
    /**
     * 超时任务认领的空闲阈值缓冲，单位：毫秒；空闲阈值 = SSE 连接超时 + 缓冲，执行锁另行保护在途任务
     */
    private static final long RECLAIM_IDLE_BUFFER_MILLIS = 60_000;
    /**
     * 超时任务单次认领条数
     */
    private static final int RECLAIM_BATCH_SIZE = 100;
    /**
     * worker 消费异常后的退避时长，单位：毫秒
     */
    private static final long CONSUME_ERROR_BACKOFF_MILLIS = 1000;
    /**
     * worker 停止时，等待单个消费线程退出的最长时长，单位：毫秒
     */
    private static final long WORKER_JOIN_TIMEOUT_MILLIS = 3000;
    /**
     * worker 停止时，等待全部 Shell 进程回收线程退出的最长时长，单位：毫秒；单个进程回收最多等待 5s
     */
    private static final long REAPER_JOIN_TIMEOUT_MILLIS = 6000;
    /**
     * 结果流连续读取失败的最大次数，达到后中断转发
     */
    private static final int READ_ERROR_MAX_COUNT = 3;
    /**
     * 结果流读取失败后的重试间隔，单位：毫秒
     */
    private static final long READ_ERROR_BACKOFF_MILLIS = 500;

    /**
     * 停止意图的检查间隔，单位：毫秒；兜底停止通知在订阅断开期间丢失
     */
    private static final long STOP_CHECK_INTERVAL_MILLIS = 1000;

    /**
     * 结果流丢失判定：转发到连接超时前该时长，结果流仍不存在时，判定任务丢失（预留两个阻塞读窗口，保证 error 能在连接超时前送达）
     */
    private static final long LOST_CHECK_ADVANCE_MILLIS = READ_BLOCK_MILLIS * 2;

    /**
     * 结果流丢失时的错误提示
     */
    static final String LOST_MESSAGE = "生成任务已丢失，请重新发送";

    /**
     * worker 中断后的错误提示；保留部分内容，不重新生成以免输出或工具调用重复
     */
    static final String INTERRUPTED_MESSAGE = "生成任务已中断，请重新发送";

    /**
     * 人工终止、生成失败时追加到正文的提示；失败不拼接原始异常
     */
    static final String TERMINATED_NOTICE = "本条回复已被人工终止";
    static final String FAILED_NOTICE = "本条回复生成失败";
    /**
     * 正文与提示之间的分隔
     */
    private static final String NOTICE_PREFIX = "\n\n> ";

    /**
     * SSE 转发异常的提示；只是连接异常，后台生成不受影响，可刷新后续传
     */
    static final String TRANSPORT_ERROR_MESSAGE = "会话连接异常，请刷新后重试";

    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_AGENT_ID = "agentId";
    private static final String FIELD_SESSION_ID = "sessionId";
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
    private Ai1SessionMessageService sessionMessageService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1SessionService sessionService;
    @Resource
    private Ai1AgentService agentService;
    @Resource
    private Ai1ModelService modelService;
    @Resource
    private Ai1KnowledgeBaseService knowledgeBaseService;

    @Resource
    private Ai1LlmChatTool llmChatTool;
    @Resource
    private Ai1RagTool ragTool;
    @Resource
    private Ai1MemoryTool memoryTool;

    @Resource
    private Ai1McpToolFactory mcpToolFactory;
    @Resource
    private Ai1SkillToolFactory skillToolFactory;

    /**
     * 结果流 TTL 上次续期时刻：助手消息编号 → 毫秒时间戳，用于 EXPIRE 节流
     */
    private final Map<Long, Long> resultExpireTimes = new ConcurrentHashMap<>();
    /**
     * 本节点执行中的生成：助手消息编号 → 运行上下文，供停止时中断生成线程、回收 Shell 进程
     */
    private final Map<Long, RunContext> runContexts = new ConcurrentHashMap<>();
    /**
     * 未结束的 Shell 进程回收线程：生成结束后可能仍在回收，停止时统一等待；线程结束时自行移除
     */
    private final Set<Thread> reapers = ConcurrentHashMap.newKeySet();
    /**
     * 实例标识，消费者名按「实例 + 序号」隔离
     */
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
    /**
     * 生成 worker 消费线程列表，停止时逐个等待退出
     */
    private final List<Thread> workers = new ArrayList<>();
    /**
     * 是否运行中：worker 主循环以此判断是否继续消费，停止时置为 false
     */
    private volatile boolean running = false;
    /**
     * SSE 转发线程池：单个转发阻塞于 XREAD 直到流结束，用有界池控制并发
     */
    private ExecutorService sseExecutor;
    /**
     * 停止通知的订阅编号
     */
    private Integer stopListenerId;
    /**
     * 停止意图的定时检查线程
     */
    private ScheduledExecutorService stopChecker;

    // ==================== 生命周期 ====================

    /**
     * 启动：建 SSE 转发线程池 → 初始化任务消费组 → 拉起 worker 消费线程
     *
     * 使用 SmartLifecycle 而非 @PostConstruct：worker 依赖其他 Bean 与 Redis，需在容器刷新完成后启动
     */
    @Override
    public void start() {
        // 1. 创建 SSE 转发线程池
        // 使用 SynchronousQueue（队列容量为 0）：否则线程池在队列未满前不会扩容到 maxPoolSize，且长连接排队同样不可接受；
        // 并发已满时直接拒绝（AbortPolicy），由 open 返回“服务繁忙”
        // 使用 Hutool ExecutorBuilder 构建线程池
        int maxConnections = ai1Properties.getSession().getSse().getMaxConnections();
        // 线程名前缀见 SSE_THREAD_NAME_PREFIX
        sseExecutor = ExecutorBuilder.create()
                .setCorePoolSize(Math.max(1, maxConnections / 8)).setMaxPoolSize(maxConnections)
                .useSynchronousQueue()
                .setThreadFactory(ThreadUtil.newNamedThreadFactory(SSE_THREAD_NAME_PREFIX, false))
                .build();

        // 2. 初始化任务消费组
        initTaskGroup();

        // 3. 订阅停止通知，即时中断本节点的生成；通知不持久，另定时检查停止意图兜底
        // 中断交给检查线程执行，避免 Redisson 订阅线程等待增量回调
        stopChecker = Executors.newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory(STOP_THREAD_NAME, true));
        stopChecker.scheduleWithFixedDelay(this::checkStopRequests,
                STOP_CHECK_INTERVAL_MILLIS, STOP_CHECK_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        stopListenerId = getStopTopic().addListener(String.class,
                (channel, messageId) -> stopChecker.execute(() -> cancelRunContext(Convert.toLong(messageId))));

        // 4. 拉起 worker 消费线程，消费者名按「实例 + 序号」隔离
        running = true;
        int workerCount = ai1Properties.getSession().getWorker().getCount();
        for (int i = 0; i < workerCount; i++) {
            String consumer = "worker-" + instanceId + "-" + i;
            Thread thread = ThreadUtil.newThread(() -> consumeLoop(consumer), WORKER_THREAD_NAME_PREFIX + consumer, true);
            workers.add(thread);
            thread.start();
        }
        log.info("[start][Chat worker 启动完成，实例({}) 数量({})]", instanceId, workerCount);
    }

    /**
     * 停止：置停止标志，取消停止订阅，中断本节点执行中的生成，等待各消费线程退出（每个最多 3s）、
     * Shell 进程回收（共最多 6s），最后关闭 SSE 转发线程池
     *
     * 中断的生成保留部分内容并标记失败，不会被其他节点认领后重新生成
     */
    @Override
    public void stop() {
        // 1. 先置停止标志，再在 runContexts 锁内取快照：与 registerRunContext 互斥，
        //    之后登记的上下文由登记方自行中断，不会漏掉阻塞读取刚返回、尚未登记的任务
        running = false;
        if (stopListenerId != null) {
            try {
                getStopTopic().removeListener(stopListenerId);
            } catch (Exception e) {
                log.warn("[stop][停止通知取消订阅失败]", e);
            }
        }
        if (stopChecker != null) {
            stopChecker.shutdownNow();
        }
        List<RunContext> contexts;
        synchronized (runContexts) {
            contexts = new ArrayList<>(runContexts.values());
        }
        // 中断只启动各自的回收线程，单个进程回收慢不影响其他上下文
        contexts.forEach(context -> context.cancel(INTERRUPTED_MESSAGE));
        // 2. 等待消费线程退出
        for (Thread worker : workers) {
            try {
                worker.join(WORKER_JOIN_TIMEOUT_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        workers.clear();
        // 3. 等待全部未结束的 Shell 进程回收，包括此前人工停止、生成已结束的回收；各回收线程并行，共用一个截止时刻
        long reaperDeadline = System.currentTimeMillis() + REAPER_JOIN_TIMEOUT_MILLIS;
        try {
            for (Thread reaper : reapers) {
                long remaining = reaperDeadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    break;
                }
                reaper.join(remaining);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            getTaskStream().createGroup(StreamCreateGroupArgs.name(Ai1RedisKeyConstants.SESSION_TASK_GROUP).id(StreamMessageId.ALL).makeStream());
        } catch (Exception e) {
            if (!isRedisError(e, "BUSYGROUP")) {
                log.warn("[initTaskGroup][Chat 任务消费组初始化失败]", e);
            }
        }
    }

    private static boolean isRedisError(Throwable e, String code) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (StrUtil.contains(cause.getMessage(), code)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 投递 / 连接入口 ====================

    /**
     * 投递生成任务
     *
     * @param messageId      助手消息编号，即结果流标识
     * @param agentId        Agent 编号
     * @param sessionId 会话编号
     * @param content        用户提问内容
     */
    public void submit(Long messageId, Long agentId, Long sessionId, String content) {
        // 1. 组装任务字段
        Map<String, String> fields = new LinkedHashMap<>();
        // 1.1 租户可选：存在租户才携带，关闭多租户时 worker 直接执行
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            fields.put(FIELD_TENANT_ID, String.valueOf(tenantId));
        }
        // 1.2 生成所需的业务字段
        fields.put(FIELD_MESSAGE_ID, String.valueOf(messageId));
        fields.put(FIELD_AGENT_ID, String.valueOf(agentId));
        // 生成时，请求 Header 中的 {session} 占位符替换为此处的会话编号
        fields.put(FIELD_SESSION_ID, String.valueOf(sessionId));
        fields.put(FIELD_CONTENT, content);

        // 2. 投递到任务队列，超过最大长度时近似裁剪最早的任务
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
        SseEmitter emitter = new SseEmitter(ai1Properties.getSession().getStream().getTimeoutMs());
        // 转发线程没有租户上下文，判定任务丢失后回写消息时使用
        Long tenantId = TenantContextHolder.getTenantId();
        try {
            sseExecutor.execute(() -> {
                try {
                    // 发送失败后由 Servlet 容器清理连接，不能再次 complete，避免与请求回收竞争
                    if (streamResult(emitter, messageId, lastEventId, tenantId)) {
                        emitter.complete();
                    }
                } catch (Exception e) {
                    // 只是转发异常，不改动消息状态，也不按生成失败提示
                    log.warn("[open][助手消息({}) 会话流转发异常]", messageId, e);
                    sendErrorAndComplete(emitter, TRANSPORT_ERROR_MESSAGE);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("[open][会话流转发并发已满，助手消息({})]", messageId);
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
        SseEmitter emitter = new SseEmitter(ai1Properties.getSession().getStream().getTimeoutMs());
        sendErrorAndComplete(emitter, message);
        return emitter;
    }

    // ==================== 停止 ====================

    /**
     * 停止会话中生成中的助手消息
     *
     * 按助手消息写入停止意图并广播，覆盖其他节点执行中、排队中的任务
     *
     * @param sessionId 会话编号
     */
    public void cancel(Long sessionId) {
        // 1. 按请求时刻定位生成中的助手消息；之后发送的新一轮不受影响
        List<Long> messageIds = sessionMessageService.getGeneratingAssistantMessageIdList(sessionId);
        Duration ttl = Duration.ofSeconds(ai1Properties.getSession().getStream().getTtlSeconds());
        for (Long messageId : messageIds) {
            // 2.1 写入停止意图：worker 执行前、执行中都会检查，与结果流的过期时间一致
            getStopBucket(messageId).set("1", ttl);
            // 2.2 广播停止通知，执行节点即时中断
            getStopTopic().publish(String.valueOf(messageId));
            // 2.3 没有 worker 执行时（排队中，或 worker 已退出），直接收尾
            cancelQueuedTask(messageId);
        }
    }

    /**
     * 收尾无人执行的任务：抢到执行锁说明没有 worker 在生成，保留已有结果并标记终止
     *
     * 任务记录仍在队列中，之后由 worker 按「已回填」补齐终态并确认，不再调用模型
     */
    private void cancelQueuedTask(Long messageId) {
        RLock lock = redissonClient.getLock(String.format(Ai1RedisKeyConstants.SESSION_TASK_LOCK, messageId));
        if (!lock.tryLock()) {
            return;
        }
        try {
            Ai1SessionMessageDO message = sessionMessageService.getSessionMessage(messageId);
            if (message == null || !Ai1SessionMessageStatusEnum.isGenerating(message.getStatus())) {
                return;
            }
            saveAssistantMessage(messageId, message.getSessionId(), withNotice(readPartialResult(messageId), TERMINATED_NOTICE),
                    Ai1SessionMessageStatusEnum.FAILED.getStatus());
            appendResult(messageId, Ai1SessionStreamEventEnum.DONE.getEvent(), "");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 收尾超时的孤儿生成：任务记录被裁剪或丢失时，助手消息会一直停在生成中，阻塞长期记忆归档
     *
     * 超时阈值与超时任务认领一致，仍有任务记录的消息此前已被认领收尾；
     * 先判定超时再抢执行锁，避免短暂持锁使 worker 把排队中的新任务误判为在途而跳过
     *
     * @param message 生成中的助手消息
     * @return 是否已不在生成中；仍在执行或未超时时返回 false
     * @throws IllegalStateException 收尾期间执行锁失效，由归档按单个用户失败处理
     */
    public boolean failStaleMessage(Ai1SessionMessageDO message) {
        // 1. 未超时直接保留，不加锁
        if (!isStaleMessage(message)) {
            return false;
        }
        // 2. 抢不到执行锁说明仍有 worker 在生成
        Long messageId = message.getId();
        RLock lock = redissonClient.getLock(String.format(Ai1RedisKeyConstants.SESSION_TASK_LOCK, messageId));
        if (!lock.tryLock()) {
            return false;
        }
        long ownerThreadId = Thread.currentThread().getId();
        try {
            // 3. 锁内重新读取，期间可能已被 worker 完成
            Ai1SessionMessageDO current = sessionMessageService.getSessionMessage(messageId);
            if (current == null || !Ai1SessionMessageStatusEnum.isGenerating(current.getStatus())) {
                return true;
            }
            if (!isStaleMessage(current)) {
                return false;
            }
            // 4. 与接管任务的收尾一致：保留部分结果并标记失败，再写入 error、done；
            //    每次写入前校验执行权，锁失效后由新持有者处理，不再改动消息或结果流
            log.warn("[failStaleMessage][助手消息({}) 生成超时且无 worker 执行，标记中断]", messageId);
            Ai1LlmChatTool.ChatText partialText = readPartialResult(messageId);
            checkTaskLock(lock, ownerThreadId);
            saveAssistantMessage(messageId, current.getSessionId(), withNotice(partialText, INTERRUPTED_MESSAGE),
                    Ai1SessionMessageStatusEnum.FAILED.getStatus());
            checkTaskLock(lock, ownerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.ERROR.getEvent(), INTERRUPTED_MESSAGE);
            checkTaskLock(lock, ownerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.DONE.getEvent(), "");
            return true;
        } finally {
            if (lock.isHeldByThread(ownerThreadId)) {
                lock.unlock();
            }
        }
    }

    private boolean isStaleMessage(Ai1SessionMessageDO message) {
        if (!Ai1SessionMessageRoleEnum.isAssistant(message.getRole()) || message.getCreateTime() == null) {
            return false;
        }
        long staleMillis = ai1Properties.getSession().getStream().getTimeoutMs() + RECLAIM_IDLE_BUFFER_MILLIS;
        return message.getCreateTime().isBefore(LocalDateTime.now().minus(Duration.ofMillis(staleMillis)));
    }

    /**
     * 登记运行上下文；节点已停机时立即中断
     *
     * 与 {@link #stop} 的快照在同一把锁内：要么被快照中断，要么在此看到停止标志
     */
    private void registerRunContext(Long messageId, RunContext context) {
        synchronized (runContexts) {
            runContexts.put(messageId, context);
            if (!running) {
                context.cancel(INTERRUPTED_MESSAGE);
            }
        }
    }

    /**
     * 中断本节点执行中的生成；不在本节点执行时忽略
     */
    private void cancelRunContext(Long messageId) {
        RunContext context = messageId != null ? runContexts.get(messageId) : null;
        if (context != null) {
            context.cancel(TERMINATED_NOTICE);
        }
    }

    /**
     * 定时检查本节点执行中的生成是否有停止意图，兜底订阅断开期间丢失的停止通知
     */
    private void checkStopRequests() {
        runContexts.keySet().forEach(messageId -> {
            try {
                if (getStopBucket(messageId).isExists()) {
                    cancelRunContext(messageId);
                }
            } catch (Exception e) {
                log.warn("[checkStopRequests][助手消息({}) 停止意图检查失败]", messageId, e);
            }
        });
    }

    // ==================== worker 消费 ====================

    /**
     * worker 主循环：周期性认领超时遗留任务，其余时间阻塞消费新任务；异常时退避 1s，保证长期稳定
     */
    private void consumeLoop(String consumer) {
        long lastReclaimTime = 0L;
        while (running) {
            try {
                // 1. 到达认领间隔时，先接管其他 worker 遗留的超时任务
                long now = System.currentTimeMillis();
                if (now - lastReclaimTime >= RECLAIM_INTERVAL_MILLIS) {
                    lastReclaimTime = now;
                    reclaimTasks(consumer).forEach((recordId, fields) -> handleTask(recordId, fields, true));
                }

                // 2. 阻塞读取从未投递过的新任务，每次一条，处理完再读取下一条
                Map<StreamMessageId, Map<String, String>> records = getTaskStream().readGroup(Ai1RedisKeyConstants.SESSION_TASK_GROUP, consumer,
                        StreamReadGroupArgs.neverDelivered().count(1).timeout(Duration.ofMillis(READ_BLOCK_MILLIS)));
                if (records != null) {
                    records.forEach(this::handleTask);
                }
            } catch (Throwable e) {
                // 3. 已停止时直接退出；否则记录异常并退避，避免 Redis 故障时空转
                if (!running) {
                    return;
                }
                log.warn("[consumeLoop][Chat worker({}) 消费异常]", consumer, e);
                // 任务队列被删除（如 Redis 清库）后消费组随之丢失，重建后继续消费
                if (isRedisError(e, "NOGROUP")) {
                    initTaskGroup();
                }
                ThreadUtil.sleep(CONSUME_ERROR_BACKOFF_MILLIS);
            }
        }
    }

    /**
     * 认领长时间未确认的任务；PEL 空闲时长不代表生成已停止，接管后仍需获取消息执行锁
     */
    private Map<StreamMessageId, Map<String, String>> reclaimTasks(String consumer) {
        Duration minIdle = Duration.ofMillis(ai1Properties.getSession().getStream().getTimeoutMs() + RECLAIM_IDLE_BUFFER_MILLIS);
        try {
            // 1. 查询空闲超过阈值的待确认任务
            RStream<String, String> taskStream = getTaskStream();
            List<PendingEntry> pendingEntries = taskStream.listPending(StreamPendingRangeArgs.groupName(Ai1RedisKeyConstants.SESSION_TASK_GROUP)
                    .startId(StreamMessageId.MIN).endId(StreamMessageId.MAX).count(RECLAIM_BATCH_SIZE).idleTime(minIdle));
            if (CollUtil.isEmpty(pendingEntries)) {
                return Collections.emptyMap();
            }

            // 2. 认领到当前消费者：认领时再次校验空闲时长，避免与其他节点重复认领
            StreamMessageId[] ids = pendingEntries.stream().map(PendingEntry::getId).toArray(StreamMessageId[]::new);
            return taskStream.claim(Ai1RedisKeyConstants.SESSION_TASK_GROUP, consumer, minIdle.toMillis(), TimeUnit.MILLISECONDS, ids);
        } catch (Exception e) {
            log.warn("[reclaimTasks][Chat 任务认领失败]", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 消费单条新任务
     */
    void handleTask(StreamMessageId recordId, Map<String, String> fields) {
        handleTask(recordId, fields, false);
    }

    /**
     * 消费任务：执行锁保护生成与回填；已完成的任务直接确认，中断的任务保留部分内容并标记失败
     */
    void handleTask(StreamMessageId recordId, Map<String, String> fields, boolean reclaimed) {
        // 1. 解析任务：没有消息编号的异常记录，直接确认丢弃
        Long messageId = Convert.toLong(fields.get(FIELD_MESSAGE_ID));
        if (messageId == null) {
            ackTask(recordId);
            return;
        }
        // 2. 看门狗为长任务自动续期；认领到在途任务时跳过，不确认，交由原 worker 完成
        RLock lock = redissonClient.getLock(String.format(Ai1RedisKeyConstants.SESSION_TASK_LOCK, messageId));
        if (!lock.tryLock()) {
            return;
        }
        long workerThreadId = Thread.currentThread().getId();
        try {
            Long tenantId = Convert.toLong(fields.get(FIELD_TENANT_ID));
            TenantUtils.execute(tenantId, () -> executeTask(recordId, fields, messageId, lock, workerThreadId, reclaimed));
        } catch (Exception e) {
            // Redis 或回填失败时保留 pending，后续接管不会重新生成
            log.error("[handleTask][助手消息({}) 处理失败]", messageId, e);
        } finally {
            resultExpireTimes.remove(messageId);
            try {
                if (lock.isHeldByThread(workerThreadId)) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.warn("[handleTask][助手消息({}) 执行锁释放失败]", messageId, e);
            }
        }
    }

    /**
     * 持有执行锁后处理任务：清理异常记录、补齐已完成任务、收尾中断任务，或执行新一轮生成
     */
    private void executeTask(StreamMessageId recordId, Map<String, String> fields, Long messageId,
                             RLock lock, long workerThreadId, boolean reclaimed) {
        // 1.1 查询助手消息，不存在或不是助手角色时确认丢弃
        Ai1SessionMessageDO message = sessionMessageService.getSessionMessage(messageId);
        if (message == null || !Ai1SessionMessageRoleEnum.isAssistant(message.getRole())) {
            checkTaskLock(lock, workerThreadId);
            ackTask(recordId);
            return;
        }
        // 1.2 回填成功后 XACK 可能失败，补齐缺失终态后确认，不再调用模型
        if (!Ai1SessionMessageStatusEnum.isGenerating(message.getStatus())) {
            completeTaskResult(message, lock, workerThreadId);
            checkTaskLock(lock, workerThreadId);
            ackTask(recordId);
            return;
        }
        // 1.3 接管任务不重跑，保留结果流中的部分输出；新任务已有结果时同样按中断处理
        Long sessionId = Convert.toLong(fields.get(FIELD_SESSION_ID));
        if (reclaimed || getResultStream(messageId).isExists()) {
            // 1.3.1 按结果流顺序恢复已产生的正文和思考
            Ai1LlmChatTool.ChatText partialText = readPartialResult(messageId);
            // 1.3.2 回填部分结果并标记失败；每次写入前校验执行权，防止旧 worker 继续写入
            checkTaskLock(lock, workerThreadId);
            saveAssistantMessage(messageId, sessionId, withNotice(partialText, INTERRUPTED_MESSAGE),
                    Ai1SessionMessageStatusEnum.FAILED.getStatus());
            // 1.3.3 写入错误提示和结束事件，续传连接可及时退出
            checkTaskLock(lock, workerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.ERROR.getEvent(), INTERRUPTED_MESSAGE);
            checkTaskLock(lock, workerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.DONE.getEvent(), "");
            // 1.3.4 确认任务；以上任一步失败时保留 pending，交由后续接管收尾
            checkTaskLock(lock, workerThreadId);
            ackTask(recordId);
            return;
        }

        // 2. 解析任务字段
        Long agentId = Convert.toLong(fields.get(FIELD_AGENT_ID));
        String content = fields.get(FIELD_CONTENT);

        // 3. 登记运行上下文，供停止时中断生成线程、回收 Shell 进程
        RunContext context = new RunContext(Thread.currentThread(), skillToolFactory.newShellTracker(agentId), reapers);
        registerRunContext(messageId, context);
        // 增量回调可能在模型线程执行，因此按 worker 线程编号校验锁归属；停止后不再写入增量
        StringBuilder contentText = new StringBuilder();
        StringBuilder thinkingText = new StringBuilder();
        Consumer<String> onThinking = delta -> {
            synchronized (context) {
                context.checkActive();
                checkTaskLock(lock, workerThreadId);
                thinkingText.append(delta);
                appendResult(messageId, Ai1SessionStreamEventEnum.THINKING.getEvent(), delta);
            }
        };
        Consumer<String> onContent = delta -> {
            synchronized (context) {
                context.checkActive();
                checkTaskLock(lock, workerThreadId);
                contentText.append(delta);
                appendResult(messageId, Ai1SessionStreamEventEnum.MESSAGE.getEvent(), delta);
            }
        };

        // 4. 标记生成开始，再调用模型
        Ai1LlmChatTool.ChatText chatText = null;
        Exception failure = null;
        String stopNotice;
        try {
            // 4.1 节点已停机，或排队期间已被停止时，不再调用模型
            context.checkActive();
            if (getStopBucket(messageId).isExists()) {
                context.cancel(TERMINATED_NOTICE);
            }
            context.checkActive();
            // 4.2 标记生成已开始；长思考没有增量时，续传也不会误判结果流丢失
            checkTaskLock(lock, workerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.PING.getEvent(), "");
            chatText = generate(context, messageId, agentId, sessionId, content, onThinking, onContent);
        } catch (Exception e) {
            failure = e;
        } finally {
            // 4.3 结束上下文并清除中断标志，之后的停止不再生效
            stopNotice = context.finish();
            runContexts.remove(messageId, context);
            context.getShellTracker().cancel();
        }

        // 5. 先回填终态，再下发 error / done；每次写入前校验执行权
        if (stopNotice != null) {
            // 5.1 停止：保留已生成的内容并追加提示；人工终止只写 done，节点停机中断另写 error
            log.info("[handleTask][助手消息({}) 生成已停止：{}]", messageId, stopNotice);
            Ai1LlmChatTool.ChatText partialText = chatText != null ? chatText
                    : new Ai1LlmChatTool.ChatText(contentText.toString(), thinkingText.toString());
            checkTaskLock(lock, workerThreadId);
            saveAssistantMessage(messageId, sessionId, withNotice(partialText, stopNotice), Ai1SessionMessageStatusEnum.FAILED.getStatus());
            if (!TERMINATED_NOTICE.equals(stopNotice)) {
                checkTaskLock(lock, workerThreadId);
                appendResult(messageId, Ai1SessionStreamEventEnum.ERROR.getEvent(), stopNotice);
            }
        } else if (failure != null) {
            // 5.2 失败：失去锁的旧 worker 不得再写结果、终态或 XACK，留给新持有者处理
            if (!lock.isHeldByThread(workerThreadId)) {
                return;
            }
            log.warn("[handleTask][助手消息({}) 生成失败]", messageId, failure);
            saveAssistantMessage(messageId, sessionId, withNotice(new Ai1LlmChatTool.ChatText(contentText.toString(),
                    thinkingText.toString()), FAILED_NOTICE), Ai1SessionMessageStatusEnum.FAILED.getStatus());
            checkTaskLock(lock, workerThreadId);
            appendResult(messageId, Ai1SessionStreamEventEnum.ERROR.getEvent(), FAILED_NOTICE);
        } else {
            // 5.3 成功：回填完整结果
            checkTaskLock(lock, workerThreadId);
            saveAssistantMessage(messageId, sessionId, chatText, Ai1SessionMessageStatusEnum.SUCCESS.getStatus());
        }
        // 6. 回填后写终态并确认；失败时由后续接管清理，不能在 finally 中无条件确认
        checkTaskLock(lock, workerThreadId);
        appendResult(messageId, Ai1SessionStreamEventEnum.DONE.getEvent(), "");
        checkTaskLock(lock, workerThreadId);
        ackTask(recordId);
    }

    /**
     * 已回填任务的终态补偿：只补齐缺失的 error/done，不再次生成或回填消息
     *
     * 数据库回填与结果流写入不是同一事务，worker 可能在两者之间退出。
     */
    private void completeTaskResult(Ai1SessionMessageDO message, RLock lock, long workerThreadId) {
        // 1. 只查询最后一条事件，已有 done 则无需再补写
        Map<StreamMessageId, Map<String, String>> last = getResultStream(message.getId())
                .rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1));
        String lastType = last.isEmpty() ? null : CollUtil.getFirst(last.values()).get(FIELD_TYPE);
        if (Ai1SessionStreamEventEnum.DONE.getEvent().equals(lastType)) {
            return;
        }
        // 2. 失败消息缺少 error 时，按正文中已回填的提示补写；人工终止只写 done，末尾已有 error 时不重复写入
        if (Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())
                && !Ai1SessionStreamEventEnum.ERROR.getEvent().equals(lastType)) {
            String notice = getSavedNotice(message.getContent());
            if (!TERMINATED_NOTICE.equals(notice)) {
                checkTaskLock(lock, workerThreadId);
                appendResult(message.getId(), Ai1SessionStreamEventEnum.ERROR.getEvent(),
                        ObjUtil.defaultIfNull(notice, INTERRUPTED_MESSAGE));
            }
        }
        // 3. 补写 done，使续传连接能够结束；调用方在补齐后再确认任务
        checkTaskLock(lock, workerThreadId);
        appendResult(message.getId(), Ai1SessionStreamEventEnum.DONE.getEvent(), "");
    }

    /**
     * 校验执行锁仍属于原 worker；增量回调可能在模型线程执行，因此不能使用当前回调线程编号
     *
     * 失去执行权时抛出异常，中止旧 worker 的后续写入，由新持有者收尾。
     */
    private static void checkTaskLock(RLock lock, long workerThreadId) {
        if (!lock.isHeldByThread(workerThreadId)) {
            throw new IllegalStateException("生成任务执行权已失效");
        }
    }

    /**
     * 按结果流顺序还原中断任务的正文和思考，忽略心跳、错误提示及结束事件
     */
    private Ai1LlmChatTool.ChatText readPartialResult(Long messageId) {
        StringBuilder content = new StringBuilder();
        StringBuilder thinking = new StringBuilder();
        // 读取完整结果流，按条目编号升序拼接各类增量
        getResultStream(messageId).range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))
                .values().forEach(fields -> {
                    if (Ai1SessionStreamEventEnum.MESSAGE.getEvent().equals(fields.get(FIELD_TYPE))) {
                        content.append(StrUtil.nullToEmpty(fields.get(FIELD_DATA)));
                    } else if (Ai1SessionStreamEventEnum.THINKING.getEvent().equals(fields.get(FIELD_TYPE))) {
                        thinking.append(StrUtil.nullToEmpty(fields.get(FIELD_DATA)));
                    }
                });
        return new Ai1LlmChatTool.ChatText(content.toString(), thinking.toString());
    }

    private void ackTask(StreamMessageId recordId) {
        getTaskStream().ack(Ai1RedisKeyConstants.SESSION_TASK_GROUP, recordId);
    }

    // ==================== 生成编排 ====================

    /**
     * 执行一次生成：校验 Agent → 解析模型 → 装配历史、长期记忆、工具、RAG → 流式会话
     *
     * 历史只取当前助手占位之前最近 historyLimit 条，跳过未完成的助手占位，并移除末尾的当前提问（由 LLM 工具另行追加）
     */
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private Ai1LlmChatTool.ChatText generate(RunContext context, Long messageId, Long agentId, Long sessionId, String content,
                                             Consumer<String> onThinking, Consumer<String> onContent) {
        // 1. 生成可能发生在其他节点，重新校验 Agent 与模型；后台会话不要求 Agent 已发布
        Ai1AgentDO agent = agentService.validateAgentExists(agentId);
        Ai1ModelRespBO model = modelService.getModelRespBO(agent.getProviderId(), agent.getModelId());
        if (!Ai1ModelTypeEnum.isChat(model.getModelType())) {
            throw ServiceExceptionUtil.exception(Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT);
        }

        // 2. 历史消息：按编号倒序取最近 N 条，再升序还原为会话顺序
        // TODO @芋艿：【优化点】截取历史的 limit 会导致 prompt cache 频繁失效。后续需要改成 prompt 压缩
        List<Ai1SessionMessageDO> recentMessages = new ArrayList<>(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(
                sessionId, messageId, ai1Properties.getSession().getHistory().getLimit()));
        recentMessages.sort(Comparator.comparing(Ai1SessionMessageDO::getId));
        List<String[]> histories = new ArrayList<>();
        for (Ai1SessionMessageDO message : recentMessages) {
            // 跳过未完成的助手占位（如异常残留），避免把半截回复带入上下文
            if (Ai1SessionMessageRoleEnum.isAssistant(message.getRole())
                    && Ai1SessionMessageStatusEnum.isGenerating(message.getStatus())) {
                continue;
            }
            histories.add(new String[]{message.getRole(), message.getContent()});
        }
        // 末尾为本次刚落库的当前提问，移除以免重复注入
        String[] lastHistory = CollUtil.getLast(histories);
        if (lastHistory != null && Ai1SessionMessageRoleEnum.isUser(lastHistory[0])) {
            histories.remove(histories.size() - 1);
        }

        // 3. 系统指令：未配置时使用默认引导文案
        String systemPrompt = StrUtil.isNotBlank(agent.getSystemPrompt()) ? agent.getSystemPrompt().trim()
                : StrUtil.replace(ai1Properties.getSession().getDefaultSystemPrompt(),
                YudaoAi1Properties.Session.AGENT_NAME_PLACEHOLDER, agent.getName());
        // 开启长期记忆时，合入用户在该 Agent 下的记忆
        systemPrompt = injectMemory(agent, sessionId, systemPrompt);

        // 4. 工具（MCP + SKILL）与 RAG Advisor；知识库不可用时降级跳过，并把提示写入思考流
        // Shell 命令经运行上下文的跟踪器执行，停止或生成结束时统一回收
        List<Object> tools = new ArrayList<>(mcpToolFactory.buildTools(agent));
        tools.addAll(skillToolFactory.buildTools(agent, context.getShellTracker()));
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
                String notice = "【知识库降级】「" + knowledgeBase.getName() + "」检索服务不可用，本次会话已跳过 RAG 上下文。\n";
                onThinking.accept(notice);
                noticeText.append(notice);
            }
        }

        // 5. 流式会话；降级提示并入最终思考文本，保证刷新后内容一致
        context.checkActive();
        Ai1LlmChatTool.ChatText chatText = llmChatTool.chat(model, systemPrompt, histories, content, tools, advisors,
                sessionId, onThinking, onContent);
        if (!noticeText.isEmpty()) {
            chatText.setThinking(noticeText + StrUtil.nullToEmpty(chatText.getThinking()));
        }
        return chatText;
    }

    /**
     * 长期记忆注入
     *
     * 查询失败时降级为原系统指令，保证对话可用
     */
    private String injectMemory(Ai1AgentDO agent, Long sessionId, String systemPrompt) {
        if (!Boolean.TRUE.equals(agent.getMemoryEnabled())) {
            return systemPrompt;
        }
        try {
            Ai1SessionDO session = sessionService.getSession(sessionId);
            if (session == null) {
                return systemPrompt;
            }
            return memoryTool.mergePrompt(systemPrompt, memoryTool.loadMemory(agent.getId(), session.getUserId()));
        } catch (Exception e) {
            log.warn("[injectMemory][Agent({}) 会话({}) 长期记忆注入失败，已降级跳过]", agent.getId(), sessionId, e);
            return systemPrompt;
        }
    }

    /**
     * 回填助手消息（占位记录）的内容、思考过程与生成状态，并刷新会话活跃时间
     */
    private void saveAssistantMessage(Long messageId, Long sessionId, Ai1LlmChatTool.ChatText chatText, Integer status) {
        sessionMessageService.updateSessionMessage(new Ai1SessionMessageDO().setId(messageId)
                .setContent(StrUtil.nullToEmpty(chatText.getContent()))
                .setReasoning(StrUtil.emptyToNull(chatText.getThinking()))
                .setStatus(status));
        sessionService.touchSession(sessionId);
    }

    // ==================== 结果流 ====================

    /**
     * 追加结果流条目并续期 TTL：生成中按半个 TTL 节流续期；终态续期一次完整 TTL 并清理节流状态
     */
    private void appendResult(Long messageId, String type, String data) {
        // 1. 追加结果流条目
        RStream<String, String> resultStream = getResultStream(messageId);
        resultStream.add(StreamAddArgs.entries(FIELD_TYPE, type, FIELD_DATA, StrUtil.nullToEmpty(data)));

        // 2. 终态：续期一次完整 TTL，保证续传时间窗从结束时刻起算，并清理节流状态
        Duration ttl = Duration.ofSeconds(ai1Properties.getSession().getStream().getTtlSeconds());
        if (Ai1SessionStreamEventEnum.isTerminal(type)) {
            resultStream.expire(ttl);
            resultExpireTimes.remove(messageId);
            return;
        }

        // 3. 生成中：距上次续期超过半个 TTL 才续期，避免每个增量都执行 EXPIRE
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
     *
     * @return 是否需要主动完成连接；断开、超时回调或发送失败时由 Servlet 容器完成清理
     */
    private boolean streamResult(SseEmitter emitter, Long messageId, String lastEventId, Long tenantId) {
        // 1. 客户端断开、完成、超时时置取消标志，终止转发循环
        AtomicBoolean cancelled = new AtomicBoolean(false);
        emitter.onCompletion(() -> cancelled.set(true));
        emitter.onTimeout(() -> cancelled.set(true));
        emitter.onError(e -> cancelled.set(true));

        // 2. 先下发 stream 事件，告知前端结果流标识（助手消息编号），用于断线续传
        StreamMessageId fromId = parseStreamMessageId(lastEventId);
        long startTime = System.currentTimeMillis();
        long timeoutMs = ai1Properties.getSession().getStream().getTimeoutMs();
        long deadline = startTime + timeoutMs;
        long lostCheckTime = startTime + Math.max(timeoutMs - LOST_CHECK_ADVANCE_MILLIS, 0);
        boolean lostChecked = false;
        int errorCount = 0;
        sendEvent(emitter, Ai1SessionStreamEventEnum.STREAM.getEvent(), String.valueOf(messageId), null, cancelled);

        // 3. 循环读取结果流并转发，直到终态、超时或取消
        while (!cancelled.get()) {
            // 3.1 超过单连接最长时长，结束转发
            if (System.currentTimeMillis() > deadline) {
                log.warn("[streamResult][助手消息({}) 会话流转发超时]", messageId);
                return !cancelled.get();
            }
            // 3.2 从上次位置之后阻塞读取；读取失败时容错重试，连续失败达到阈值才中断，客户端可再续传
            Map<StreamMessageId, Map<String, String>> records;
            try {
                records = getResultStream(messageId).read(StreamReadArgs.greaterThan(fromId)
                        .count(READ_BATCH_SIZE).timeout(Duration.ofMillis(READ_BLOCK_MILLIS)));
            } catch (Exception e) {
                log.warn("[streamResult][助手消息({}) 会话流读取异常]", messageId, e);
                if (++errorCount >= READ_ERROR_MAX_COUNT) {
                    return !cancelled.get();
                }
                ThreadUtil.sleep(READ_ERROR_BACKOFF_MILLIS);
                continue;
            }
            errorCount = 0;
            // 3.3 阻塞窗口内无新条目时，下发空闲心跳，防止网关、浏览器因长时间无数据断开
            if (MapUtil.isEmpty(records)) {
                // 临近连接超时，结果流仍不存在：任务丢失（投递失败、worker 回填失败、结果流已过期等），
                // 回写失败并下发 error 终态，避免消息永远停在生成中、前端反复续传
                if (!lostChecked && System.currentTimeMillis() >= lostCheckTime) {
                    lostChecked = true;
                    if (!getResultStream(messageId).isExists()) {
                        log.warn("[streamResult][助手消息({}) 结果流不存在，判定生成任务丢失]", messageId);
                        failLostMessage(messageId, tenantId);
                        sendEvent(emitter, Ai1SessionStreamEventEnum.ERROR.getEvent(), LOST_MESSAGE, null, cancelled);
                        return !cancelled.get();
                    }
                }
                sendEvent(emitter, Ai1SessionStreamEventEnum.PING.getEvent(), "", null, cancelled);
                continue;
            }
            // 3.4 按条目编号顺序下发（Redisson 返回的 Map 有序），事件编号为条目编号；遇到终态结束
            for (Map.Entry<StreamMessageId, Map<String, String>> record : records.entrySet()) {
                fromId = record.getKey();
                String type = record.getValue().get(FIELD_TYPE);
                sendEvent(emitter, type, record.getValue().get(FIELD_DATA), fromId.toString(), cancelled);
                if (Ai1SessionStreamEventEnum.isTerminal(type)) {
                    return !cancelled.get();
                }
            }
        }
        return false;
    }

    /**
     * 任务丢失时，把仍处于生成中的助手消息回写为失败；已完成或已失败的消息不改动
     */
    void failLostMessage(Long messageId, Long tenantId) {
        try {
            TenantUtils.execute(tenantId, () -> {
                Ai1SessionMessageDO message = sessionMessageService.getSessionMessage(messageId);
                if (message == null || !Ai1SessionMessageStatusEnum.isGenerating(message.getStatus())) {
                    return;
                }
                sessionMessageService.updateSessionMessage(new Ai1SessionMessageDO().setId(messageId)
                        .setStatus(Ai1SessionMessageStatusEnum.FAILED.getStatus()));
            });
        } catch (Exception e) {
            log.error("[failLostMessage][助手消息({}) 回写失败状态失败]", messageId, e);
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
            emitter.send(SseEmitter.event().name(Ai1SessionStreamEventEnum.ERROR.getEvent())
                    .data(JsonUtils.toJsonString(StrUtil.blankToDefault(message, "生成失败"))));
        } catch (Exception ignored) {
            // 客户端已断开，由 Servlet 容器清理连接
            return;
        }
        emitter.complete();
    }

    // ==================== 通用方法 ====================

    private RStream<String, String> getTaskStream() {
        return redissonClient.getStream(Ai1RedisKeyConstants.SESSION_TASK_STREAM, StringCodec.INSTANCE);
    }

    private RStream<String, String> getResultStream(Long messageId) {
        return redissonClient.getStream(String.format(Ai1RedisKeyConstants.SESSION_RESULT_STREAM, messageId), StringCodec.INSTANCE);
    }

    private RBucket<String> getStopBucket(Long messageId) {
        return redissonClient.getBucket(String.format(Ai1RedisKeyConstants.SESSION_STOP, messageId), StringCodec.INSTANCE);
    }

    private RTopic getStopTopic() {
        return redissonClient.getTopic(Ai1RedisKeyConstants.SESSION_STOP_TOPIC, StringCodec.INSTANCE);
    }

    /**
     * 正文末尾追加提示
     */
    static Ai1LlmChatTool.ChatText withNotice(Ai1LlmChatTool.ChatText chatText, String notice) {
        return new Ai1LlmChatTool.ChatText(StrUtil.nullToEmpty(chatText.getContent()) + NOTICE_PREFIX + notice, chatText.getThinking());
    }

    /**
     * 解析正文末尾已回填的提示；没有时返回空
     */
    static String getSavedNotice(String content) {
        for (String notice : java.util.Arrays.asList(TERMINATED_NOTICE, FAILED_NOTICE, INTERRUPTED_MESSAGE)) {
            if (StrUtil.endWith(content, NOTICE_PREFIX + notice)) {
                return notice;
            }
        }
        return null;
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

    /**
     * 生成的运行上下文
     *
     * 停止与结束互斥，结束后迟到的停止不再中断线程
     */
    static class RunContext {

        /**
         * 生成线程，即 worker 线程
         */
        private final Thread thread;
        /**
         * Shell 进程跟踪器
         */
        @Getter
        private final Ai1SkillShellTracker shellTracker;
        /**
         * 是否已结束
         */
        private boolean finished;
        /**
         * 停止提示；为空表示未停止
         */
        private String stopNotice;

        /**
         * 未结束的 Shell 进程回收线程，与运行上下文的生命周期无关，由节点停止时统一等待
         */
        private final Set<Thread> reapers;

        RunContext(Thread thread, Ai1SkillShellTracker shellTracker, Set<Thread> reapers) {
            this.thread = thread;
            this.shellTracker = shellTracker;
            this.reapers = reapers;
        }

        /**
         * 停止生成：中断生成线程，并由独立线程回收在册的后台进程；已结束、已停止时忽略
         *
         * 单个进程回收最多等待 5s：不在调用方执行，避免阻塞停止检查线程或其他上下文的停止；
         * 不依赖 worker 响应中断，模型或工具库吞掉中断时进程也会被回收；
         * 回收可能晚于生成结束，结束时 worker 拿不到已被回收线程认领的进程，因此回收线程登记到 reapers，不随上下文移除
         *
         * @param notice 停止提示
         */
        synchronized void cancel(String notice) {
            if (finished || stopNotice != null) {
                return;
            }
            stopNotice = notice;
            thread.interrupt();
            Thread reaper = ThreadUtil.newThread(() -> {
                try {
                    shellTracker.cancel();
                } finally {
                    reapers.remove(Thread.currentThread());
                }
            }, REAPER_THREAD_NAME_PREFIX + thread.getName(), true);
            // 先登记再启动，结束时必能移除自身
            reapers.add(reaper);
            try {
                reaper.start();
            } catch (Throwable e) {
                reapers.remove(reaper);
                throw e;
            }
        }

        /**
         * 校验生成仍在进行，已停止或已结束时中止后续步骤
         */
        synchronized void checkActive() {
            if (finished || stopNotice != null) {
                throw new CancellationException(StrUtil.blankToDefault(stopNotice, "生成已结束"));
            }
        }

        /**
         * 结束生成，并清除 worker 线程的中断标志；需在 worker 线程调用
         *
         * @return 停止提示；未停止时为空
         */
        synchronized String finish() {
            finished = true;
            Thread.interrupted();
            return stopNotice;
        }

    }

}
