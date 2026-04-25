package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionAgentChatRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dConvertReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionAgentTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionSessionDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionAgentTaskMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionSessionMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionIntentEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionQualityPresetEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionWorkflowModeEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 服装设计智能体 Service 实现类
 *
 * <p><strong>核心理念：纯自然语言驱动，零按钮，全自动多智能体链路执行。</strong></p>
 *
 * <p>一句话可触发完整链路，例如：</p>
 * <pre>
 * 输入：帮我设计5款甜酷风红色连衣裙然后转3D旋转90度看看
 *
 * 自动执行链路：
 *   STEP 0 – BATCH_GENERATE(5款甜酷风红色连衣裙)  [SDXL→TEXTURE→UPSAMPLE × 5，并行]
 *   STEP 1 – GENERATE_3D                          [取第0步第一个taskId，深度估计→网格→纹理]
 *   STEP 2 – ROTATE(90°)                          [取第1步 assetId，生成旋转预览+GIF]
 *
 * 全程 SSE 实时推送，前端无需任何手动操作。
 * </pre>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionAgentServiceImpl implements AiFashionAgentService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 旋转角度提取：旋转90度 / 转90 / 90度
    private static final Pattern ROTATE_ANGLE_PATTERN =
            Pattern.compile("旋转?\\s*(\\d+)\\s*度|转\\s*(\\d+)\\s*度|(\\d+)\\s*度.*旋转");

    // 3D 关键词
    private static final List<String> THREE_D_KEYWORDS =
            Arrays.asList("3d", "3D", "三维", "立体", "转3d", "做成3d", "生成3d");

    // 旋转关键词
    private static final List<String> ROTATE_KEYWORDS =
            Arrays.asList("旋转", "转动", "转一下", "换角度", "旋转360", "360度");

    // 任务完成轮询超时（毫秒）
    private static final long TASK_POLL_TIMEOUT_MS = 5 * 60 * 1000L;
    private static final long TASK_POLL_INTERVAL_MS = 3_000L;

    @Resource
    private AiFashionNlpParserService nlpParser;
    @Resource
    private AiFashionSessionMapper sessionMapper;
    @Resource
    private AiFashionAgentTaskMapper agentTaskMapper;
    @Resource
    private AiFashionTaskMapper fashionTaskMapper;
    @Resource
    private AiFashionTaskService fashionTaskService;
    @Resource
    private AiFashion3dService threeDService;
    @Resource
    private AiFashionSseProgressService sseService;

    // ─────────────────────────────────────────────────────────────────────────
    // 公开接口
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiFashionAgentChatRespVO chat(Long userId, AiFashionSmartChatReqVO reqVO) {
        String traceId = IdUtil.fastSimpleUUID();
        MDC.put("traceId", traceId);
        MDC.put("userId", String.valueOf(userId));

        try {
            // 1. 会话
            AiFashionSessionDO session = getOrCreateSession(userId, reqVO.getSessionToken());
            AiFashionSessionContext ctx = buildContext(session);

            // 2. 解析主意图
            AiFashionIntentParseResult mainParsed = nlpParser.parse(reqVO.getMessage(), ctx);
            log.info("[Agent.chat] 主意图={} batch={} prompt={}",
                    mainParsed.getIntent(), mainParsed.getBatchCount(), mainParsed.getBasePrompt());

            // 3. 组建意图链
            AiFashionIntentChain chain = buildChain(reqVO.getMessage(), mainParsed, ctx);

            // 4. 创建链路 ID 并持久化 agent task 记录
            String chainId = "CHAIN_" + IdUtil.fastHexId().substring(0, 8).toUpperCase();
            List<AiFashionAgentTaskDO> agentTasks = persistChain(chainId, userId, chain);

            // 5. 更新会话
            updateSession(session, mainParsed);

            // 6. 异步执行
            executeChainAsync(chainId, userId, agentTasks);

            // 7. 立即返回
            return buildChatResp(chainId, chain, agentTasks, session.getSessionToken());

        } finally {
            MDC.remove("traceId");
            MDC.remove("userId");
        }
    }

    @Override
    public AiFashionAgentChatRespVO queryChain(String chainId) {
        List<AiFashionAgentTaskDO> tasks = agentTaskMapper.selectByChainId(chainId);
        if (tasks.isEmpty()) {
            return AiFashionAgentChatRespVO.builder()
                    .chainId(chainId)
                    .chainStatus("NOT_FOUND")
                    .agentReply("未找到链路 " + chainId)
                    .build();
        }
        return buildQueryResp(chainId, tasks);
    }

    @Override
    public void retryFromStep(String chainId, int stepOrder) {
        List<AiFashionAgentTaskDO> tasks = agentTaskMapper.selectByChainId(chainId);
        if (tasks.isEmpty()) {
            return;
        }
        // 重置指定步骤及其之后的步骤为 PENDING
        tasks.stream()
                .filter(t -> t.getStepOrder() >= stepOrder)
                .forEach(t -> {
                    t.setStatus("PENDING");
                    t.setProgress(0);
                    t.setResultJson(null);
                    t.setErrorMessage(null);
                    agentTaskMapper.updateById(t);
                });
        List<AiFashionAgentTaskDO> toExec = tasks.stream()
                .filter(t -> t.getStepOrder() >= stepOrder)
                .sorted(Comparator.comparingInt(AiFashionAgentTaskDO::getStepOrder))
                .collect(Collectors.toList());
        Long userId = tasks.get(0).getUserId();
        executeChainAsync(chainId, userId, toExec);
    }

    @Override
    public List<AiFashionAgentChatRespVO> recentChains(Long userId, int limit) {
        List<AiFashionAgentTaskDO> heads =
                agentTaskMapper.selectRecentChainHeadsByUser(userId, limit);
        return heads.stream()
                .map(h -> queryChain(h.getChainId()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 链路构建
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 从用户输入构建意图链。
     *
     * <p>规则：</p>
     * <ol>
     *   <li>主解析结果（设计/修改类）作为 STEP 0（若不是纯 3D 请求）</li>
     *   <li>检测到 3D 关键词 → 追加 GENERATE_3D 步骤</li>
     *   <li>检测到旋转关键词 → 追加 ROTATE 步骤（需前有 3D 步骤）</li>
     * </ol>
     */
    private AiFashionIntentChain buildChain(String input,
                                            AiFashionIntentParseResult mainParsed,
                                            AiFashionSessionContext ctx) {
        List<AiFashionIntentParseResult> steps = new ArrayList<>();
        boolean inputLower = false; // 仅用于变量名合法

        boolean has3d = has3dKeyword(input);
        boolean hasRotate = hasRotateKeyword(input);
        int rotateAngle = extractRotateAngle(input);

        // STEP 0：主意图（设计/修改/批量）
        if (mainParsed.getIntent() != AiFashionIntentEnum.GENERATE_3D) {
            steps.add(mainParsed);
        }

        // 追加 3D 步骤
        if (has3d) {
            steps.add(AiFashionIntentParseResult.builder()
                    .intent(AiFashionIntentEnum.GENERATE_3D)
                    .batchCount(1)
                    .basePrompt(mainParsed.getBasePrompt())
                    .interpretation("生成3D模型")
                    .workflowMode(AiFashionWorkflowModeEnum.FULL.name())
                    .qualityPreset(AiFashionQualityPresetEnum.HIGH.name())
                    .build());
        }

        // 追加旋转步骤（需有 3D）
        if (hasRotate && has3d) {
            steps.add(AiFashionIntentParseResult.builder()
                    .intent(AiFashionIntentEnum.GENERATE_3D) // 复用 3D intent，rotate 在 agent 层区分
                    .batchCount(1)
                    .basePrompt("rotate:" + rotateAngle)     // 特殊标记
                    .interpretation("旋转查看 " + rotateAngle + "°")
                    .workflowMode(AiFashionWorkflowModeEnum.FULL.name())
                    .qualityPreset(AiFashionQualityPresetEnum.HIGH.name())
                    .build());
        }

        // 若输入纯粹是 3D/旋转（无设计步骤），用 sessionCtx 的 lastTaskId 作为 3D 输入
        if (steps.isEmpty()) {
            steps.add(mainParsed);
        }

        // 构建人类可读描述
        String desc = buildChainDesc(steps);

        return AiFashionIntentChain.builder()
                .steps(steps)
                .chainDescription(desc)
                .autoChain(true)
                .build();
    }

    private boolean has3dKeyword(String input) {
        String lower = input.toLowerCase();
        return THREE_D_KEYWORDS.stream().anyMatch(k -> lower.contains(k.toLowerCase()));
    }

    private boolean hasRotateKeyword(String input) {
        return ROTATE_KEYWORDS.stream().anyMatch(input::contains)
                || ROTATE_ANGLE_PATTERN.matcher(input).find();
    }

    private int extractRotateAngle(String input) {
        Matcher m = ROTATE_ANGLE_PATTERN.matcher(input);
        if (m.find()) {
            for (int g = 1; g <= m.groupCount(); g++) {
                if (m.group(g) != null) {
                    return Integer.parseInt(m.group(g));
                }
            }
        }
        return 360; // 默认 360 全圈
    }

    private String buildChainDesc(List<AiFashionIntentParseResult> steps) {
        StringBuilder sb = new StringBuilder("为您自动执行：");
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(steps.get(i).getInterpretation());
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 持久化
    // ─────────────────────────────────────────────────────────────────────────

    private List<AiFashionAgentTaskDO> persistChain(String chainId, Long userId,
                                                    AiFashionIntentChain chain) {
        List<AiFashionAgentTaskDO> result = new ArrayList<>();
        List<AiFashionIntentParseResult> steps = chain.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            AiFashionIntentParseResult step = steps.get(i);
            AiFashionAgentTaskDO do_ = new AiFashionAgentTaskDO()
                    .setChainId(chainId)
                    .setUserId(userId)
                    .setStepOrder(i)
                    .setIntent(step.getIntent().getCode())
                    .setIntentDesc(step.getInterpretation())
                    .setPrompt(step.getBasePrompt())
                    .setStatus("PENDING")
                    .setProgress(0)
                    .setNotified(false);
            agentTaskMapper.insert(do_);
            result.add(do_);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 异步执行链路
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    public void executeChainAsync(String chainId, Long userId,
                                  List<AiFashionAgentTaskDO> agentTasks) {
        MDC.put("chainId", chainId);
        MDC.put("userId", String.valueOf(userId));
        int total = agentTasks.size();
        String previousResultJson = null;

        try {
            for (AiFashionAgentTaskDO agentTask : agentTasks) {
                int stepOrder = agentTask.getStepOrder();
                log.info("[Agent][chain={}][step={}/{}] 开始执行: {}",
                        chainId, stepOrder, total - 1, agentTask.getIntentDesc());

                // 标记 RUNNING
                updateStep(agentTask, "RUNNING", 5, null, null);
                sseService.pushProgress(chainId, stepOrder, total,
                        "正在执行: " + agentTask.getIntentDesc(),
                        calcChainPct(stepOrder, total, 10));

                long startMs = System.currentTimeMillis();
                try {
                    String resultJson = dispatch(agentTask, previousResultJson, userId);
                    long dur = System.currentTimeMillis() - startMs;
                    agentTask.setDurationMs(dur);

                    updateStep(agentTask, "SUCCESS", 100, resultJson, null);
                    sseService.pushStepDone(chainId, stepOrder,
                            agentTask.getIntentDesc(), resultJson);
                    previousResultJson = resultJson;

                    log.info("[Agent][chain={}][step={}] 完成 耗时={}ms",
                            chainId, stepOrder, dur);

                } catch (Exception ex) {
                    log.error("[Agent][chain={}][step={}] 失败", chainId, stepOrder, ex);
                    updateStep(agentTask, "FAIL", 0, null, ex.getMessage());
                    sseService.pushError(chainId, stepOrder, ex.getMessage(), false);
                    // 关键步骤（设计生成）失败则中断链路
                    if (isCriticalIntent(agentTask.getIntent())) {
                        sseService.pushChainDone(chainId,
                                "第 " + (stepOrder + 1) + " 步失败，链路中断：" + ex.getMessage(),
                                "{}");
                        return;
                    }
                    // 非关键步骤降级继续（3D/旋转失败不影响设计图）
                    sseService.pushError(chainId, stepOrder, "已降级，跳过此步骤", true);
                }
            }

            // 全部完成
            String summaryMsg = buildDoneSummary(agentTasks);
            String finalJson = buildFinalJson(agentTasks);
            sseService.pushChainDone(chainId, summaryMsg, finalJson);
            log.info("[Agent][chain={}] 链路完成", chainId);

        } finally {
            MDC.remove("chainId");
            MDC.remove("userId");
        }
    }

    /**
     * 根据 intent 分派到具体执行逻辑。
     */
    private String dispatch(AiFashionAgentTaskDO agentTask, String previousResultJson,
                            Long userId) throws Exception {
        String intent = agentTask.getIntent();
        String prompt = agentTask.getPrompt();

        // 旋转步骤（通过特殊 prompt 标记区分）
        if (prompt != null && prompt.startsWith("rotate:")) {
            int angle = Integer.parseInt(prompt.substring("rotate:".length()));
            return executeRotate(agentTask, previousResultJson, userId, angle);
        }

        AiFashionIntentEnum ie = AiFashionIntentEnum.valueOf(intent);
        switch (ie) {
            case GENERATE_3D:
                return execute3d(agentTask, previousResultJson, userId);
            case NEW_DESIGN:
            case BATCH_GENERATE:
            case BATCH_VARIANT:
            case MODIFY_CURRENT:
            case MODIFY_COLOR:
            case MODIFY_STYLE:
            case MODIFY_FABRIC:
            case MODIFY_LENGTH:
            case MODIFY_FIT:
            default:
                return executeDesign(agentTask, previousResultJson, userId);
        }
    }

    // ── 设计步骤 ──────────────────────────────────────────────────────────────

    private String executeDesign(AiFashionAgentTaskDO agentTask, String previousResultJson,
                                 Long userId) throws Exception {
        // 构建创建请求
        AiFashionTaskCreateReqVO createReq = new AiFashionTaskCreateReqVO();
        createReq.setPrompt(agentTask.getPrompt());
        createReq.setWidth(768);
        createReq.setHeight(1024);

        String intent = agentTask.getIntent();
        boolean isBatch = AiFashionIntentEnum.BATCH_GENERATE.getCode().equals(intent)
                || AiFashionIntentEnum.BATCH_VARIANT.getCode().equals(intent);

        if (isBatch) {
            // 批量：解析 prompt 中携带的 batch 元数据（格式：{BATCH:5}实际prompt）
            int batchCount = extractBatchCount(agentTask.getPrompt());
            List<Long> taskIds = new ArrayList<>();
            List<String> prompts = buildVariantPrompts(agentTask.getPrompt(), batchCount);
            String chainId = agentTask.getChainId();
            int total = batchCount;

            for (int i = 0; i < prompts.size(); i++) {
                createReq.setPrompt(prompts.get(i));
                Long taskId = fashionTaskService.createTask(userId, createReq);
                taskIds.add(taskId);
                sseService.pushProgress(chainId, agentTask.getStepOrder(),
                        agentTask.getStepOrder() + 1,
                        agentTask.getIntentDesc() + " (" + (i + 1) + "/" + total + ")",
                        calcChainPct(i, total, 80));
            }
            // 等待全部完成
            for (Long tid : taskIds) {
                waitForTask(tid);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("taskIds", taskIds);
            result.put("batchCount", taskIds.size());
            // 取第一个任务的图片 URL 作为代表
            AiFashionTaskDO first = fashionTaskMapper.selectById(taskIds.get(0));
            if (first != null && StrUtil.isNotBlank(first.getFinalPicUrl())) {
                result.put("picUrl", first.getFinalPicUrl());
            }
            return MAPPER.writeValueAsString(result);
        } else {
            // 单款
            Long taskId = fashionTaskService.createTask(userId, createReq);
            waitForTask(taskId);
            AiFashionTaskDO task = fashionTaskMapper.selectById(taskId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("taskId", taskId);
            result.put("picUrl", task != null ? task.getFinalPicUrl() : null);
            return MAPPER.writeValueAsString(result);
        }
    }

    // ── 3D 步骤 ───────────────────────────────────────────────────────────────

    private String execute3d(AiFashionAgentTaskDO agentTask, String previousResultJson,
                             Long userId) throws Exception {
        // 从上一步结果中取图片 URL 或 taskId
        String sourceUrl = extractPicUrl(previousResultJson);
        Long sourceTaskId = extractTaskId(previousResultJson);

        AiFashion3dConvertReqVO req = new AiFashion3dConvertReqVO();
        if (StrUtil.isNotBlank(sourceUrl)) {
            req.setSourceImageUrl(sourceUrl);
        } else if (sourceTaskId != null) {
            req.setSourceTaskId(sourceTaskId);
        } else {
            throw new IllegalStateException("3D步骤找不到上一步产出图片，请先完成设计步骤");
        }
        req.setOutputFormat("obj");
        req.setGenerateRotations(true);
        req.setColorChange(null);

        var result3d = threeDService.convert(userId, req);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assetId", result3d.getAssetId());
        result.put("objUrl", result3d.getObjUrl());
        result.put("previewUrls", result3d.getPreviewUrls());
        result.put("rotationGifUrl", result3d.getRotationGifUrl());
        result.put("vertexCount", result3d.getVertexCount());
        result.put("faceCount", result3d.getFaceCount());
        return MAPPER.writeValueAsString(result);
    }

    // ── 旋转步骤 ──────────────────────────────────────────────────────────────

    private String executeRotate(AiFashionAgentTaskDO agentTask, String previousResultJson,
                                 Long userId, int angle) throws Exception {
        Long assetId = extractAssetId(previousResultJson);
        if (assetId == null) {
            throw new IllegalStateException("旋转步骤找不到3D资产ID，请先完成3D生成步骤");
        }

        AiFashion3dConvertReqVO req = new AiFashion3dConvertReqVO();
        req.setSourceAssetId(assetId);
        req.setRotationAngle(angle);
        req.setGenerateRotations(true);

        var result3d = threeDService.rotate(userId, req);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assetId", result3d.getAssetId());
        result.put("rotationAngle", angle);
        result.put("previewUrls", result3d.getPreviewUrls());
        result.put("rotationGifUrl", result3d.getRotationGifUrl());
        return MAPPER.writeValueAsString(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 任务轮询等待
    // ─────────────────────────────────────────────────────────────────────────

    private void waitForTask(Long taskId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TASK_POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            AiFashionTaskDO task = fashionTaskMapper.selectById(taskId);
            if (task == null) {
                break;
            }
            String status = task.getStatus();
            if ("SUCCESS".equals(status) || "FAIL".equals(status)) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(TASK_POLL_INTERVAL_MS);
        }
        log.warn("[Agent] 等待任务 {} 超时", taskId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 辅助工具
    // ─────────────────────────────────────────────────────────────────────────

    private void updateStep(AiFashionAgentTaskDO task, String status, int pct,
                            String resultJson, String errorMsg) {
        task.setStatus(status);
        task.setProgress(pct);
        if (resultJson != null) task.setResultJson(resultJson);
        if (errorMsg != null) task.setErrorMessage(errorMsg);
        agentTaskMapper.updateById(task);
    }

    private boolean isCriticalIntent(String intent) {
        return AiFashionIntentEnum.NEW_DESIGN.getCode().equals(intent)
                || AiFashionIntentEnum.BATCH_GENERATE.getCode().equals(intent)
                || AiFashionIntentEnum.BATCH_VARIANT.getCode().equals(intent)
                || AiFashionIntentEnum.MODIFY_CURRENT.getCode().equals(intent)
                || AiFashionIntentEnum.MODIFY_COLOR.getCode().equals(intent);
    }

    private int calcChainPct(int done, int total, int base) {
        if (total <= 0) return base;
        return base + (int) ((100.0 - base) * done / total);
    }

    private int extractBatchCount(String prompt) {
        if (prompt == null) return 1;
        Matcher m = Pattern.compile("\\{BATCH:(\\d+)\\}").matcher(prompt);
        return m.find() ? Integer.parseInt(m.group(1)) : 1;
    }

    private List<String> buildVariantPrompts(String prompt, int count) {
        String base = prompt.replaceAll("\\{BATCH:\\d+\\}", "").trim();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(base + ", design variant " + (i + 1) + ", seed " + (i * 777));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private String extractPicUrl(String json) {
        if (StrUtil.isBlank(json)) return null;
        try {
            Map<String, Object> m = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object v = m.get("picUrl");
            return v instanceof String ? (String) v : null;
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Long extractTaskId(String json) {
        if (StrUtil.isBlank(json)) return null;
        try {
            Map<String, Object> m = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object v = m.getOrDefault("taskId", m.get("taskIds"));
            if (v instanceof Number) return ((Number) v).longValue();
            if (v instanceof List && !((List<?>) v).isEmpty()) {
                Object first = ((List<?>) v).get(0);
                if (first instanceof Number) return ((Number) first).longValue();
            }
            return null;
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Long extractAssetId(String json) {
        if (StrUtil.isBlank(json)) return null;
        try {
            Map<String, Object> m = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object v = m.get("assetId");
            return v instanceof Number ? ((Number) v).longValue() : null;
        } catch (Exception e) { return null; }
    }

    private String buildDoneSummary(List<AiFashionAgentTaskDO> tasks) {
        long ok = tasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
        long fail = tasks.stream().filter(t -> "FAIL".equals(t.getStatus())).count();
        if (fail == 0) {
            return "✅ 全部完成！共执行 " + ok + " 个步骤。";
        } else {
            return "⚠️ 完成 " + ok + " 步，" + fail + " 步失败（已降级跳过）。";
        }
    }

    private String buildFinalJson(List<AiFashionAgentTaskDO> tasks) {
        try {
            Map<String, Object> final_ = new LinkedHashMap<>();
            for (AiFashionAgentTaskDO t : tasks) {
                if ("SUCCESS".equals(t.getStatus()) && StrUtil.isNotBlank(t.getResultJson())) {
                    final_.put("step_" + t.getStepOrder() + "_" + t.getIntent(), t.getResultJson());
                }
            }
            return MAPPER.writeValueAsString(final_);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 会话管理
    // ─────────────────────────────────────────────────────────────────────────

    private AiFashionSessionDO getOrCreateSession(Long userId, String token) {
        if (StrUtil.isNotBlank(token)) {
            AiFashionSessionDO s = sessionMapper.selectByUserAndToken(userId, token);
            if (s != null) return s;
        }
        String newToken = IdUtil.fastSimpleUUID();
        AiFashionSessionDO s = new AiFashionSessionDO();
        s.setUserId(userId);
        s.setSessionToken(newToken);
        sessionMapper.insert(s);
        return s;
    }

    private AiFashionSessionContext buildContext(AiFashionSessionDO s) {
        return AiFashionSessionContext.builder()
                .lastPrompt(s.getLastPrompt())
                .currentColors(s.getCurrentColors())
                .currentStyle(s.getCurrentStyle())
                .currentFabric(s.getCurrentFabric())
                .currentLength(s.getCurrentLength())
                .currentFit(s.getCurrentFit())
                .lastTaskId(s.getLastTaskId())
                .build();
    }

    private void updateSession(AiFashionSessionDO session, AiFashionIntentParseResult parsed) {
        if (StrUtil.isNotBlank(parsed.getBasePrompt())) session.setLastPrompt(parsed.getBasePrompt());
        if (StrUtil.isNotBlank(parsed.getDetectedColor())) session.setCurrentColors(parsed.getDetectedColor());
        if (StrUtil.isNotBlank(parsed.getDetectedStyle())) session.setCurrentStyle(parsed.getDetectedStyle());
        if (StrUtil.isNotBlank(parsed.getDetectedFabric())) session.setCurrentFabric(parsed.getDetectedFabric());
        if (StrUtil.isNotBlank(parsed.getDetectedLength())) session.setCurrentLength(parsed.getDetectedLength());
        if (StrUtil.isNotBlank(parsed.getDetectedFit())) session.setCurrentFit(parsed.getDetectedFit());
        sessionMapper.updateById(session);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 响应构建
    // ─────────────────────────────────────────────────────────────────────────

    private AiFashionAgentChatRespVO buildChatResp(String chainId, AiFashionIntentChain chain,
                                                   List<AiFashionAgentTaskDO> agentTasks,
                                                   String sessionToken) {
        List<AiFashionAgentChatRespVO.StepSummary> stepList = agentTasks.stream()
                .map(t -> AiFashionAgentChatRespVO.StepSummary.builder()
                        .stepOrder(t.getStepOrder())
                        .intent(t.getIntent())
                        .desc(t.getIntentDesc())
                        .status(t.getStatus())
                        .progress(0)
                        .build())
                .collect(Collectors.toList());

        String reply = "好的！我已理解您的需求。" + chain.getChainDescription()
                + "。全程自动完成，进度通过SSE实时推送，无需任何手动操作。";

        return AiFashionAgentChatRespVO.builder()
                .chainId(chainId)
                .agentReply(reply)
                .sseUrl("/admin-api/ai/fashion/agent/progress/" + chainId)
                .steps(stepList)
                .sessionToken(sessionToken)
                .parsedIntentDesc(chain.getChainDescription())
                .totalProgress(0)
                .chainStatus("RUNNING")
                .build();
    }

    private AiFashionAgentChatRespVO buildQueryResp(String chainId,
                                                    List<AiFashionAgentTaskDO> tasks) {
        long done = tasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
        int pct = (int) (done * 100.0 / tasks.size());
        boolean allDone = done == tasks.size();
        boolean anyFail = tasks.stream().anyMatch(t -> "FAIL".equals(t.getStatus()));
        String status = allDone ? "SUCCESS" : anyFail ? "PARTIAL_FAIL" : "RUNNING";

        List<AiFashionAgentChatRespVO.StepSummary> stepList = tasks.stream()
                .map(t -> AiFashionAgentChatRespVO.StepSummary.builder()
                        .stepOrder(t.getStepOrder())
                        .intent(t.getIntent())
                        .desc(t.getIntentDesc())
                        .status(t.getStatus())
                        .progress(t.getProgress() != null ? t.getProgress() : 0)
                        .resultSummary(StrUtil.subPre(t.getResultJson(), 200))
                        .build())
                .collect(Collectors.toList());

        return AiFashionAgentChatRespVO.builder()
                .chainId(chainId)
                .chainStatus(status)
                .totalProgress(pct)
                .steps(stepList)
                .agentReply(allDone ? "✅ 链路全部完成！" : "⏳ 执行中… " + pct + "%")
                .build();
    }
}
