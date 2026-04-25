package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.service.RunPodComfyService;
import cn.iocoder.yudao.module.deepay.service.RunPodComfyService.JobStatusResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 图像生成代理接口（RunPod Serverless ComfyUI）。
 *
 * <pre>
 * POST /api/image/generate          — 提交 ComfyUI 工作流，立即返回 { jobId }
 * GET  /api/image/status/{jobId}    — 查询生成状态，COMPLETED 时返回 { status, images }
 * </pre>
 *
 * <p>安全说明：RunPod API Key 保存在服务端环境变量 RUNPOD_API_KEY，
 * 不会暴露给浏览器。前端只与此接口交互。</p>
 *
 * <p>前端调用示例（Vue + axios）：
 * <pre>
 * // 1. 提交
 * const { data } = await axios.post('/api/image/generate', {
 *   workflow: workflowJson,       // ComfyUI workflow_api.json 内容
 *   promptNode: '6',              // 可选：提示词节点 ID
 *   promptText: '深海蓝丝绒礼服'  // 可选：注入的提示词
 * })
 * const jobId = data.data.jobId
 *
 * // 2. 轮询
 * while (true) {
 *   await sleep(2000)
 *   const { data: s } = await axios.get(`/api/image/status/${jobId}`)
 *   if (s.data.status === 'COMPLETED') { showImages(s.data.images); break }
 *   if (s.data.status === 'FAILED')    { showError(s.data.error);   break }
 * }
 * </pre>
 * </p>
 */
@Tag(name = "Deepay - AI 图像生成（RunPod 代理）")
@RestController
@RequestMapping("/api/image")
@Validated
@Slf4j
public class ImageGenController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private RunPodComfyService runPodComfyService;

    // ── 请求 VO ─────────────────────────────────────────────────────

    public static class GenerateReqVO {
        /** ComfyUI workflow_api.json 完整内容（字符串 JSON 或对象均可） */
        @NotNull(message = "workflow 不能为空")
        public Object workflow;

        /**
         * 可选：要注入 prompt 的节点 ID（如 "6"）。
         * 留空则不修改工作流，直接使用 workflow 中的原始 prompt。
         */
        public String promptNode;

        /** 可选：注入到 promptNode 的提示词文本 */
        public String promptText;
    }

    // ── 接口 ────────────────────────────────────────────────────────

    /**
     * 提交 ComfyUI 工作流，立即返回 jobId（异步）。
     *
     * @return { "jobId": "xxx", "status": "IN_QUEUE" }
     */
    @PostMapping("/generate")
    @Operation(summary = "提交 ComfyUI 工作流到 RunPod Serverless")
    public CommonResult<Map<String, Object>> generate(@Valid @RequestBody GenerateReqVO req) {
        if (!runPodComfyService.isConfigured()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "RunPod 未配置，请联系管理员设置 RUNPOD_API_KEY 和 RUNPOD_ENDPOINT_ID");
            err.put("code",  503);
            return success(err);
        }

        // 将 workflow 反序列化为 Map（无论前端传的是 object 还是 string JSON）
        Map<String, Object> workflowMap = toWorkflowMap(req.workflow);

        // 可选：注入提示词
        if (isNotBlank(req.promptNode) && isNotBlank(req.promptText)) {
            injectPrompt(workflowMap, req.promptNode, req.promptText);
            log.info("[ImageGen] 注入 prompt 到节点 {} → {}", req.promptNode, req.promptText);
        }

        String jobId = runPodComfyService.submitJob(workflowMap);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("jobId",  jobId);
        resp.put("status", "IN_QUEUE");
        resp.put("message", "任务已提交，请通过 /api/image/status/{jobId} 轮询结果");
        log.info("[ImageGen] 任务已提交 jobId={}", jobId);
        return success(resp);
    }

    /**
     * 查询 RunPod 任务状态。
     *
     * <p>状态值：
     * <ul>
     *   <li>IN_QUEUE     — 排队中</li>
     *   <li>IN_PROGRESS  — 生成中（ComfyUI 正在运行工作流）</li>
     *   <li>COMPLETED    — 完成，images 字段包含图片 URL/base64 列表</li>
     *   <li>FAILED       — 失败，error 字段说明原因</li>
     * </ul>
     * </p>
     *
     * @return { "status": "COMPLETED", "images": ["data:image/png;base64,..."] }
     */
    @GetMapping("/status/{jobId}")
    @Operation(summary = "查询 RunPod 任务状态")
    public CommonResult<Map<String, Object>> status(@PathVariable @NotBlank String jobId) {
        JobStatusResult result = runPodComfyService.queryStatus(jobId);
        return success(result.toMap());
    }

    // ── 工具 ────────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> toWorkflowMap(Object workflow) {
        if (workflow instanceof Map) {
            return (Map<String, Object>) workflow;
        }
        if (workflow instanceof String) {
            try {
                return MAPPER.readValue((String) workflow, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                throw new IllegalArgumentException("workflow 字段不是合法的 JSON: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("workflow 字段类型不支持: " + workflow.getClass().getSimpleName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void injectPrompt(Map<String, Object> workflow, String nodeId, String text) {
        Object node = workflow.get(nodeId);
        if (!(node instanceof Map)) {
            throw new IllegalArgumentException("工作流中不存在节点 ID: " + nodeId);
        }
        Map<String, Object> nodeMap = (Map<String, Object>) node;
        Map<String, Object> inputs  = (Map<String, Object>) nodeMap.computeIfAbsent("inputs", k -> new LinkedHashMap<>());
        inputs.put("text", text);
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
