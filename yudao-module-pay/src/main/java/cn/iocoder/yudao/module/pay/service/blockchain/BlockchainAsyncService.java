package cn.iocoder.yudao.module.pay.service.blockchain;

import cn.iocoder.yudao.module.pay.dal.dataobject.blockchain.BlockchainTaskDO;
import cn.iocoder.yudao.module.pay.dal.mysql.blockchain.BlockchainTaskMapper;
import cn.iocoder.yudao.module.pay.service.blockchain.dto.OrderProofDTO;
import cn.iocoder.yudao.module.pay.service.blockchain.event.BlockchainTaskFailedEvent;
import cn.iocoder.yudao.module.pay.service.blockchain.event.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.module.pay.framework.job.config.PayJobConfiguration.BLOCKCHAIN_THREAD_POOL_TASK_EXECUTOR;

/**
 * 区块链异步存证服务
 * <p>
 * 核心设计：
 * 1. 支付回调事务提交后通过 {@link TransactionalEventListener} 触发，不阻塞主流程
 * 2. Redisson 分布式锁防止集群环境下重复提交同一订单
 * 3. 手动重试（最多 {@link #maxRetryAttempts} 次，指数退避），避免引入 spring-retry 依赖
 * 4. JD Chain 可选：有实现 Bean 时走链上存证；否则降级为本地哈希 + 批量公链上传
 *
 * @author deepay
 */
@Slf4j
@Service
public class BlockchainAsyncService implements ApplicationEventPublisherAware {

    // ========== 依赖注入 ==========

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private BlockchainTaskMapper blockchainTaskMapper;

    /**
     * 可选：有 JD Chain SDK 集成时注入；否则自动降级为本地哈希方案
     * 使用 @Autowired(required=false) 因为 @Resource 不支持可选注入
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private JdChainService jdChainService;

    private ApplicationEventPublisher eventPublisher;

    // ========== 配置项 ==========

    @Value("${blockchain.async.enabled:true}")
    private boolean asyncEnabled;

    @Value("${blockchain.retry.max-attempts:3}")
    private int maxRetryAttempts;

    /** 首次重试等待时间（毫秒） */
    @Value("${blockchain.retry.delay:2000}")
    private long retryDelay;

    @Value("${blockchain.batch.size:10}")
    private int batchSize;

    // ========== 内存批量队列 ==========

    private final Queue<BlockchainTaskDO> memoryQueue = new LinkedList<>();
    private final Object queueLock = new Object();
    private final AtomicInteger queueSize = new AtomicInteger(0);

    private static final int MAX_QUEUE_SIZE = 1000;

    // ========== 公开接口 ==========

    /**
     * 支付成功事件监听（事务提交后触发）
     * 此方法快速返回，将真正的存证工作转交给异步线程
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        if (!asyncEnabled) {
            log.debug("区块链异步存证已禁用，跳过");
            return;
        }
        OrderProofDTO proofDTO = buildProofDTO(event);
        asyncStoreOrderProof(proofDTO).exceptionally(ex -> {
            log.error("异步存证触发异常，orderId={}: {}", event.getOrderId(), ex.getMessage(), ex);
            return "ASYNC_ERROR";
        });
        log.debug("支付成功事件已触发异步存证，orderId={}", event.getOrderId());
    }

    /**
     * 异步存证主方法（带手动重试）
     * 使用独立线程池，不影响业务响应时间
     */
    @Async(BLOCKCHAIN_THREAD_POOL_TASK_EXECUTOR)
    public CompletableFuture<String> asyncStoreOrderProof(OrderProofDTO proofDTO) {
        String orderId = proofDTO.getOrderId();
        String taskId = buildTaskId(orderId);
        String lockKey = "pay_blockchain:lock:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 分布式锁防重，最多等待 3 秒竞争锁
            if (!lock.tryLock(3, 5 * 60, TimeUnit.SECONDS)) {
                log.warn("订单 {} 存证锁竞争失败，已有其他节点处理，跳过", orderId);
                return CompletableFuture.completedFuture("LOCKED");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture("INTERRUPTED");
        }

        try {
            return CompletableFuture.completedFuture(doStoreWithRetry(taskId, orderId, proofDTO));
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 批量上传待上链任务到公链（由定时 Job 调用，例如 XXL-Job handler）
     */
    public void batchUploadToPublicChain() {
        if (memoryQueue.isEmpty()) {
            return;
        }

        List<BlockchainTaskDO> batchTasks = new ArrayList<>();
        synchronized (queueLock) {
            int count = Math.min(batchSize, memoryQueue.size());
            for (int i = 0; i < count; i++) {
                batchTasks.add(memoryQueue.poll());
            }
            queueSize.addAndGet(-count);
        }

        if (batchTasks.isEmpty()) {
            return;
        }

        try {
            String batchData = buildBatchJson(batchTasks);
            String batchHash = sha256(batchData);
            String publicTxHash = simulatePublicChainUpload(batchData, batchHash);

            LocalDateTime now = LocalDateTime.now();
            batchTasks.forEach(task -> {
                BlockchainTaskDO patch = new BlockchainTaskDO();
                patch.setId(task.getId());
                patch.setPublicChainTxHash(publicTxHash);
                patch.setBatchUploaded(true);
                patch.setBatchUploadTime(now);
                blockchainTaskMapper.updateById(patch);
            });
            log.info("批量上链成功，共 {} 条，txHash={}", batchTasks.size(), publicTxHash);

        } catch (Exception e) {
            log.error("批量上链失败: {}", e.getMessage(), e);
            // 重新放回队列，下次继续
            synchronized (queueLock) {
                batchTasks.forEach(t -> {
                    if (queueSize.get() < MAX_QUEUE_SIZE) {
                        memoryQueue.offer(t);
                        queueSize.incrementAndGet();
                    }
                });
            }
        }
    }

    // ========== 私有实现 ==========

    /**
     * 带指数退避的重试循环
     */
    private String doStoreWithRetry(String taskId, String orderId, OrderProofDTO proofDTO) {
        Exception lastException = null;
        long delay = retryDelay;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                return doStore(taskId, orderId, proofDTO);
            } catch (Exception e) {
                lastException = e;
                log.warn("订单 {} 存证第 {}/{} 次失败: {}", orderId, attempt, maxRetryAttempts, e.getMessage());
                if (attempt < maxRetryAttempts) {
                    sleepQuietly(delay);
                    delay = (long) (delay * 1.5);
                }
            }
        }

        // 所有重试耗尽 → 转入死信
        return handleDeadLetter(orderId, proofDTO, lastException);
    }

    /**
     * 单次存证核心逻辑
     */
    private String doStore(String taskId, String orderId, OrderProofDTO proofDTO) {
        // 1. 持久化任务记录（PROCESSING）
        BlockchainTaskDO task = buildTask(taskId, orderId, proofDTO);
        blockchainTaskMapper.insert(task);

        // 2. 计算防篡改哈希
        String dataHash = sha256(buildHashInput(proofDTO));
        task.setDataHash(dataHash);

        // 3. 选择存证策略
        String txHash;
        if (jdChainService != null && isJdChainHealthy()) {
            txHash = storeToJdChain(proofDTO, dataHash);
            task.setChainType("JD_CHAIN");
        } else {
            txHash = buildLocalTxHash(dataHash);
            task.setChainType("LOCAL_HASH");
            enqueueForBatch(task);
        }

        // 4. 更新为 SUCCESS
        BlockchainTaskDO success = new BlockchainTaskDO();
        success.setId(task.getId());
        success.setDataHash(dataHash);
        success.setTxHash(txHash);
        success.setChainType(task.getChainType());
        success.setStatus("SUCCESS");
        success.setCompletedAt(LocalDateTime.now());
        blockchainTaskMapper.updateById(success);

        log.info("订单 {} 存证成功，txHash={}", orderId, txHash);
        return txHash;
    }

    /**
     * 死信处理：持久化记录并告警
     */
    private String handleDeadLetter(String orderId, OrderProofDTO proofDTO, Exception lastException) {
        log.error("订单 {} 存证重试全部失败，转入死信", orderId, lastException);

        BlockchainTaskDO dead = new BlockchainTaskDO();
        dead.setTaskId("DEAD_" + buildTaskId(orderId));
        dead.setOrderId(orderId);
        dead.setUserId(proofDTO.getUserId());
        dead.setAmount(proofDTO.getAmount());
        dead.setCurrency(proofDTO.getCurrency());
        dead.setStatus("DEAD_LETTER");
        dead.setErrorMsg(lastException != null ? lastException.getMessage() : "unknown");
        dead.setCreatedAt(LocalDateTime.now());
        dead.setCompletedAt(LocalDateTime.now());
        dead.setRetryCount(maxRetryAttempts);
        blockchainTaskMapper.insert(dead);

        // 发布失败事件供监控系统捕获
        eventPublisher.publishEvent(new BlockchainTaskFailedEvent(orderId, lastException));

        sendAlert("区块链存证死信告警",
                String.format("订单 %s 存证全部重试失败，已转入死信队列。错误: %s",
                        orderId, lastException != null ? lastException.getMessage() : "unknown"));

        return "DEAD_LETTER";
    }

    // ========== 辅助方法 ==========

    private String storeToJdChain(OrderProofDTO proofDTO, String dataHash) {
        Map<String, Object> chainData = new LinkedHashMap<>();
        chainData.put("orderId", proofDTO.getOrderId());
        chainData.put("amount", proofDTO.getAmount());
        chainData.put("currency", proofDTO.getCurrency());
        chainData.put("paymentTime", proofDTO.getPaymentTime());
        chainData.put("userId", proofDTO.getUserId());
        chainData.put("productInfo", proofDTO.getProductInfo());
        chainData.put("dataHash", dataHash);
        chainData.put("timestamp", System.currentTimeMillis());
        return jdChainService.storeData("ORDER_" + proofDTO.getOrderId(), "payment_proof", chainData);
    }

    private String buildLocalTxHash(String dataHash) {
        return "LOCAL_HASH_" + dataHash.substring(0, 16) + "_" + System.currentTimeMillis();
    }

    private boolean isJdChainHealthy() {
        try {
            return jdChainService != null && jdChainService.isHealthy();
        } catch (Exception e) {
            log.warn("JD Chain 健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    private void enqueueForBatch(BlockchainTaskDO task) {
        synchronized (queueLock) {
            if (queueSize.get() < MAX_QUEUE_SIZE) {
                memoryQueue.offer(task);
                queueSize.incrementAndGet();
            } else {
                log.warn("批量队列已满（{}），丢弃任务 taskId={}", MAX_QUEUE_SIZE, task.getTaskId());
            }
        }
    }

    private BlockchainTaskDO buildTask(String taskId, String orderId, OrderProofDTO proofDTO) {
        BlockchainTaskDO task = new BlockchainTaskDO();
        task.setTaskId(taskId);
        task.setOrderId(orderId);
        task.setUserId(proofDTO.getUserId());
        task.setAmount(proofDTO.getAmount());
        task.setCurrency(proofDTO.getCurrency());
        task.setStatus("PROCESSING");
        task.setRetryCount(0);
        task.setBatchUploaded(false);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private OrderProofDTO buildProofDTO(PaymentSuccessEvent event) {
        OrderProofDTO dto = new OrderProofDTO();
        dto.setOrderId(event.getOrderId());
        dto.setAmount(event.getAmount());
        dto.setCurrency(event.getCurrency());
        dto.setPaymentTime(event.getPaymentTime());
        dto.setUserId(event.getUserId());
        dto.setProductInfo(event.getProductInfo());
        dto.setTransactionId(event.getTransactionId());
        return dto;
    }

    private static String buildTaskId(String orderId) {
        return "TASK_" + orderId + "_" + System.currentTimeMillis();
    }

    private static String buildHashInput(OrderProofDTO dto) {
        return String.join("|",
                nullSafe(dto.getOrderId()),
                nullSafe(dto.getAmount() != null ? dto.getAmount().toPlainString() : null),
                nullSafe(dto.getPaymentTime()),
                nullSafe(dto.getUserId()),
                nullSafe(dto.getProductInfo()));
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }

    private static String buildBatchJson(List<BlockchainTaskDO> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"batchId\":\"").append(UUID.randomUUID())
          .append("\",\"timestamp\":").append(System.currentTimeMillis())
          .append(",\"count\":").append(tasks.size())
          .append(",\"items\":[");
        for (int i = 0; i < tasks.size(); i++) {
            BlockchainTaskDO t = tasks.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"orderId\":\"").append(t.getOrderId())
              .append("\",\"hash\":\"").append(t.getDataHash()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 公链上传占位方法。
     * 实际使用时替换为以太坊/BSC等链上合约调用；
     * 此处返回批次哈希的前 64 位作为模拟交易哈希。
     */
    private static String simulatePublicChainUpload(String batchData, String batchHash) {
        return "0x" + batchHash.substring(0, Math.min(64, batchHash.length()));
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendAlert(String title, String message) {
        // 扩展点：可集成邮件、钉钉、企业微信等告警渠道
        log.error("[告警] {}: {}", title, message);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

}
