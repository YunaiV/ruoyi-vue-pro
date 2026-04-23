package cn.iocoder.yudao.module.deepay.scheduler;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayRetryTaskDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayRetryTaskMapper;
import cn.iocoder.yudao.module.deepay.orchestrator.ProductionOrchestrator;
import cn.iocoder.yudao.module.deepay.service.DeepayAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 失败重试调度器 — 扫描 deepay_retry_task，对失败任务执行指数退避重试。
 *
 * <p>每 5 分钟扫描一次，取所有 PENDING / RETRYING 且已到重试时间的任务，
 * 按任务类型分发重试逻辑。超过 maxRetry 次后标记为 FAILED，不再自动重试。</p>
 */
@Component
public class DeepayRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeepayRetryScheduler.class);

    @Resource private DeepayRetryTaskMapper   retryTaskMapper;
    @Resource private ProductionOrchestrator  productionOrchestrator;
    @Resource private DeepayAuditService      auditService;

    /** 每 5 分钟执行一次。 */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void retryPendingTasks() {
        List<DeepayRetryTaskDO> tasks = retryTaskMapper.selectPendingTasks();
        if (tasks.isEmpty()) {
            return;
        }
        log.info("[RetryScheduler] 本轮待重试任务数={}", tasks.size());

        for (DeepayRetryTaskDO task : tasks) {
            try {
                executeRetry(task);
            } catch (Exception e) {
                log.error("[RetryScheduler] 任务重试异常 id={} type={}", task.getId(), task.getTaskType(), e);
            }
        }
    }

    // ----------------------------------------------------------------

    private void executeRetry(DeepayRetryTaskDO task) {
        log.info("[RetryScheduler] 开始重试 id={} type={} chainCode={} retryCount={}",
                task.getId(), task.getTaskType(), task.getChainCode(), task.getRetryCount());

        try {
            switch (task.getTaskType()) {
                case "AI_DESIGN":
                case "PATTERN":
                case "PUBLISH": {
                    // 重新触发完整生产流水线
                    Context ctx = new Context();
                    ctx.chainCode = task.getChainCode();
                    ctx.keyword   = task.getChainCode(); // 最小降级：用 chainCode 当 keyword
                    productionOrchestrator.run(ctx);
                    break;
                }
                case "PAYMENT":
                    // 支付重试由外部 Webhook 触发，此处仅记录不重跑
                    log.info("[RetryScheduler] PAYMENT 类型任务仅记录，需外部重触发 chainCode={}",
                            task.getChainCode());
                    break;
                default:
                    log.warn("[RetryScheduler] 未知任务类型 type={}", task.getTaskType());
            }

            // 重试成功
            retryTaskMapper.markDone(task.getId());
            auditService.log(task.getChainCode(), "RETRY",
                    "status=" + task.getStatus() + " retryCount=" + task.getRetryCount(),
                    "status=DONE");
            log.info("[RetryScheduler] 重试成功 id={}", task.getId());

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 2000))
                    : "Unknown error";
            retryTaskMapper.markRetryFailed(
                    task.getId(), errorMsg,
                    task.getRetryCount() != null ? task.getRetryCount() : 0,
                    task.getMaxRetry() != null ? task.getMaxRetry() : 3);
            log.warn("[RetryScheduler] 重试失败 id={} error={}", task.getId(), errorMsg);
        }
    }

    /**
     * 便捷方法：登记一条失败任务（由其他 Component 调用）。
     */
    public void register(String chainCode, String taskType, String errorMsg) {
        DeepayRetryTaskDO task = new DeepayRetryTaskDO();
        task.setChainCode(chainCode);
        task.setTaskType(taskType);
        task.setStatus("PENDING");
        task.setErrorMsg(errorMsg != null
                ? errorMsg.substring(0, Math.min(errorMsg.length(), 2000)) : null);
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setNextRetryAt(LocalDateTime.now().plusMinutes(2));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        retryTaskMapper.insert(task);
        log.info("[RetryScheduler] 登记失败任务 chainCode={} type={}", chainCode, taskType);
    }

}
