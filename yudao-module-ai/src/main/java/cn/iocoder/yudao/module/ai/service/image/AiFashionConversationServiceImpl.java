package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionSessionDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionSessionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * AI 服装设计智能对话服务实现类
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionConversationServiceImpl implements AiFashionConversationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private AiFashionNlpParserService nlpParser;

    @Resource
    private AiFashionSessionMapper sessionMapper;

    @Resource
    private AiFashionTaskService fashionTaskService;

    @Override
    public AiFashionSmartChatRespVO chat(Long userId, AiFashionSmartChatReqVO reqVO) {
        // 1. 获取或创建会话
        AiFashionSessionDO session = getOrCreateSession(userId, reqVO.getSessionToken());

        // 2. 构建 NLP 上下文
        AiFashionSessionContext context = AiFashionSessionContext.builder()
                .lastPrompt(session.getLastPrompt())
                .currentColors(session.getCurrentColors())
                .currentStyle(session.getCurrentStyle())
                .currentFabric(session.getCurrentFabric())
                .currentLength(session.getCurrentLength())
                .currentFit(session.getCurrentFit())
                .lastTaskId(session.getLastTaskId())
                .build();

        // 3. NLP 解析
        AiFashionIntentParseResult parsed = nlpParser.parse(reqVO.getMessage(), context);
        log.info("[SmartChat][userId={} session={}] 意图={} 批量={} prompt={}",
                userId, session.getSessionToken(), parsed.getIntent(), parsed.getBatchCount(), parsed.getBasePrompt());

        // 4. 创建任务
        List<Long> taskIds = createTasks(userId, reqVO, parsed);

        // 5. 更新会话
        updateSession(session, parsed, taskIds);

        // 6. 构建响应
        AiFashionSmartChatRespVO resp = new AiFashionSmartChatRespVO();
        resp.setSessionToken(session.getSessionToken());
        resp.setInterpretation(parsed.getInterpretation());
        resp.setIntent(parsed.getIntent().getCode());
        resp.setBatchCount(parsed.getBatchCount());
        resp.setDetectedColor(parsed.getDetectedColor());
        resp.setDetectedColorHex(parsed.getDetectedColorHex());
        resp.setDetectedStyle(parsed.getDetectedStyle());
        resp.setDetectedFabric(parsed.getDetectedFabric());
        resp.setModifications(parsed.getModifications());
        resp.setWorkflowMode(parsed.getWorkflowMode());
        resp.setQualityPreset(parsed.getQualityPreset());
        resp.setEnhancedPrompt(parsed.getBasePrompt());
        if (!taskIds.isEmpty()) {
            resp.setTaskId(taskIds.get(0));
            resp.setTaskIds(taskIds);
        }
        return resp;
    }

    @Override
    public AiFashionSessionDO getOrCreateSession(Long userId, String sessionToken) {
        // 如果有 token，先尝试查找
        if (StrUtil.isNotBlank(sessionToken)) {
            AiFashionSessionDO existing = sessionMapper.selectByUserAndToken(userId, sessionToken);
            if (existing != null) {
                return existing;
            }
        }
        // 创建新会话
        String newToken = StrUtil.isNotBlank(sessionToken) ? sessionToken : IdUtil.randomUUID();
        AiFashionSessionDO session = new AiFashionSessionDO()
                .setUserId(userId)
                .setSessionToken(newToken);
        sessionMapper.insert(session);
        return session;
    }

    // ==============================
    // 私有方法
    // ==============================

    /** 创建任务（批量时并行） */
    private List<Long> createTasks(Long userId, AiFashionSmartChatReqVO reqVO, AiFashionIntentParseResult parsed) {
        List<String> variantPrompts = parsed.getVariantPrompts();
        int count = variantPrompts.size();

        if (count <= 1) {
            Long taskId = createSingleTask(userId, reqVO, parsed, variantPrompts.get(0));
            return List.of(taskId);
        }

        // 并行创建多个任务，最多6个线程
        int poolSize = Math.min(count, 6);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Long> taskIds = new ArrayList<>();
        try {
            List<CompletableFuture<Long>> futures = new ArrayList<>();
            for (String prompt : variantPrompts) {
                futures.add(CompletableFuture.supplyAsync(
                        () -> createSingleTask(userId, reqVO, parsed, prompt),
                        executor));
            }
            for (CompletableFuture<Long> future : futures) {
                try {
                    taskIds.add(future.get(30, TimeUnit.SECONDS));
                } catch (Exception e) {
                    log.error("[SmartChat] 并行创建任务失败", e);
                }
            }
        } finally {
            executor.shutdown();
        }
        return taskIds;
    }

    /** 创建单个任务 */
    private Long createSingleTask(Long userId, AiFashionSmartChatReqVO reqVO,
                                   AiFashionIntentParseResult parsed, String prompt) {
        AiFashionTaskCreateReqVO createReqVO = new AiFashionTaskCreateReqVO();
        createReqVO.setPrompt(prompt);
        createReqVO.setNegativePrompt(AiFashionPromptEnhancer.defaultNegativePrompt());
        createReqVO.setWidth(reqVO.getWidth() != null ? reqVO.getWidth() : 768);
        createReqVO.setHeight(reqVO.getHeight() != null ? reqVO.getHeight() : 1024);
        createReqVO.setQualityPreset(reqVO.getQualityPreset() != null ? reqVO.getQualityPreset() : parsed.getQualityPreset());
        createReqVO.setWorkflowMode(parsed.getWorkflowMode());
        createReqVO.setSeed(-1L);
        return fashionTaskService.createTask(userId, createReqVO);
    }

    /** 更新会话上下文 */
    private void updateSession(AiFashionSessionDO session, AiFashionIntentParseResult parsed, List<Long> taskIds) {
        try {
            String variantTaskIdsJson = OBJECT_MAPPER.writeValueAsString(taskIds);
            sessionMapper.updateById(new AiFashionSessionDO()
                    .setId(session.getId())
                    .setLastTaskId(taskIds.isEmpty() ? null : taskIds.get(0))
                    .setLastPrompt(StrUtil.maxLength(parsed.getBasePrompt(), 2000))
                    .setCurrentColors(parsed.getDetectedColor())
                    .setCurrentStyle(parsed.getDetectedStyle())
                    .setCurrentFabric(parsed.getDetectedFabric())
                    .setVariantTaskIds(variantTaskIdsJson));
        } catch (JsonProcessingException e) {
            log.error("[SmartChat] 会话更新序列化失败", e);
        }
    }

}
