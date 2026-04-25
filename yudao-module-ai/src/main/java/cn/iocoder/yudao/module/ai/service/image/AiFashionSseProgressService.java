package cn.iocoder.yudao.module.ai.service.image;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AI 服装设计智能体 SSE 进度推送服务
 *
 * <p>实现纯自动化交互的核心组件之一：后端任务执行过程中，
 * 通过 Server-Sent Events 实时推送进度给前端，
 * 无需用户轮询，无需任何手动操作。</p>
 *
 * <p>事件类型：</p>
 * <ul>
 *   <li>{@code progress} – 步骤进度更新，data 为 {@link ProgressEvent}</li>
 *   <li>{@code step_done} – 单步完成，data 为步骤结果 JSON</li>
 *   <li>{@code chain_done} – 整条链路完成，data 为最终输出</li>
 *   <li>{@code error}     – 步骤失败，data 为错误描述</li>
 * </ul>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionSseProgressService {

    /** chainId → 订阅的 SSE Emitter 列表 */
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────
    // 订阅 / 取消
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 前端订阅某条链路的进度推送。
     *
     * @param chainId 链路编号
     * @return SSE Emitter（返回给前端保持长连接）
     */
    public SseEmitter subscribe(String chainId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 分钟超时
        emitters.computeIfAbsent(chainId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(chainId, emitter));
        emitter.onTimeout(() -> {
            remove(chainId, emitter);
            emitter.complete();
        });
        emitter.onError(e -> remove(chainId, emitter));

        log.debug("[SSE] 订阅链路进度, chainId={}", chainId);

        // 立即推送一条连接确认事件
        push(chainId, "connected", Map.of("chainId", chainId, "msg", "已连接，等待任务进度..."));
        return emitter;
    }

    private void remove(String chainId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(chainId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 推送方法
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 推送步骤进度事件（progress）。
     *
     * @param chainId     链路编号
     * @param stepOrder   当前步骤序号（0-based）
     * @param totalSteps  链路总步骤数
     * @param stepDesc    步骤描述，如"正在生成 5 款甜酷风红色连衣裙 (2/5)…"
     * @param pct         0-100
     */
    public void pushProgress(String chainId, int stepOrder, int totalSteps,
                             String stepDesc, int pct) {
        push(chainId, "progress", new ProgressEvent(chainId, stepOrder, totalSteps, stepDesc, pct));
    }

    /**
     * 推送单步完成事件（step_done）。
     *
     * @param chainId   链路编号
     * @param stepOrder 完成的步骤序号
     * @param stepDesc  步骤描述
     * @param resultJson 结果 JSON 字符串
     */
    public void pushStepDone(String chainId, int stepOrder, String stepDesc, String resultJson) {
        push(chainId, "step_done",
                Map.of("chainId", chainId, "stepOrder", stepOrder,
                        "stepDesc", stepDesc, "result", resultJson != null ? resultJson : "{}"));
    }

    /**
     * 推送整条链路完成事件（chain_done）。
     *
     * @param chainId      链路编号
     * @param summaryMsg   总结描述，如"您的5款设计+3D模型已全部完成！"
     * @param finalJson    最终产物 JSON（包含所有图片URL等）
     */
    public void pushChainDone(String chainId, String summaryMsg, String finalJson) {
        push(chainId, "chain_done",
                Map.of("chainId", chainId, "summary", summaryMsg,
                        "final", finalJson != null ? finalJson : "{}"));
        // 完成后关闭所有订阅的 Emitter
        List<SseEmitter> list = emitters.remove(chainId);
        if (list != null) {
            list.forEach(e -> {
                try { e.complete(); } catch (Exception ignored) {}
            });
        }
    }

    /**
     * 推送错误事件（error）。
     *
     * @param chainId      链路编号
     * @param stepOrder    失败步骤序号
     * @param errorMessage 错误描述
     * @param degraded     是否已降级继续执行
     */
    public void pushError(String chainId, int stepOrder, String errorMessage, boolean degraded) {
        push(chainId, "error",
                Map.of("chainId", chainId, "stepOrder", stepOrder,
                        "error", errorMessage, "degraded", degraded));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 内部工具
    // ─────────────────────────────────────────────────────────────────────────

    private void push(String chainId, String eventName, Object data) {
        List<SseEmitter> list = emitters.get(chainId);
        if (list == null || list.isEmpty()) {
            return;
        }

        String json = toJson(data);
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(json);

        list.removeIf(emitter -> {
            try {
                emitter.send(event);
                return false;
            } catch (IOException e) {
                log.warn("[SSE] emitter 已断开, chainId={}", chainId);
                return true;
            } catch (Exception e) {
                log.warn("[SSE] 推送失败, chainId={}, error={}", chainId, e.getMessage());
                return true;
            }
        });
    }

    private String toJson(Object data) {
        try {
            return cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString(data);
        } catch (Exception e) {
            return data.toString();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Event VO
    // ─────────────────────────────────────────────────────────────────────────

    @Getter
    public static class ProgressEvent {
        private final String chainId;
        private final int stepOrder;
        private final int totalSteps;
        private final String stepDesc;
        private final int pct;
        private final long ts = System.currentTimeMillis();

        public ProgressEvent(String chainId, int stepOrder, int totalSteps,
                             String stepDesc, int pct) {
            this.chainId = chainId;
            this.stepOrder = stepOrder;
            this.totalSteps = totalSteps;
            this.stepDesc = stepDesc;
            this.pct = pct;
        }
    }

}
