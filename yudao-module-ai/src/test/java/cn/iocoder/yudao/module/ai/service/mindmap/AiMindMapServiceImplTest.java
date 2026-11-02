package cn.iocoder.yudao.module.ai.service.mindmap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.mindmap.AiMindMapMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogServiceImpl;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AiMindMapServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiMindMapServiceImplTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long MODEL_ID = 10L;
    private static final String MODEL = "gpt-4o-mini";
    /**
     * 思维导图编号：mock insert 时分配
     */
    private static final Long MIND_MAP_ID = 100L;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @InjectMocks
    private AiMindMapServiceImpl mindMapService;

    @Mock
    private AiModelService modalService;
    @Mock
    private AiChatRoleService chatRoleService;
    @Mock
    private AiMindMapMapper mindMapMapper;
    /**
     * 请求日志：真实构建、更新快照，仅 mock 创建，不访问数据库
     */
    @Spy
    private AiRequestLogServiceImpl requestLogService;

    private ChatModel chatModel;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 模型：没有思维导图角色，使用默认模型
        when(chatRoleService.getChatRoleListByName(eq(AiChatRoleEnum.AI_MIND_MAP_ROLE.getName())))
                .thenReturn(Collections.emptyList());
        AiModelDO model = AiModelDO.builder().id(MODEL_ID).platform(AiPlatformEnum.OPENAI.getPlatform())
                .model(MODEL).type(AiModelTypeEnum.CHAT.getType()).build();
        when(modalService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(model);
        chatModel = mock(ChatModel.class);
        when(modalService.getChatModel(MODEL_ID)).thenReturn(chatModel);
        // mock 插入：分配思维导图编号
        doAnswer(invocation -> {
            AiMindMapDO mindMap = invocation.getArgument(0);
            mindMap.setId(MIND_MAP_ID);
            return 1;
        }).when(mindMapMapper).insert(any(AiMindMapDO.class));
        // mock 请求日志创建
        lenient().doNothing().when(requestLogService).createRequestLog(any(AiRequestLogCreateReqDTO.class));
    }

    @AfterEach
    public void after() {
        TenantContextHolder.clear();
    }

    @Test
    public void testGenerateMindMap_success() {
        // mock 方法：文本帧中途的用量，被最终无 generation 的用量帧覆盖；末尾 EmptyUsage 不能覆盖已有用量
        Flux<ChatResponse> stream = Flux.just(chunk("# Java", new EmptyUsage()),
                chunk("\n## 基础", new DefaultUsage(3, 1, 4)),
                usageChunk(new DefaultUsage(10, 5, 15)), usageChunk(new EmptyUsage()));

        // 调用
        List<CommonResult<AiMindMapGenerateRespVO>> results = generate(stream);
        // 断言：正文
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals("# Java\n## 基础", joinData(results));
        // 断言：每帧返回当前用量快照；仅用量的帧返回空内容；EmptyUsage 不覆盖已有用量，不逐帧累加
        assertFrame(results.get(0), "# Java", null, null, null);
        assertFrame(results.get(1), "\n## 基础", 3L, 1L, 4L);
        assertFrame(results.get(2), "", 10L, 5L, 15L);
        assertFrame(results.get(3), "", 10L, 5L, 15L);
        // 断言：请求日志与业务快照
        assertFinish(AiRequestLogStatusEnum.SUCCESS, "# Java\n## 基础", null, 10L, 5L, 15L);
    }

    @Test
    public void testGenerateMindMap_unknownAndRealZeroUsage() {
        // 调用：全程只有 EmptyUsage
        List<CommonResult<AiMindMapGenerateRespVO>> results = generate(Flux.just(chunk("unknown", new EmptyUsage()),
                usageChunk(new EmptyUsage())));
        // 断言：未知为 null，不补 0
        assertEquals("unknown", joinData(results));
        assertFrame(CollUtil.getLast(results), "", null, null, null);
        String unknownRequestId = assertFinish(AiRequestLogStatusEnum.SUCCESS, "unknown", null, null, null, null);

        // 清理调用记录，避免与第二次调用混淆
        clearInvocations(mindMapMapper, requestLogService);

        // 调用：真实 0 用量
        results = generate(Flux.just(chunk("zero", new DefaultUsage(0, 0, 0))));
        // 断言：真实 0 保留为 0
        assertEquals("zero", joinData(results));
        assertFrame(results.get(0), "zero", 0L, 0L, 0L);
        String zeroRequestId = assertFinish(AiRequestLogStatusEnum.SUCCESS, "zero", null, 0L, 0L, 0L);
        assertNotEquals(unknownRequestId, zeroRequestId);
    }

    @Test
    public void testGenerateMindMap_partialUsage() {
        // 调用：原生用量只返回输入，输出未知
        List<CommonResult<AiMindMapGenerateRespVO>> results = generate(Flux.just(chunk("partial",
                new DefaultUsage(9, 0, 9, MapUtil.of("prompt_tokens", 9)))));
        // 断言：只知道其中一项，合计仍未知，不按 0 补齐
        assertEquals("partial", joinData(results));
        assertFrame(results.get(0), "partial", 9L, null, null);
        assertFinish(AiRequestLogStatusEnum.SUCCESS, "partial", null, 9L, null, null);
    }

    @Test
    public void testGenerateMindMap_streamError() {
        // mock 方法：已收到有效用量后出错
        Flux<ChatResponse> stream = Flux.concat(Flux.just(chunk("partial", new DefaultUsage(7, 3, 10))),
                Flux.error(new RuntimeException("stream-error")));

        // 调用
        List<CommonResult<AiMindMapGenerateRespVO>> results = generate(stream);
        // 断言：错误流返回 error CommonResult
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertFrame(results.get(0), "partial", 7L, 3L, 10L);
        assertFalse(results.get(1).isSuccess());
        assertEquals(ErrorCodeConstants.WRITE_STREAM_ERROR.getCode(), results.get(1).getCode());
        // 断言：失败状态，保留已知用量
        assertFinish(AiRequestLogStatusEnum.FAIL, null, "stream-error", 7L, 3L, 10L);
    }

    @Test
    public void testGenerateMindMap_cancel() {
        // mock 方法：收到用量后流不结束，由订阅方取消
        when(chatModel.stream(any(Prompt.class))).thenReturn(
                Flux.concat(Flux.just(chunk("cancel", new DefaultUsage(4, 2, 6))), Flux.never()));

        // 调用
        List<CommonResult<AiMindMapGenerateRespVO>> results = mindMapService.generateMindMap(buildReqVO(), USER_ID)
                .take(1).collectList().block(TIMEOUT);
        // 断言：正文
        assertNotNull(results);
        assertEquals(1, results.size());
        assertFrame(results.get(0), "cancel", 4L, 2L, 6L);
        // 断言：取消状态，保留已知用量
        assertFinish(AiRequestLogStatusEnum.CANCEL, null, null, 4L, 2L, 6L);
    }

    @Test
    public void testGenerateMindMap_streamFactoryThrow() {
        // mock 方法：stream 工厂同步抛错
        when(chatModel.stream(any(Prompt.class))).thenThrow(new IllegalStateException("sync-error"));

        // 调用，并断言原异常抛出
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> mindMapService.generateMindMap(buildReqVO(), USER_ID));
        assertEquals("sync-error", ex.getMessage());
        // 断言：失败状态，用量未知
        assertFinish(AiRequestLogStatusEnum.FAIL, null, "sync-error", null, null, null);
    }

    @Test
    public void testGenerateMindMap_requestLogCreateFail() {
        // mock 方法：请求日志创建抛错
        doThrow(new RuntimeException("store-error")).when(requestLogService)
                .createRequestLog(any(AiRequestLogCreateReqDTO.class));

        // 调用
        List<CommonResult<AiMindMapGenerateRespVO>> results = generate(Flux.just(chunk("kept", new DefaultUsage(2, 1, 3))));
        // 断言：正文不受影响
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals("kept", joinData(results));
        assertFrame(results.get(0), "kept", 2L, 1L, 3L);
        // 断言：仍尝试创建一次日志，业务结果与用量照常更新
        assertFinish(AiRequestLogStatusEnum.SUCCESS, "kept", null, 2L, 1L, 3L);
    }

    // ========== 调用 ==========

    private List<CommonResult<AiMindMapGenerateRespVO>> generate(Flux<ChatResponse> stream) {
        when(chatModel.stream(any(Prompt.class))).thenReturn(stream);
        List<CommonResult<AiMindMapGenerateRespVO>> results = mindMapService.generateMindMap(buildReqVO(), USER_ID)
                .collectList().block(TIMEOUT);
        assertNotNull(results);
        return results;
    }

    private static AiMindMapGenerateReqVO buildReqVO() {
        AiMindMapGenerateReqVO reqVO = new AiMindMapGenerateReqVO();
        reqVO.setPrompt("Java 学习路线");
        return reqVO;
    }

    private static ChatResponse chunk(String text, Usage usage) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("ut-response").model(MODEL)
                .usage(usage).build();
        return new ChatResponse(Collections.singletonList(new Generation(new AssistantMessage(text))), metadata);
    }

    /**
     * 无 generation、仅携带用量的帧，例如 OpenAI 开启 stream_options.include_usage 后的最后一帧
     */
    private static ChatResponse usageChunk(Usage usage) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("ut-response").model(MODEL)
                .usage(usage).build();
        return new ChatResponse(Collections.emptyList(), metadata);
    }

    private static String joinData(List<CommonResult<AiMindMapGenerateRespVO>> results) {
        return results.stream().map(result -> result.getData().getContent()).collect(Collectors.joining());
    }

    // ========== 断言 ==========

    /**
     * 断言流式返回的一帧：业务编号、本次内容片段、当前用量快照
     */
    private static void assertFrame(CommonResult<AiMindMapGenerateRespVO> result, String content,
                                    Long inputTokens, Long outputTokens, Long totalTokens) {
        assertTrue(result.isSuccess());
        AiMindMapGenerateRespVO frame = result.getData();
        assertEquals(MIND_MAP_ID, frame.getId());
        assertEquals(content, frame.getContent());
        assertEquals(inputTokens, frame.getInputTokens());
        assertEquals(outputTokens, frame.getOutputTokens());
        assertEquals(totalTokens, frame.getTotalTokens());
    }

    /**
     * 断言插入、请求日志创建、业务更新各恰好一次，且日志与业务快照一致
     *
     * @return 本次请求编号
     */
    private String assertFinish(AiRequestLogStatusEnum status, String generatedContent, String errorMessage,
                                Long inputTokens, Long outputTokens, Long totalTokens) {
        // 断言：插入思维导图
        ArgumentCaptor<AiMindMapDO> insertCaptor = ArgumentCaptor.forClass(AiMindMapDO.class);
        verify(mindMapMapper).insert(insertCaptor.capture());
        AiMindMapDO mindMap = insertCaptor.getValue();
        assertEquals(MIND_MAP_ID, mindMap.getId());
        assertEquals(USER_ID, mindMap.getUserId());
        assertEquals("Java 学习路线", mindMap.getPrompt());
        assertEquals(AiPlatformEnum.OPENAI.getPlatform(), mindMap.getPlatform());
        assertEquals(MODEL_ID, mindMap.getModelId());
        assertEquals(MODEL, mindMap.getModel());
        assertEquals(36, StrUtil.length(mindMap.getRequestId()));

        // 断言：请求日志恰好创建一次
        ArgumentCaptor<AiRequestLogCreateReqDTO> requestLogCaptor = ArgumentCaptor.forClass(AiRequestLogCreateReqDTO.class);
        verify(requestLogService).createRequestLog(requestLogCaptor.capture());
        AiRequestLogCreateReqDTO requestLog = requestLogCaptor.getValue();
        assertEquals(mindMap.getRequestId(), requestLog.getRequestId());
        assertEquals(3, requestLog.getBizType().intValue()); // 思维导图
        assertEquals(MIND_MAP_ID, requestLog.getBizId());
        assertEquals(USER_ID, requestLog.getUserId());
        assertEquals(TENANT_ID, requestLog.getTenantId());
        assertEquals(AiPlatformEnum.OPENAI.getPlatform(), requestLog.getPlatform());
        assertEquals(MODEL_ID, requestLog.getModelId());
        assertEquals(MODEL, requestLog.getModel());
        assertEquals(status.getStatus(), requestLog.getStatus());
        assertEquals(errorMessage, requestLog.getErrorMessage());
        assertNotNull(requestLog.getBeginTime());
        assertEquals(inputTokens, requestLog.getInputTokens());
        assertEquals(outputTokens, requestLog.getOutputTokens());
        assertEquals(totalTokens, requestLog.getTotalTokens());

        // 断言：业务结果恰好更新一次，用量与日志一致
        ArgumentCaptor<AiMindMapDO> updateCaptor = ArgumentCaptor.forClass(AiMindMapDO.class);
        verify(mindMapMapper).updateById(updateCaptor.capture());
        AiMindMapDO updateObj = updateCaptor.getValue();
        assertEquals(MIND_MAP_ID, updateObj.getId());
        assertEquals(generatedContent, updateObj.getGeneratedContent());
        assertEquals(errorMessage, updateObj.getErrorMessage());
        assertEquals(inputTokens, updateObj.getInputTokens());
        assertEquals(outputTokens, updateObj.getOutputTokens());
        assertEquals(totalTokens, updateObj.getTotalTokens());
        assertEquals(USER_ID.toString(), updateObj.getUpdater());
        return mindMap.getRequestId();
    }

}
