package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskStepMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiImageOptions;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.FASHION_TASK_NOT_EXISTS;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.FASHION_TASK_NOT_FAIL;

/**
 * AI 服装设计流水线任务 Service 实现类
 *
 * <p>流水线顺序：SDXL(txt2img) → POSE(img2img+ControlNet，可选)
 *   → FABRIC(img2img，可选) → UPSCALE(extras，可选)</p>
 *
 * <p>每步产物通过 {@link FileApi#createFile(byte[])} 持久化；
 * 步骤状态实时写回 {@code ai_fashion_task_step}；
 * Upscale 失败时降级：将最终图设为上一步输出并标记 WARN（不中断任务）。</p>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionTaskServiceImpl implements AiFashionTaskService {

    private static final String MDC_TRACE_KEY = "fashionTraceId";

    @Resource
    private AiFashionTaskMapper taskMapper;

    @Resource
    private AiFashionTaskStepMapper stepMapper;

    @Resource
    private FileApi fileApi;

    /**
     * SD WebUI 服务地址，注入自 application 配置
     * 示例：http://103.196.86.126:15112
     */
    @Value("${yudao.ai.stable-diffusion-web-ui.base-url:http://localhost:7860}")
    private String sdBaseUrl;

    @Value("${yudao.ai.stable-diffusion-web-ui.api-key:}")
    private String sdApiKey;

    // ========== 公开接口 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(Long userId, AiFashionTaskCreateReqVO createVO) {
        String traceId = IdUtil.fastSimpleUUID();

        // 1. 构建主任务
        AiFashionTaskDO task = new AiFashionTaskDO()
                .setUserId(userId)
                .setPrompt(createVO.getPrompt())
                .setNegativePrompt(createVO.getNegativePrompt())
                .setWidth(createVO.getWidth() != null ? createVO.getWidth() : 768)
                .setHeight(createVO.getHeight() != null ? createVO.getHeight() : 1024)
                .setSeed(createVO.getSeed() != null ? createVO.getSeed() : -1L)
                .setQualityPreset(createVO.getQualityPreset())
                .setModelCheckpoint(createVO.getModelCheckpoint())
                .setPoseImageUrl(createVO.getPoseImageUrl())
                .setFabricRefUrl(createVO.getFabricRefUrl())
                .setFabricStrength(createVO.getFabricStrength())
                .setUpscale(createVO.getUpscale() != null ? createVO.getUpscale() : Boolean.TRUE)
                .setUpscaleFactor(createVO.getUpscaleFactor() != null ? createVO.getUpscaleFactor() : 2)
                .setUpscalerName(StrUtil.blankToDefault(createVO.getUpscalerName(), "R-ESRGAN 4x+"))
                .setStatus(AiFashionTaskStatusEnum.IN_PROGRESS.getStatus())
                .setTraceId(traceId);
        taskMapper.insert(task);

        // 2. 构建步骤记录（不执行，仅预建记录）
        buildInitialSteps(task).forEach(step -> stepMapper.insert(step));

        // 3. 触发异步执行
        getSelf().executeTaskAsync(task.getId());
        return task.getId();
    }

    @Override
    @Async
    public void executeTaskAsync(Long taskId) {
        AiFashionTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("[executeTaskAsync][taskId({}) 不存在]", taskId);
            return;
        }

        // 写入 MDC 实现日志追踪
        MDC.put(MDC_TRACE_KEY, task.getTraceId());
        try {
            doExecute(task);
        } finally {
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    @Override
    public AiFashionTaskDO getTask(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public List<AiFashionTaskStepDO> getTaskSteps(Long taskId) {
        return stepMapper.selectListByTaskId(taskId);
    }

    @Override
    public PageResult<AiFashionTaskDO> getTaskPage(AiFashionTaskPageReqVO pageReqVO) {
        return taskMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryTask(Long id) {
        // 1. 校验任务存在且处于失败状态
        AiFashionTaskDO task = taskMapper.selectById(id);
        if (task == null) {
            throw exception(FASHION_TASK_NOT_EXISTS);
        }
        if (!AiFashionTaskStatusEnum.FAIL.getStatus().equals(task.getStatus())) {
            throw exception(FASHION_TASK_NOT_FAIL);
        }

        // 2. 重置任务状态
        taskMapper.updateById(new AiFashionTaskDO()
                .setId(id)
                .setStatus(AiFashionTaskStatusEnum.IN_PROGRESS.getStatus())
                .setFinalPicUrl(null)
                .setErrorMessage(null)
                .setStartTime(null)
                .setFinishTime(null));

        // 3. 重置所有步骤状态，重试计数 +1
        List<AiFashionTaskStepDO> steps = stepMapper.selectListByTaskId(id);
        for (AiFashionTaskStepDO step : steps) {
            stepMapper.updateById(new AiFashionTaskStepDO()
                    .setId(step.getId())
                    .setStatus(AiFashionTaskStepStatusEnum.IN_PROGRESS.getStatus())
                    .setOutputPicUrl(null)
                    .setErrorMessage(null)
                    .setDurationMs(null)
                    .setRetryCount(step.getRetryCount() + 1));
        }

        // 4. 异步重新执行
        getSelf().executeTaskAsync(id);
    }

    // ========== 私有方法 ==========

    /**
     * 根据任务配置预建步骤记录（status=IN_PROGRESS）
     */
    private List<AiFashionTaskStepDO> buildInitialSteps(AiFashionTaskDO task) {
        List<AiFashionTaskStepDO> steps = new ArrayList<>();
        int order = 0;

        // 步骤 0：SDXL 始终存在
        steps.add(newStep(task.getId(), AiFashionTaskStepTypeEnum.SDXL, order++));

        // 步骤 1：POSE（可选）
        if (StrUtil.isNotBlank(task.getPoseImageUrl())) {
            steps.add(newStep(task.getId(), AiFashionTaskStepTypeEnum.POSE, order++));
        }

        // 步骤 2：FABRIC（可选）
        if (StrUtil.isNotBlank(task.getFabricRefUrl())) {
            steps.add(newStep(task.getId(), AiFashionTaskStepTypeEnum.FABRIC, order++));
        }

        // 步骤 3：UPSCALE（可选）
        if (Boolean.TRUE.equals(task.getUpscale())) {
            steps.add(newStep(task.getId(), AiFashionTaskStepTypeEnum.UPSCALE, order));
        }

        return steps;
    }

    private AiFashionTaskStepDO newStep(Long taskId, AiFashionTaskStepTypeEnum type, int order) {
        return new AiFashionTaskStepDO()
                .setTaskId(taskId)
                .setStepType(type.getType())
                .setStepOrder(order)
                .setStatus(AiFashionTaskStepStatusEnum.IN_PROGRESS.getStatus())
                .setRetryCount(0);
    }

    /**
     * 实际执行流水线（同步，运行在 @Async 线程）
     */
    private void doExecute(AiFashionTaskDO task) {
        // 更新开始时间
        taskMapper.updateById(new AiFashionTaskDO().setId(task.getId())
                .setStartTime(LocalDateTime.now()));

        StableDiffusionWebUiApi api = buildApi();
        List<AiFashionTaskStepDO> steps = stepMapper.selectListByTaskId(task.getId());

        String currentImageBase64 = null; // 当前步骤的输入图片 base64
        String currentPicUrl = null;      // 当前步骤产物的持久化 URL

        try {
            for (AiFashionTaskStepDO step : steps) {
                long stepStart = System.currentTimeMillis();
                String stepType = step.getStepType();

                log.info("[FashionPipeline][taskId={} step={} type={}] 开始执行",
                        task.getId(), step.getStepOrder(), stepType);

                try {
                    if (AiFashionTaskStepTypeEnum.SDXL.getType().equals(stepType)) {
                        currentImageBase64 = executeSDXL(task, step, api);
                    } else if (AiFashionTaskStepTypeEnum.POSE.getType().equals(stepType)) {
                        currentImageBase64 = executePose(task, step, api, currentImageBase64);
                    } else if (AiFashionTaskStepTypeEnum.FABRIC.getType().equals(stepType)) {
                        currentImageBase64 = executeFabric(task, step, api, currentImageBase64);
                    } else if (AiFashionTaskStepTypeEnum.UPSCALE.getType().equals(stepType)) {
                        currentImageBase64 = executeUpscale(task, step, api, currentImageBase64, currentPicUrl);
                    }

                    // 上传产物到 FileApi
                    byte[] imageBytes = Base64.decode(currentImageBase64);
                    currentPicUrl = fileApi.createFile(imageBytes);

                    long durationMs = System.currentTimeMillis() - stepStart;
                    log.info("[FashionPipeline][taskId={} step={} type={}] 完成，耗时 {}ms，产物：{}",
                            task.getId(), step.getStepOrder(), stepType, durationMs, currentPicUrl);

                    // 更新步骤为成功
                    stepMapper.updateById(new AiFashionTaskStepDO()
                            .setId(step.getId())
                            .setStatus(AiFashionTaskStepStatusEnum.SUCCESS.getStatus())
                            .setOutputPicUrl(currentPicUrl)
                            .setDurationMs(durationMs));

                } catch (Exception ex) {
                    long durationMs = System.currentTimeMillis() - stepStart;
                    log.error("[FashionPipeline][taskId={} step={} type={}] 步骤失败，耗时 {}ms",
                            task.getId(), step.getStepOrder(), stepType, durationMs, ex);

                    // UPSCALE 失败降级：不中断任务，使用上一步产物作为最终图并标记 WARN
                    if (AiFashionTaskStepTypeEnum.UPSCALE.getType().equals(stepType)) {
                        log.warn("[FashionPipeline][taskId={}] UPSCALE 步骤失败，降级使用上一步产物: {}",
                                task.getId(), currentPicUrl);
                        stepMapper.updateById(new AiFashionTaskStepDO()
                                .setId(step.getId())
                                .setStatus(AiFashionTaskStepStatusEnum.FAIL.getStatus())
                                .setDurationMs(durationMs)
                                .setErrorMessage("[WARN-DEGRADED] " + ex.getMessage()));
                        // currentPicUrl 保持上一步的值，继续完成任务
                        break;
                    }

                    // 其他步骤失败：整体任务失败
                    stepMapper.updateById(new AiFashionTaskStepDO()
                            .setId(step.getId())
                            .setStatus(AiFashionTaskStepStatusEnum.FAIL.getStatus())
                            .setDurationMs(durationMs)
                            .setErrorMessage(ex.getMessage()));

                    taskMapper.updateById(new AiFashionTaskDO()
                            .setId(task.getId())
                            .setStatus(AiFashionTaskStatusEnum.FAIL.getStatus())
                            .setErrorMessage("[" + stepType + "] " + ex.getMessage())
                            .setFinishTime(LocalDateTime.now()));
                    return;
                }
            }

            // 所有步骤完成，更新任务为成功
            taskMapper.updateById(new AiFashionTaskDO()
                    .setId(task.getId())
                    .setStatus(AiFashionTaskStatusEnum.SUCCESS.getStatus())
                    .setFinalPicUrl(currentPicUrl)
                    .setFinishTime(LocalDateTime.now()));
            log.info("[FashionPipeline][taskId={}] 全流水线完成，最终图：{}", task.getId(), currentPicUrl);

        } catch (Exception ex) {
            log.error("[FashionPipeline][taskId={}] 未捕获异常", task.getId(), ex);
            taskMapper.updateById(new AiFashionTaskDO()
                    .setId(task.getId())
                    .setStatus(AiFashionTaskStatusEnum.FAIL.getStatus())
                    .setErrorMessage(ex.getMessage())
                    .setFinishTime(LocalDateTime.now()));
        }
    }

    // ===== 各步骤执行方法 =====

    /**
     * SDXL 步骤：txt2img 生成基础服装设计图
     */
    private String executeSDXL(AiFashionTaskDO task, AiFashionTaskStepDO step,
                                StableDiffusionWebUiApi api) {
        StableDiffusionWebUiImageOptions.OverrideSettings overrideSettings = null;
        if (StrUtil.isNotBlank(task.getModelCheckpoint())) {
            overrideSettings = StableDiffusionWebUiImageOptions.OverrideSettings.builder()
                    .sdModelCheckpoint(task.getModelCheckpoint())
                    .build();
        }

        Map<String, Object> inputOptions = buildBaseInputOptions(task);
        stepMapper.updateById(new AiFashionTaskStepDO()
                .setId(step.getId())
                .setModelName(task.getModelCheckpoint())
                .setInputOptions(inputOptions));

        StableDiffusionWebUiApi.Txt2ImgRequest request = StableDiffusionWebUiApi.Txt2ImgRequest.builder()
                .prompt(task.getPrompt())
                .negativePrompt(task.getNegativePrompt())
                .width(task.getWidth())
                .height(task.getHeight())
                .steps(resolveSteps(task.getQualityPreset()))
                .cfgScale(7.0F)
                .samplerName("DPM++ 2M Karras")
                .seed(task.getSeed())
                .overrideSettings(overrideSettings)
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Txt2ImgResponse> resp = api.txt2img(request);
        List<String> images = resp.getBody() != null ? resp.getBody().getImages() : Collections.emptyList();
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("SDXL 返回空图片列表");
        }

        // 更新输出元信息
        if (resp.getBody() != null && resp.getBody().getInfo() != null) {
            stepMapper.updateById(new AiFashionTaskStepDO()
                    .setId(step.getId())
                    .setOutputOptions(Map.of("info", resp.getBody().getInfo())));
        }
        return images.get(0);
    }

    /**
     * POSE 步骤：img2img + ControlNet openpose 透传
     */
    private String executePose(AiFashionTaskDO task, AiFashionTaskStepDO step,
                                StableDiffusionWebUiApi api, String baseImageBase64) {
        // 下载姿势参考图并转 base64
        String poseBase64 = downloadToBase64(task.getPoseImageUrl());

        // ControlNet alwayson_scripts 透传
        Map<String, Object> controlnetArg = new HashMap<>();
        controlnetArg.put("enabled", true);
        controlnetArg.put("image", poseBase64);
        controlnetArg.put("module", "openpose");
        controlnetArg.put("model", "control_openpose-fp16 [9ca67cc5]");
        controlnetArg.put("weight", 1.0);

        Map<String, Object> alwaysonScripts = Map.of(
                "controlnet", Map.of("args", List.of(controlnetArg)));

        Map<String, Object> inputOptions = new HashMap<>(buildBaseInputOptions(task));
        inputOptions.put("poseImageUrl", task.getPoseImageUrl());
        stepMapper.updateById(new AiFashionTaskStepDO()
                .setId(step.getId())
                .setInputPicUrl(task.getPoseImageUrl())
                .setInputOptions(inputOptions));

        StableDiffusionWebUiApi.Img2ImgRequest request = StableDiffusionWebUiApi.Img2ImgRequest.builder()
                .initImages(List.of(baseImageBase64))
                .prompt(task.getPrompt())
                .negativePrompt(task.getNegativePrompt())
                .denoisingStrength(0.5F)
                .steps(resolveSteps(task.getQualityPreset()))
                .cfgScale(7.0F)
                .samplerName("DPM++ 2M Karras")
                .seed(task.getSeed())
                .width(task.getWidth())
                .height(task.getHeight())
                .alwaysonScripts(alwaysonScripts)
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Img2ImgResponse> resp = api.img2img(request);
        List<String> images = resp.getBody() != null ? resp.getBody().getImages() : Collections.emptyList();
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("POSE(img2img) 返回空图片列表");
        }
        return images.get(0);
    }

    /**
     * FABRIC 步骤：img2img 面料转换
     */
    private String executeFabric(AiFashionTaskDO task, AiFashionTaskStepDO step,
                                  StableDiffusionWebUiApi api, String currentImageBase64) {
        float strength = task.getFabricStrength() != null
                ? task.getFabricStrength().floatValue() : 0.70F;

        Map<String, Object> inputOptions = new HashMap<>(buildBaseInputOptions(task));
        inputOptions.put("fabricRefUrl", task.getFabricRefUrl());
        inputOptions.put("fabricStrength", strength);
        stepMapper.updateById(new AiFashionTaskStepDO()
                .setId(step.getId())
                .setInputPicUrl(task.getFabricRefUrl())
                .setInputOptions(inputOptions));

        // 将面料参考图作为 init_image 之一
        String fabricBase64 = downloadToBase64(task.getFabricRefUrl());

        StableDiffusionWebUiApi.Img2ImgRequest request = StableDiffusionWebUiApi.Img2ImgRequest.builder()
                .initImages(List.of(currentImageBase64, fabricBase64))
                .prompt(task.getPrompt() + ", fabric texture from reference")
                .negativePrompt(task.getNegativePrompt())
                .denoisingStrength(strength)
                .steps(resolveSteps(task.getQualityPreset()))
                .cfgScale(7.0F)
                .samplerName("DPM++ 2M Karras")
                .seed(task.getSeed())
                .width(task.getWidth())
                .height(task.getHeight())
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Img2ImgResponse> resp = api.img2img(request);
        List<String> images = resp.getBody() != null ? resp.getBody().getImages() : Collections.emptyList();
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("FABRIC(img2img) 返回空图片列表");
        }
        return images.get(0);
    }

    /**
     * UPSCALE 步骤：extras-single-image 超分辨率
     *
     * <p>若失败，调用方捕获异常并降级，不影响最终图输出。</p>
     */
    private String executeUpscale(AiFashionTaskDO task, AiFashionTaskStepDO step,
                                   StableDiffusionWebUiApi api, String currentImageBase64,
                                   String currentPicUrl) {
        int factor = task.getUpscaleFactor() != null ? task.getUpscaleFactor() : 2;
        String upscalerName = StrUtil.blankToDefault(task.getUpscalerName(), "R-ESRGAN 4x+");

        Map<String, Object> inputOptions = Map.of(
                "upscaleFactor", factor,
                "upscalerName", upscalerName,
                "inputPicUrl", StrUtil.blankToDefault(currentPicUrl, ""));
        stepMapper.updateById(new AiFashionTaskStepDO()
                .setId(step.getId())
                .setInputPicUrl(currentPicUrl)
                .setInputOptions(inputOptions)
                .setModelName(upscalerName));

        StableDiffusionWebUiApi.ExtraSingleImageRequest request =
                StableDiffusionWebUiApi.ExtraSingleImageRequest.builder()
                        .image(currentImageBase64)
                        .upscalingResize(factor)
                        .upscaler1(upscalerName)
                        .build();

        ResponseEntity<StableDiffusionWebUiApi.ExtraSingleImageResponse> resp = api.extraSingleImage(request);
        if (resp.getBody() == null || StrUtil.isBlank(resp.getBody().getImage())) {
            throw new IllegalStateException("UPSCALE(extras) 返回空图片");
        }
        return resp.getBody().getImage();
    }

    // ===== 辅助方法 =====

    private Map<String, Object> buildBaseInputOptions(AiFashionTaskDO task) {
        Map<String, Object> map = new HashMap<>();
        map.put("prompt", task.getPrompt());
        map.put("negativePrompt", task.getNegativePrompt());
        map.put("width", task.getWidth());
        map.put("height", task.getHeight());
        map.put("seed", task.getSeed());
        return map;
    }

    /**
     * 根据质量预设决定推理步数
     */
    private int resolveSteps(String qualityPreset) {
        if ("HIGH".equalsIgnoreCase(qualityPreset)) {
            return 40;
        } else if ("LOW".equalsIgnoreCase(qualityPreset)) {
            return 15;
        }
        return 25; // MEDIUM / 默认
    }

    /**
     * 下载 URL 图片并返回 base64 字符串（不含 data URI 前缀）
     */
    private String downloadToBase64(String url) {
        byte[] bytes = cn.hutool.http.HttpUtil.downloadBytes(url);
        return Base64.encode(bytes);
    }

    /**
     * 构建 SD WebUI API 客户端
     */
    private StableDiffusionWebUiApi buildApi() {
        return new StableDiffusionWebUiApi(sdBaseUrl, StrUtil.blankToNull(sdApiKey));
    }

    /**
     * 获取自身代理，确保 @Async 生效
     */
    private AiFashionTaskServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
