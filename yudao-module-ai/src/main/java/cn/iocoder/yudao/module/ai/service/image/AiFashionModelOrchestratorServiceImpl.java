package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskStepMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionRoutingStrategyEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepTypeEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionWorkflowModeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiImageOptions;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * AI 服装设计模型协调器实现
 *
 * <p>对应 Python 参考代码中的 {@code ModelOrchestrator.execute_workflow()} +
 * {@code FashionDesignOrchestrator._execute_design_workflow()}。</p>
 *
 * <p>核心流程：
 * <ol>
 *   <li>构建阶段列表（由 workflowMode + 条件参数共同决定）</li>
 *   <li>并发预加载 poseImage + fabricImage（CompletableFuture）</li>
 *   <li>顺序执行各阶段，每步路由 → 调用 SD WebUI → 存图 → 回写 DB</li>
 *   <li>UPSAMPLE / THREE_D 失败时降级，不中断整体任务</li>
 *   <li>每步完成后评估质量分并写入 outputOptions</li>
 * </ol>
 * </p>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionModelOrchestratorServiceImpl implements AiFashionModelOrchestratorService {

    /** 单步最大重试次数（对应 Python max_retries=3）*/
    private static final int STAGE_MAX_RETRY = 2;
    /** 单步超时（秒）*/
    private static final int STAGE_TIMEOUT_SECONDS = 180;

    @Resource
    private AiFashionModelRouterService modelRouter;
    @Resource
    private AiFashionModelRouterServiceImpl routerImpl; // 用于记录实测延迟
    @Resource
    private AiFashionTaskStepMapper stepMapper;
    @Resource
    private FileApi fileApi;

    @Value("${yudao.ai.stable-diffusion-web-ui.base-url:http://localhost:7860}")
    private String sdBaseUrl;

    @Value("${yudao.ai.stable-diffusion-web-ui.api-key:}")
    private String sdApiKey;

    /** 图片预加载线程池（最多 4 并发下载） */
    private final ExecutorService preloadExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "fashion-preload-" + r.hashCode());
        t.setDaemon(true);
        return t;
    });

    // ===== buildStageList =====

    @Override
    public List<AiFashionTaskStepDO> buildStageList(AiFashionTaskDO task) {
        AiFashionWorkflowModeEnum mode = AiFashionWorkflowModeEnum.of(task.getWorkflowMode());
        List<String> defaultStages = mode.getDefaultStages();

        List<AiFashionTaskStepDO> steps = new ArrayList<>();
        int order = 0;
        for (String stageType : defaultStages) {
            // 条件跳过：TEXTURE/FABRIC 需要 fabricRefUrl；POSE 需要 poseImageUrl
            if (("TEXTURE".equals(stageType) || "FABRIC".equals(stageType))
                    && StrUtil.isBlank(task.getFabricRefUrl())) {
                continue;
            }
            if ("POSE".equals(stageType) && StrUtil.isBlank(task.getPoseImageUrl())) {
                continue;
            }
            // THREE_D 需要 require3d=true
            if ("THREE_D".equals(stageType) && !Boolean.TRUE.equals(task.getRequire3d())) {
                continue;
            }
            steps.add(newStep(task.getId(), stageType, order++));
        }
        return steps;
    }

    // ===== executePipeline =====

    @Override
    public String executePipeline(AiFashionTaskDO task, List<AiFashionTaskStepDO> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("任务步骤列表为空，无法执行流水线");
        }

        AiFashionQualityConfig config = AiFashionQualityConfig.ofPreset(task.getQualityPreset());
        AiFashionRoutingStrategyEnum strategy = AiFashionRoutingStrategyEnum.of(task.getRoutingStrategy());
        StableDiffusionWebUiApi api = buildApi();

        // ── 阶段0：并发预加载参考图（在 CONCEPT/SKETCH 执行期间异步下载）──
        CompletableFuture<String> poseFuture  = preloadImageAsync(task.getPoseImageUrl());
        CompletableFuture<String> fabricFuture = preloadImageAsync(task.getFabricRefUrl());

        String currentBase64 = null;
        String currentPicUrl = null;

        for (AiFashionTaskStepDO step : steps) {
            long stepStart = System.currentTimeMillis();
            String stepType = step.getStepType().toUpperCase();

            // 获取路由决策
            ModelRouteDecision decision = modelRouter.route(stepType, config, strategy);

            log.info("[Orchestrator][taskId={} order={} type={}] 开始执行 → 端点={} steps={} reason={}",
                    task.getId(), step.getStepOrder(), stepType,
                    decision.getSdApiEndpoint(), decision.getSteps(), decision.getRoutingReason());

            // 写入步骤输入信息
            stepMapper.updateById(new AiFashionTaskStepDO()
                    .setId(step.getId())
                    .setModelName(decision.getSampler())
                    .setInputPicUrl(currentPicUrl)
                    .setInputOptions(buildInputOptions(task, decision)));

            try {
                String resultBase64 = executeStageWithRetry(
                        task, step, api, config, decision,
                        currentBase64, poseFuture, fabricFuture,
                        STAGE_MAX_RETRY);

                // 持久化产物
                byte[] imageBytes = Base64.decode(resultBase64);
                currentPicUrl = fileApi.createFile(imageBytes);
                currentBase64 = resultBase64;

                long durationMs = System.currentTimeMillis() - stepStart;
                routerImpl.recordLatency(stepType, durationMs);

                // 质量评估
                Map<String, Object> quality = evaluateQuality(imageBytes);
                quality.put("durationMs", durationMs);

                stepMapper.updateById(new AiFashionTaskStepDO()
                        .setId(step.getId())
                        .setStatus(AiFashionTaskStepStatusEnum.SUCCESS.getStatus())
                        .setOutputPicUrl(currentPicUrl)
                        .setOutputOptions(quality)
                        .setDurationMs(durationMs));

                log.info("[Orchestrator][taskId={} type={}] 完成 耗时={}ms 产物={}",
                        task.getId(), stepType, durationMs, currentPicUrl);

            } catch (Exception ex) {
                long durationMs = System.currentTimeMillis() - stepStart;
                log.error("[Orchestrator][taskId={} type={}] 失败 耗时={}ms",
                        task.getId(), stepType, durationMs, ex);

                // UPSAMPLE / THREE_D 降级：不中断任务
                if ("UPSAMPLE".equals(stepType) || "UPSCALE".equals(stepType)
                        || "THREE_D".equals(stepType)) {
                    log.warn("[Orchestrator][taskId={}] {} 失败，降级使用上一步产物: {}",
                            task.getId(), stepType, currentPicUrl);
                    stepMapper.updateById(new AiFashionTaskStepDO()
                            .setId(step.getId())
                            .setStatus(AiFashionTaskStepStatusEnum.FAIL.getStatus())
                            .setDurationMs(durationMs)
                            .setErrorMessage("[WARN-DEGRADED] " + ex.getMessage()));
                    continue;
                }

                // 其他步骤失败 → 更新步骤状态后向上抛出（由 Service 层处理任务级失败）
                stepMapper.updateById(new AiFashionTaskStepDO()
                        .setId(step.getId())
                        .setStatus(AiFashionTaskStepStatusEnum.FAIL.getStatus())
                        .setDurationMs(durationMs)
                        .setErrorMessage(ex.getMessage()));
                throw new RuntimeException("[" + stepType + "] 步骤失败: " + ex.getMessage(), ex);
            }
        }

        return currentPicUrl;
    }

    // ===== evaluateQuality =====

    @Override
    public Map<String, Object> evaluateQuality(byte[] imageBytes) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        if (imageBytes == null || imageBytes.length == 0) {
            return metrics;
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) {
                return metrics;
            }
            int w = img.getWidth();
            int h = img.getHeight();
            metrics.put("width", w);
            metrics.put("height", h);
            metrics.put("resolution", w * h);

            // 灰度统计：对比度（标准差）+ 清晰度（相邻差方差，近似 Laplacian）
            double[] gray = new double[w * h];
            int idx = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = img.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >>  8) & 0xFF;
                    int b =  rgb        & 0xFF;
                    gray[idx++] = 0.299 * r + 0.587 * g + 0.114 * b;
                }
            }
            double mean = Arrays.stream(gray).average().orElse(128);
            double variance = Arrays.stream(gray).map(v -> (v - mean) * (v - mean)).average().orElse(0);
            double stdDev = Math.sqrt(variance);
            metrics.put("contrast", Math.round(stdDev * 100.0) / 100.0);

            // 近似清晰度：采样行列差方差
            double lapSum = 0;
            int lapCount = 0;
            for (int y = 1; y < h - 1; y += 4) {
                for (int x = 1; x < w - 1; x += 4) {
                    double center = gray[y * w + x];
                    double laplacian = -4 * center
                            + gray[(y - 1) * w + x]
                            + gray[(y + 1) * w + x]
                            + gray[y * w + (x - 1)]
                            + gray[y * w + (x + 1)];
                    lapSum += laplacian * laplacian;
                    lapCount++;
                }
            }
            double sharpness = lapCount > 0 ? lapSum / lapCount : 0;
            metrics.put("sharpness", Math.round(sharpness * 100.0) / 100.0);

            // 总质量分（0~1）
            double qualityScore = Math.min(1.0,
                    (Math.min(stdDev, 80) / 80.0) * 0.4
                            + (Math.min(sharpness, 2000) / 2000.0) * 0.6);
            metrics.put("qualityScore", Math.round(qualityScore * 1000.0) / 1000.0);

        } catch (Exception e) {
            log.warn("[Orchestrator] 质量评估异常: {}", e.getMessage());
        }
        return metrics;
    }

    // ===== 私有辅助方法 =====

    /**
     * 带重试的阶段执行
     */
    private String executeStageWithRetry(AiFashionTaskDO task, AiFashionTaskStepDO step,
            StableDiffusionWebUiApi api, AiFashionQualityConfig config,
            ModelRouteDecision decision, String currentBase64,
            CompletableFuture<String> poseFuture, CompletableFuture<String> fabricFuture,
            int maxRetry) {

        Exception lastEx = null;
        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            if (attempt > 0) {
                log.warn("[Orchestrator][taskId={} type={}] 第{}次重试",
                        task.getId(), step.getStepType(), attempt);
                stepMapper.updateById(new AiFashionTaskStepDO()
                        .setId(step.getId())
                        .setRetryCount(attempt));
            }
            try {
                return executeStage(task, step, api, config, decision,
                        currentBase64, poseFuture, fabricFuture);
            } catch (Exception ex) {
                lastEx = ex;
                if (attempt < maxRetry) {
                    try { Thread.sleep(1000L * (long) Math.pow(2, attempt)); } // 指数退避
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("重试" + maxRetry + "次后仍失败: " + lastEx.getMessage(), lastEx);
    }

    /**
     * 执行单阶段（分发到对应 SD WebUI 端点）
     */
    private String executeStage(AiFashionTaskDO task, AiFashionTaskStepDO step,
            StableDiffusionWebUiApi api, AiFashionQualityConfig config,
            ModelRouteDecision decision, String currentBase64,
            CompletableFuture<String> poseFuture, CompletableFuture<String> fabricFuture) {

        String stepType = step.getStepType().toUpperCase();
        String enhancedPrompt = AiFashionPromptEnhancer.enhance(
                task.getPrompt(), task.getQualityPreset(), stepType);
        String negPrompt = StrUtil.blankToDefault(
                task.getNegativePrompt(), AiFashionPromptEnhancer.defaultNegativePrompt());

        return switch (decision.getSdApiEndpoint()) {
            case "TXT2IMG" -> callTxt2Img(api, task, decision, enhancedPrompt, negPrompt, config);
            case "IMG2IMG" -> callImg2Img(api, task, step, decision, enhancedPrompt, negPrompt,
                    currentBase64, poseFuture, fabricFuture, config);
            case "EXTRAS"  -> callExtras(api, task, config, currentBase64);
            default        -> handlePlaceholder(stepType); // THREE_D
        };
    }

    // ── TXT2IMG ──

    private String callTxt2Img(StableDiffusionWebUiApi api, AiFashionTaskDO task,
            ModelRouteDecision decision, String prompt, String negPrompt,
            AiFashionQualityConfig config) {

        StableDiffusionWebUiImageOptions.OverrideSettings override = null;
        if (StrUtil.isNotBlank(task.getModelCheckpoint())) {
            override = StableDiffusionWebUiImageOptions.OverrideSettings.builder()
                    .sdModelCheckpoint(task.getModelCheckpoint()).build();
        }

        StableDiffusionWebUiApi.Txt2ImgRequest req = StableDiffusionWebUiApi.Txt2ImgRequest.builder()
                .prompt(prompt)
                .negativePrompt(negPrompt)
                .width(task.getWidth())
                .height(task.getHeight())
                .steps(decision.getSteps())
                .cfgScale(decision.getCfgScale())
                .samplerName(decision.getSampler())
                .seed(task.getSeed())
                .overrideSettings(override)
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Txt2ImgResponse> resp = api.txt2img(req);
        return extractFirstImage(resp.getBody() != null ? resp.getBody().getImages() : null,
                "TXT2IMG");
    }

    // ── IMG2IMG ──

    private String callImg2Img(StableDiffusionWebUiApi api, AiFashionTaskDO task,
            AiFashionTaskStepDO step, ModelRouteDecision decision,
            String prompt, String negPrompt, String currentBase64,
            CompletableFuture<String> poseFuture, CompletableFuture<String> fabricFuture,
            AiFashionQualityConfig config) {

        if (StrUtil.isBlank(currentBase64)) {
            throw new IllegalStateException(step.getStepType() + " 阶段需要上一步输出图片，但当前为空");
        }

        String stepType = step.getStepType().toUpperCase();
        List<String> initImages = new ArrayList<>();
        initImages.add(currentBase64);

        // FABRIC/TEXTURE：拼入面料参考图
        if ("TEXTURE".equals(stepType) || "FABRIC".equals(stepType)) {
            String fabricBase64 = getPreloaded(fabricFuture, "面料参考图");
            if (StrUtil.isNotBlank(fabricBase64)) {
                initImages.add(fabricBase64);
                prompt = prompt + ", fabric texture from reference image";
            }
        }

        // ControlNet 透传（POSE / DETAIL）
        Map<String, Object> alwaysonScripts = null;
        if (decision.isUseControlNet()) {
            String cnBase64 = null;
            if ("POSE".equals(stepType)) {
                cnBase64 = getPreloaded(poseFuture, "姿势参考图");
            } else {
                // DETAIL 阶段用当前图自身做 lineart
                cnBase64 = currentBase64;
            }
            if (StrUtil.isNotBlank(cnBase64)) {
                Map<String, Object> cnArg = new HashMap<>();
                cnArg.put("enabled", true);
                cnArg.put("image", cnBase64);
                cnArg.put("module", decision.getControlNetModule());
                cnArg.put("model", decision.getControlNetModel());
                cnArg.put("weight", decision.getControlNetWeight());
                cnArg.put("guidance_start", 0.0);
                cnArg.put("guidance_end", 1.0);
                alwaysonScripts = Map.of("controlnet", Map.of("args", List.of(cnArg)));
            }
        }

        Float denoising = decision.getDenoisingStrength();
        if (denoising == null) denoising = 0.55F;

        StableDiffusionWebUiApi.Img2ImgRequest req = StableDiffusionWebUiApi.Img2ImgRequest.builder()
                .initImages(initImages)
                .prompt(prompt)
                .negativePrompt(negPrompt)
                .denoisingStrength(denoising)
                .steps(decision.getSteps())
                .cfgScale(decision.getCfgScale())
                .samplerName(decision.getSampler())
                .seed(task.getSeed())
                .width(task.getWidth())
                .height(task.getHeight())
                .alwaysonScripts(alwaysonScripts)
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Img2ImgResponse> resp = api.img2img(req);
        return extractFirstImage(resp.getBody() != null ? resp.getBody().getImages() : null,
                "IMG2IMG");
    }

    // ── EXTRAS ──

    private String callExtras(StableDiffusionWebUiApi api, AiFashionTaskDO task,
            AiFashionQualityConfig config, String currentBase64) {

        if (StrUtil.isBlank(currentBase64)) {
            throw new IllegalStateException("UPSAMPLE 阶段需要上一步输出图片");
        }
        int factor = task.getUpscaleFactor() != null ? task.getUpscaleFactor() : config.getUpscaleFactor();
        String upscalerName = StrUtil.blankToDefault(task.getUpscalerName(), config.getUpscalerModel());

        StableDiffusionWebUiApi.ExtraSingleImageRequest req =
                StableDiffusionWebUiApi.ExtraSingleImageRequest.builder()
                        .image(currentBase64)
                        .upscalingResize(factor)
                        .upscaler1(upscalerName)
                        .build();

        ResponseEntity<StableDiffusionWebUiApi.ExtraSingleImageResponse> resp = api.extraSingleImage(req);
        if (resp.getBody() == null || StrUtil.isBlank(resp.getBody().getImage())) {
            throw new IllegalStateException("EXTRAS 返回空图片");
        }
        return resp.getBody().getImage();
    }

    // ── THREE_D 占位 ──

    private String handlePlaceholder(String stepType) {
        log.info("[Orchestrator][{}] 阶段为占位实现，跳过 SD 调用", stepType);
        throw new UnsupportedOperationException(stepType + " 阶段暂未对接 3D 重建服务，已降级跳过");
    }

    // ===== 图片预加载 =====

    private CompletableFuture<String> preloadImageAsync(String url) {
        if (StrUtil.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] bytes = HttpUtil.downloadBytes(url);
                log.debug("[Orchestrator][preload] 下载完成 url={} size={}B", url, bytes.length);
                return Base64.encode(bytes);
            } catch (Exception e) {
                log.warn("[Orchestrator][preload] 下载失败 url={}: {}", url, e.getMessage());
                return null;
            }
        }, preloadExecutor);
    }

    private String getPreloaded(CompletableFuture<String> future, String name) {
        if (future == null) return null;
        try {
            return future.get(STAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Orchestrator] {} 预加载超时/失败: {}", name, e.getMessage());
            return null;
        }
    }

    // ===== 其他工具 =====

    private String extractFirstImage(List<String> images, String endpoint) {
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException(endpoint + " 返回空图片列表");
        }
        return images.get(0);
    }

    private Map<String, Object> buildInputOptions(AiFashionTaskDO task,
            ModelRouteDecision decision) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("prompt", task.getPrompt());
        m.put("negativePrompt", task.getNegativePrompt());
        m.put("width", task.getWidth());
        m.put("height", task.getHeight());
        m.put("seed", task.getSeed());
        m.put("steps", decision.getSteps());
        m.put("cfgScale", decision.getCfgScale());
        m.put("sampler", decision.getSampler());
        m.put("endpoint", decision.getSdApiEndpoint());
        m.put("routingReason", decision.getRoutingReason());
        return m;
    }

    private AiFashionTaskStepDO newStep(Long taskId, String stageType, int order) {
        return new AiFashionTaskStepDO()
                .setTaskId(taskId)
                .setStepType(stageType)
                .setStepOrder(order)
                .setStatus(AiFashionTaskStepStatusEnum.IN_PROGRESS.getStatus())
                .setRetryCount(0);
    }

    private StableDiffusionWebUiApi buildApi() {
        return new StableDiffusionWebUiApi(sdBaseUrl, StrUtil.blankToNull(sdApiKey));
    }

}
