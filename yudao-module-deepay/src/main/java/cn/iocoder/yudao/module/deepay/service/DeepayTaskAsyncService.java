package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTaskMapper;
import cn.iocoder.yudao.module.deepay.orchestrator.ProductionOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 异步出图任务服务（STEP 21）。
 *
 * <p>Controller 创建 task 记录后调用 {@link #runTask}，立即返回 taskId。
 * 本方法在独立线程中执行全链路 orchestrator，完成后更新 task 状态。</p>
 *
 * <p>状态流：pending → running → success | failed</p>
 */
@Slf4j
@Service
public class DeepayTaskAsyncService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource private DeepayTaskMapper          taskMapper;
    @Resource private ProductionOrchestrator    orchestrator;
    @Resource private DeepayDesignCacheService  cacheService;
    @Resource private OssService                ossService;
    @Resource private DeepayDesignImageMapper   designImageMapper;

    /**
     * 异步执行完整出图链路。
     *
     * <p>严格执行顺序（任一步失败均跳入 catch → failed）：
     * <ol>
     *   <li>标记 running</li>
     *   <li>缓存检查（STEP 22）：命中则直接返回，不走 AI</li>
     *   <li>运行 ProductionOrchestrator（AI 链路）</li>
     *   <li>OSS 持久化（STEP 24）：AI 临时 URL → 永久 URL，含 Redis MD5 去重</li>
     *   <li>写缓存（STEP 22）：空列表不写入，防空缓存</li>
     *   <li>更新 design_image view_count（STEP 28）</li>
     *   <li>标记 success</li>
     * </ol>
     * </p>
     *
     * @param taskId 任务 ID
     * @param ctx    每次请求创建的全新 Context（避免多线程污染）
     */
    @Async("taskExecutor")
    public void runTask(Long taskId, Context ctx) {
        DeepayTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("[TaskAsync] 任务不存在 taskId={}", taskId);
            return;
        }

        // 1. 标记为 running
        taskMapper.markRunning(taskId);
        log.info("[TaskAsync] 任务开始 taskId={} userId={}", taskId, task.getUserId());

        try {
            // 2. 检查缓存（STEP 22）
            String cacheKey = cacheService.buildKey(ctx.category, ctx.stylePreference, ctx.market, ctx.priceLevel);
            List<String> cached = cacheService.get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                log.info("[TaskAsync] 缓存命中，跳过 AI 生成 taskId={} size={}", taskId, cached.size());
                taskMapper.markSuccess(taskId, MAPPER.writeValueAsString(cached));
                return;
            }

            // 3. 运行全链路编排
            ctx = orchestrator.run(ctx);

            // 4. 取 safeImages（fallback → designImages）
            List<String> images = resolveImages(ctx);

            // 5. OSS 持久化（STEP 24）：将 AI 临时 URL 替换为永久存储 URL
            images = ossService.persistAll(images);

            // 6. 更新 design_image view_count（STEP 25 结构化日志 + STEP 28 计数）
            for (String url : images) {
                try {
                    designImageMapper.incrementViewCount(url);
                } catch (Exception ex) {
                    log.debug("[TaskAsync] view_count 更新跳过 url={}", url);
                }
            }

            // 7. 写缓存（STEP 22）
            cacheService.set(cacheKey, images);

            // 8. 标记成功
            String resultJson = MAPPER.writeValueAsString(images);
            taskMapper.markSuccess(taskId, resultJson);
            log.info("[TaskAsync] 任务完成 taskId={} images={}", taskId, images.size());

        } catch (Exception e) {
            log.error("[TaskAsync] 任务失败 taskId={}", taskId, e);
            taskMapper.markFailed(taskId, e.getMessage());
        }
    }

    private List<String> resolveImages(Context ctx) {
        if (ctx.safeImages != null && !ctx.safeImages.isEmpty()) {
            return ctx.safeImages;
        }
        if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
            return ctx.designImages;
        }
        return Collections.emptyList();
    }

}
