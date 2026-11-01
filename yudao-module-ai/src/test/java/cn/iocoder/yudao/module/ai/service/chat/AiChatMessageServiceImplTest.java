package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiModelInputModalityEnum;
import org.mockito.MockedStatic;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import java.util.ArrayList;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiToolDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.model.AiToolService;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletion;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionChunk;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionFinishReason;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionMessage;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionMessage.ChatCompletionFunction;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionMessage.Role;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionMessage.ToolCall;
import org.springframework.ai.deepseek.api.DeepSeekApi.ChatCompletionRequest;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_MESSAGE_IMAGE_READ_FAIL;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_STREAM_ERROR;
import static cn.iocoder.yudao.module.ai.util.AiUtils.TOOL_CONTEXT_LOGIN_USER;
import static cn.iocoder.yudao.module.ai.util.AiUtils.TOOL_CONTEXT_TENANT_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AiChatMessageServiceImpl} 的单元测试类
 *
 * 使用真实的 {@link DeepSeekChatModel} 和工具调用，仅 mock 底层的 {@link DeepSeekApi}
 *
 * @author 芋道源码
 */
public class AiChatMessageServiceImplTest extends BaseMockitoUnitTest {

    private static final String MODEL_NAME = "deepseek-test";
    private static final Double TEMPERATURE = 0.5;
    private static final Integer MAX_TOKENS = 1024;

    private static final String TOOL_NAME = "query_weather";
    private static final String TOOL_RESULT = "杭州：晴，25℃";
    private static final ChatCompletionMessage TOOL_CALL_MESSAGE = new ChatCompletionMessage("我来查询一下杭州的天气",
            Role.ASSISTANT, null, null, singletonList(new ToolCall("call_1", "function",
                    new ChatCompletionFunction(TOOL_NAME, "{\"city\":\"杭州\"}"))));

    private static final List<String> TOOL_ANSWER_CHUNKS = asList("杭州今天", "晴，25℃");
    private static final String TOOL_ANSWER = String.join("", TOOL_ANSWER_CHUNKS);
    private static final List<String> PLAIN_ANSWER_CHUNKS = asList("你好，", "我是 AI 助手");
    private static final String PLAIN_ANSWER = String.join("", PLAIN_ANSWER_CHUNKS);

    /**
     * assistant 消息编号：先插入 user 消息（编号 1），再插入 assistant 消息（编号 2）
     */
    private static final Long ASSISTANT_MESSAGE_ID = 2L;

    @InjectMocks
    private AiChatMessageServiceImpl chatMessageService;

    @Mock
    private AiChatMessageMapper chatMessageMapper;
    @Mock
    private AiChatConversationService chatConversationService;
    @Mock
    private AiChatRoleService chatRoleService;
    @Mock
    private AiModelService modalService;
    @Mock
    private AiKnowledgeDocumentService knowledgeDocumentService;
    @Mock
    private AiToolService toolService;
    @Mock
    private ToolCallbackResolver toolCallbackResolver;
    @Mock
    private DeepSeekApi deepSeekApi;

    @AfterEach
    public void after() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    public void testSendMessage_toolCall() {
        // 准备参数
        Long userId = randomLongId();
        Long roleId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("杭州天气怎么样？");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, roleId);
        // mock 工具
        List<ToolContext> toolContexts = new CopyOnWriteArrayList<>();
        mockWeatherTool(roleId, toolContexts);
        // mock 登录用户、租户
        Long tenantId = randomLongId();
        mockLoginUser(userId, tenantId);
        // mock deepSeekApi 的方法：先请求工具，再返回回答
        when(deepSeekApi.chatCompletionEntity(any())).thenReturn(buildChatCompletion(TOOL_CALL_MESSAGE),
                buildChatCompletion(new ChatCompletionMessage(TOOL_ANSWER, Role.ASSISTANT)));

        // 调用
        AiChatMessageSendRespVO respVO = chatMessageService.sendMessage(reqVO, userId);
        // 断言
        assertEquals(TOOL_ANSWER, respVO.getReceive().getContent());
        ArgumentCaptor<ChatCompletionRequest> requestCaptor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(deepSeekApi, times(2)).chatCompletionEntity(requestCaptor.capture());
        assertToolExecuted(requestCaptor.getAllValues(), toolContexts, userId, tenantId);
        // 断言模型参数
        ChatCompletionRequest request = requestCaptor.getAllValues().get(0);
        assertEquals(MODEL_NAME, request.model());
        assertEquals(TEMPERATURE, request.temperature());
        assertEquals(MAX_TOKENS, request.maxTokens());
        // 断言消息保存
        assertAssistantMessageUpdated(TOOL_ANSWER);
    }

    @Test
    public void testSendMessage_withoutTool() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("你好");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, null);
        // mock deepSeekApi 的方法
        when(deepSeekApi.chatCompletionEntity(any()))
                .thenReturn(buildChatCompletion(new ChatCompletionMessage(PLAIN_ANSWER, Role.ASSISTANT)));

        // 调用
        AiChatMessageSendRespVO respVO = chatMessageService.sendMessage(reqVO, userId);
        // 断言
        assertEquals(PLAIN_ANSWER, respVO.getReceive().getContent());
        verify(deepSeekApi).chatCompletionEntity(any());
        // 断言消息保存
        assertAssistantMessageUpdated(PLAIN_ANSWER);
    }

    @Test
    public void testSendMessage_toolCallThenModelError() {
        // 准备参数
        Long userId = randomLongId();
        Long roleId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("杭州天气怎么样？");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, roleId);
        // mock 工具
        List<ToolContext> toolContexts = new CopyOnWriteArrayList<>();
        mockWeatherTool(roleId, toolContexts);
        // mock 登录用户、租户
        mockLoginUser(userId, randomLongId());
        // mock deepSeekApi 的方法：先请求工具，再抛出异常
        RuntimeException modelException = new IllegalStateException("模型服务异常");
        when(deepSeekApi.chatCompletionEntity(any())).thenReturn(buildChatCompletion(TOOL_CALL_MESSAGE))
                .thenThrow(modelException);

        // 调用，并断言异常
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatMessageService.sendMessage(reqVO, userId));
        assertSame(modelException, exception);
        // 断言工具已执行、消息未保存
        verify(deepSeekApi, times(2)).chatCompletionEntity(any());
        assertEquals(1, toolContexts.size());
        verify(chatMessageMapper, never()).updateById(any(AiChatMessageDO.class));
    }

    @Test
    public void testSendChatMessageStream_toolCall() {
        // 准备参数
        Long userId = randomLongId();
        Long roleId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("杭州天气怎么样？");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, roleId);
        // mock 工具
        List<ToolContext> toolContexts = new CopyOnWriteArrayList<>();
        mockWeatherTool(roleId, toolContexts);
        // mock 登录用户、租户
        Long tenantId = randomLongId();
        mockLoginUser(userId, tenantId);
        // mock deepSeekApi 的方法：先请求工具，再流式返回回答
        when(deepSeekApi.chatCompletionStream(any())).thenReturn(Flux.just(buildChatCompletionChunk(TOOL_CALL_MESSAGE)),
                buildAnswerChunks(TOOL_ANSWER_CHUNKS));

        // 调用
        List<CommonResult<AiChatMessageSendRespVO>> results = chatMessageService
                .sendChatMessageStream(reqVO, userId).collectList().block();
        // 断言（工具调用轮次不下发）
        assertNotNull(results);
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals(TOOL_ANSWER, joinContent(results));
        ArgumentCaptor<ChatCompletionRequest> requestCaptor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(deepSeekApi, times(2)).chatCompletionStream(requestCaptor.capture());
        assertToolExecuted(requestCaptor.getAllValues(), toolContexts, userId, tenantId);
        // 断言消息保存
        assertAssistantMessageUpdated(TOOL_ANSWER);
    }

    @Test
    public void testSendChatMessageStream_withoutTool() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("你好");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, null);
        // mock deepSeekApi 的方法
        when(deepSeekApi.chatCompletionStream(any())).thenReturn(buildAnswerChunks(PLAIN_ANSWER_CHUNKS));

        // 调用
        List<CommonResult<AiChatMessageSendRespVO>> results = chatMessageService
                .sendChatMessageStream(reqVO, userId).collectList().block();
        // 断言
        assertNotNull(results);
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        assertEquals(PLAIN_ANSWER, joinContent(results));
        verify(deepSeekApi).chatCompletionStream(any());
        // 断言消息保存
        assertAssistantMessageUpdated(PLAIN_ANSWER);
    }

    @Test
    public void testSendChatMessageStream_toolCallThenModelError() {
        // 准备参数
        Long userId = randomLongId();
        Long roleId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("杭州天气怎么样？");
        // mock 对话、模型
        mockConversation(reqVO.getConversationId(), userId, roleId);
        // mock 工具
        List<ToolContext> toolContexts = new CopyOnWriteArrayList<>();
        mockWeatherTool(roleId, toolContexts);
        // mock 登录用户、租户
        mockLoginUser(userId, randomLongId());
        // mock deepSeekApi 的方法：先请求工具，再返回异常
        when(deepSeekApi.chatCompletionStream(any())).thenReturn(Flux.just(buildChatCompletionChunk(TOOL_CALL_MESSAGE)),
                Flux.error(new IllegalStateException("模型服务异常")));

        // 调用
        List<CommonResult<AiChatMessageSendRespVO>> results = chatMessageService
                .sendChatMessageStream(reqVO, userId).collectList().block();
        // 断言
        assertNotNull(results);
        assertEquals(CHAT_STREAM_ERROR.getCode(), CollUtil.getLast(results).getCode());
        verify(deepSeekApi, times(2)).chatCompletionStream(any());
        assertEquals(1, toolContexts.size());
        // 断言消息删除（无内容）
        verify(chatMessageMapper).deleteById(ASSISTANT_MESSAGE_ID);
        verify(chatMessageMapper, never()).updateById(any(AiChatMessageDO.class));
    }

    private void mockConversation(Long conversationId, Long userId, Long roleId) {
        // mock 对话
        AiChatConversationDO conversation = new AiChatConversationDO().setId(conversationId).setUserId(userId)
                .setRoleId(roleId).setModelId(randomLongId()).setTemperature(TEMPERATURE).setMaxTokens(MAX_TOKENS);
        when(chatConversationService.validateChatConversationExists(eq(conversationId))).thenReturn(conversation);
        // mock 模型：真实的 DeepSeekChatModel
        AiModelDO model = new AiModelDO().setId(conversation.getModelId()).setModel(MODEL_NAME)
                .setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform());
        when(modalService.validateModel(eq(model.getId()))).thenReturn(model);
        when(modalService.getChatModel(eq(model.getId())))
                .thenReturn(DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build());
        // mock 消息插入：依次分配编号
        AtomicLong messageId = new AtomicLong();
        doAnswer(invocation -> {
            invocation.<AiChatMessageDO>getArgument(0).setId(messageId.incrementAndGet());
            return 1;
        }).when(chatMessageMapper).insert(any(AiChatMessageDO.class));
    }

    private void mockWeatherTool(Long roleId, List<ToolContext> toolContexts) {
        // mock 工具：真实的 FunctionToolCallback
        Long toolId = randomLongId();
        ToolCallback toolCallback = FunctionToolCallback.<WeatherRequest, String>builder(TOOL_NAME,
                (request, toolContext) -> {
                    toolContexts.add(toolContext);
                    return request.getCity() + "：晴，25℃";
                }).description("查询城市天气").inputType(WeatherRequest.class).build();
        when(chatRoleService.getChatRole(eq(roleId))).thenReturn(new AiChatRoleDO().setId(roleId)
                .setToolIds(singletonList(toolId)));
        when(toolService.getToolList(eq(singletonList(toolId))))
                .thenReturn(singletonList(new AiToolDO().setId(toolId).setName(TOOL_NAME)));
        when(toolCallbackResolver.resolve(eq(TOOL_NAME))).thenReturn(toolCallback);
    }

    private static void mockLoginUser(Long userId, Long tenantId) {
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        TenantContextHolder.setTenantId(tenantId);
    }

    private static void assertToolExecuted(List<ChatCompletionRequest> requests, List<ToolContext> toolContexts,
                                           Long userId, Long tenantId) {
        // 断言首轮请求带上工具
        assertEquals(1, requests.get(0).tools().size());
        // 断言工具执行，并拿到登录用户、租户
        assertEquals(1, toolContexts.size());
        Map<String, Object> context = toolContexts.get(0).getContext();
        assertEquals(userId, ((LoginUser) context.get(TOOL_CONTEXT_LOGIN_USER)).getId());
        assertEquals(tenantId, context.get(TOOL_CONTEXT_TENANT_ID));
        // 断言次轮请求回传工具结果
        ChatCompletionMessage toolMessage = CollUtil.getLast(requests.get(1).messages());
        assertEquals(Role.TOOL, toolMessage.role());
        assertTrue(toolMessage.content().contains(TOOL_RESULT));
    }

    private void assertAssistantMessageUpdated(String content) {
        ArgumentCaptor<AiChatMessageDO> messageCaptor = ArgumentCaptor.forClass(AiChatMessageDO.class);
        verify(chatMessageMapper).updateById(messageCaptor.capture());
        assertEquals(ASSISTANT_MESSAGE_ID, messageCaptor.getValue().getId());
        assertEquals(content, messageCaptor.getValue().getContent());
    }

    private static String joinContent(List<CommonResult<AiChatMessageSendRespVO>> results) {
        return results.stream().map(result -> result.getData().getReceive().getContent())
                .collect(Collectors.joining());
    }

    private static ResponseEntity<ChatCompletion> buildChatCompletion(ChatCompletionMessage message) {
        ChatCompletion.Choice choice = new ChatCompletion.Choice(buildFinishReason(message), 0, message, null);
        return ResponseEntity.ok(new ChatCompletion("chatcmpl-1", singletonList(choice), null, MODEL_NAME,
                null, null, null));
    }

    private static Flux<ChatCompletionChunk> buildAnswerChunks(List<String> contents) {
        return Flux.fromIterable(contents)
                .map(content -> buildChatCompletionChunk(new ChatCompletionMessage(content, Role.ASSISTANT)));
    }

    private static ChatCompletionChunk buildChatCompletionChunk(ChatCompletionMessage delta) {
        ChatCompletionChunk.ChunkChoice choice = new ChatCompletionChunk.ChunkChoice(buildFinishReason(delta),
                0, delta, null);
        return new ChatCompletionChunk("chatcmpl-1", singletonList(choice), null, MODEL_NAME,
                null, null, null, null);
    }

    private static ChatCompletionFinishReason buildFinishReason(ChatCompletionMessage message) {
        return CollUtil.isNotEmpty(message.toolCalls()) ? ChatCompletionFinishReason.TOOL_CALLS
                : ChatCompletionFinishReason.STOP;
    }

    // ========== 附件多模态：模型配置 image 的图片走 Media，其它附件走原 Attachment 文本 ==========

    /**
     * 自定义型号：不在任何已知名单里，是否传图只看模型配置的 inputModalities
     */
    private static final String CUSTOM_MODEL_NAME = "my-vlm-x";
    private static final String IMAGE_URL = "https://www.iocoder.cn/a.png?X-Amz-Signature=xxx#v1";
    private static final String OLD_IMAGE_URL = "https://www.iocoder.cn/old.png";
    private static final String PDF_URL = "https://www.iocoder.cn/b.pdf";
    private static final String VIDEO_URL = "https://www.iocoder.cn/c.mp4";
    private static final byte[] IMAGE_BYTES = new byte[]{1, 2, 3};
    private static final List<String> TEXT_IMAGE = asList(AiModelInputModalityEnum.TEXT.getModality(),
            AiModelInputModalityEnum.IMAGE.getModality());

    @Mock
    private ChatModel chatModel;

    @Test
    public void testSendMessage_imageWithImageModality() {
        // 准备参数：签名 URL 的图片 + pdf 文档
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setAttachmentUrls(asList(IMAGE_URL, PDF_URL));
        // mock 对话、模型：自定义型号配置了 image
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);
        // mock 方法
        when(knowledgeDocumentService.readUrl(eq(PDF_URL))).thenReturn("文档内容");
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL))).thenReturn(IMAGE_BYTES);
            // 调用
            chatMessageService.sendMessage(reqVO, userId);
        }
        // 断言：图片和问题在同一条 UserMessage 中，以 Media 传输
        Prompt prompt = captureCallPrompt();
        UserMessage currentMessage = findUserMessage(prompt, reqVO.getContent());
        assertEquals(1, currentMessage.getMedia().size());
        Media media = CollUtil.getFirst(currentMessage.getMedia());
        assertEquals("image/png", media.getMimeType().toString());
        assertArrayEquals(IMAGE_BYTES, (byte[]) media.getData());
        // 断言：文档走 Attachment 文本，图片不再拼进文本
        UserMessage attachmentMessage = findUserMessage(prompt, "<Attachment");
        assertTrue(attachmentMessage.getText().contains("文档内容"));
        assertFalse(attachmentMessage.getText().contains("a.png"));
        verify(knowledgeDocumentService, never()).readUrl(eq(IMAGE_URL));
    }

    @Test
    public void testSendMessage_imageWithoutImageModality() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setAttachmentUrls(singletonList(IMAGE_URL));
        // mock 对话、模型：未配置 image
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK,
                singletonList(AiModelInputModalityEnum.TEXT.getModality()));
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL))).thenReturn(IMAGE_BYTES);
            // 调用：不拒绝
            chatMessageService.sendMessage(reqVO, userId);
        }
        // 断言：没有 Media，图片沿用原逻辑，以 Base64 拼进 Attachment 文本
        Prompt prompt = captureCallPrompt();
        assertNoMedia(prompt);
        UserMessage attachmentMessage = findUserMessage(prompt, "<Attachment");
        assertTrue(attachmentMessage.getText().contains("name=\"a.png\""));
        assertTrue(attachmentMessage.getText().contains(Base64.encode(IMAGE_BYTES)));
    }

    @Test
    public void testSendMessage_historyImage() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setUseContext(true);
        // mock 对话、模型：maxContexts = 1，只选入最近一组
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);
        when(chatMessageMapper.selectListByConversationId(eq(reqVO.getConversationId())))
                .thenReturn(buildHistoryMessages());
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL))).thenReturn(IMAGE_BYTES);
            // 调用
            chatMessageService.sendMessage(reqVO, userId);
            // 断言：未选入上下文的历史图片不下载
            httpUtilMock.verify(() -> HttpUtil.downloadBytes(eq(OLD_IMAGE_URL)), never());
        }
        // 断言：历史图片挂在它所属的 user 消息上；当前消息没有图片
        Prompt prompt = captureCallPrompt();
        assertEquals(1, findUserMessage(prompt, "上一问").getMedia().size());
        assertTrue(CollUtil.isEmpty(findUserMessage(prompt, reqVO.getContent()).getMedia()));
        assertNull(findUserMessageOrNull(prompt, "旧问题"));
    }

    @Test
    public void testSendMessage_historyImageWithoutContext() {
        // 准备参数：不使用上下文
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setUseContext(false);
        // mock 对话、模型
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);
        when(chatMessageMapper.selectListByConversationId(eq(reqVO.getConversationId())))
                .thenReturn(buildHistoryMessages());
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            // 调用
            chatMessageService.sendMessage(reqVO, userId);
            // 断言：历史图片不下载
            httpUtilMock.verify(() -> HttpUtil.downloadBytes(any(String.class)), never());
        }
        // 断言：没有 Media，也没有历史消息
        Prompt prompt = captureCallPrompt();
        assertNoMedia(prompt);
        assertNull(findUserMessageOrNull(prompt, "上一问"));
    }

    @Test
    public void testSendMessage_imageReadFail() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setAttachmentUrls(singletonList(IMAGE_URL));
        // mock 对话、模型：配置了 image
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL)))
                    .thenThrow(new IllegalStateException("下载失败"));
            // 调用，并断言异常
            assertServiceException(() -> chatMessageService.sendMessage(reqVO, userId),
                    CHAT_MESSAGE_IMAGE_READ_FAIL, "a.png");
        }
        // 断言：不调用模型、不写消息
        verify(chatModel, never()).call(any(Prompt.class));
        verify(chatMessageMapper, never()).insert(any(AiChatMessageDO.class));
    }

    @Test
    public void testSendChatMessageStream_imageReadFail() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setAttachmentUrls(singletonList(IMAGE_URL));
        // mock 对话、模型：配置了 image
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);

        List<CommonResult<AiChatMessageSendRespVO>> results;
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL)))
                    .thenThrow(new IllegalStateException("下载失败"));
            // 调用
            results = chatMessageService.sendChatMessageStream(reqVO, userId).collectList().block();
        }
        // 断言：返回错误帧，不调用模型、不写消息
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(CHAT_MESSAGE_IMAGE_READ_FAIL.getCode(), results.get(0).getCode());
        verify(chatModel, never()).stream(any(Prompt.class));
        verify(chatMessageMapper, never()).insert(any(AiChatMessageDO.class));
    }

    @Test
    public void testSendChatMessageStream_imageWithImageModality() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setAttachmentUrls(singletonList(IMAGE_URL));
        // mock 对话、模型：配置了 image
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);
        // mock 方法
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(buildChatResponse()));

        List<CommonResult<AiChatMessageSendRespVO>> results;
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL))).thenReturn(IMAGE_BYTES);
            // 调用
            results = chatMessageService.sendChatMessageStream(reqVO, userId).collectList().block();
        }
        // 断言：正常返回，图片以 Media 传输
        assertNotNull(results);
        assertTrue(CollUtil.getLast(results).isSuccess());
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).stream(promptCaptor.capture());
        assertEquals(1, findUserMessage(promptCaptor.getValue(), reqVO.getContent()).getMedia().size());
    }

    @Test
    public void testSendMessage_video() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("视频里有什么？").setAttachmentUrls(singletonList(VIDEO_URL));
        // mock 对话、模型：即使配置了 video，本期也不实现视频传输
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK,
                asList(AiModelInputModalityEnum.TEXT.getModality(), AiModelInputModalityEnum.IMAGE.getModality(),
                        AiModelInputModalityEnum.VIDEO.getModality()));
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            // 调用：不拒绝
            chatMessageService.sendMessage(reqVO, userId);
            // 断言：视频不当作图片下载
            httpUtilMock.verify(() -> HttpUtil.downloadBytes(any(String.class)), never());
        }
        // 断言：没有 Media，视频沿用原 Attachment 逻辑
        assertNoMedia(captureCallPrompt());
        verify(knowledgeDocumentService).readUrl(eq(VIDEO_URL));
    }

    @Test
    public void testSendMessage_textOnly() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("你好");
        // mock 对话、模型
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.DEEP_SEEK, TEXT_IMAGE);
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        // 调用
        chatMessageService.sendMessage(reqVO, userId);
        // 断言：纯文本，没有 Media
        Prompt prompt = captureCallPrompt();
        assertNoMedia(prompt);
        assertEquals(reqVO.getContent(), findUserMessage(prompt, reqVO.getContent()).getText());
    }

    @Test
    public void testSendMessage_tongYiMultiModelByMedia() {
        // 准备参数：当前消息纯文本，历史消息带图
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("这是我的车？").setUseContext(true);
        // mock 对话、模型：通义的自定义型号，不在 TONG_YI_MULTI_MODELS 名单
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.TONG_YI, TEXT_IMAGE);
        when(chatMessageMapper.selectListByConversationId(eq(reqVO.getConversationId())))
                .thenReturn(buildHistoryMessages());
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(eq(IMAGE_URL))).thenReturn(IMAGE_BYTES);
            // 调用
            chatMessageService.sendMessage(reqVO, userId);
        }
        // 断言：Prompt 含历史 Media，走多模态接口
        DashScopeChatOptions options = (DashScopeChatOptions) captureCallPrompt().getOptions();
        assertEquals(Boolean.TRUE, options.getMultiModel());
    }

    @Test
    public void testSendMessage_tongYiTextOnly() {
        // 准备参数
        Long userId = randomLongId();
        AiChatMessageSendReqVO reqVO = new AiChatMessageSendReqVO().setConversationId(randomLongId())
                .setContent("你好");
        // mock 对话、模型：通义的自定义型号
        mockMediaConversation(reqVO.getConversationId(), userId, AiPlatformEnum.TONG_YI, TEXT_IMAGE);
        // mock 方法
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse());

        // 调用
        chatMessageService.sendMessage(reqVO, userId);
        // 断言：纯文本保持原通道
        DashScopeChatOptions options = (DashScopeChatOptions) captureCallPrompt().getOptions();
        assertEquals(Boolean.FALSE, options.getMultiModel());
    }


    private void mockMediaConversation(Long conversationId, Long userId, AiPlatformEnum platform,
                                       List<String> inputModalities) {
        // mock 对话
        AiChatConversationDO conversation = new AiChatConversationDO().setId(conversationId).setUserId(userId)
                .setModelId(randomLongId()).setTemperature(TEMPERATURE).setMaxTokens(MAX_TOKENS).setMaxContexts(1);
        when(chatConversationService.validateChatConversationExists(eq(conversationId))).thenReturn(conversation);
        // mock 模型：mock 的 ChatModel，用于捕获 Prompt
        AiModelDO model = new AiModelDO().setId(conversation.getModelId()).setName("自定义视觉模型")
                .setModel(CUSTOM_MODEL_NAME).setPlatform(platform.getPlatform()).setInputModalities(inputModalities);
        when(modalService.validateModel(eq(model.getId()))).thenReturn(model);
        when(modalService.getChatModel(eq(model.getId()))).thenReturn(chatModel);
    }

    /**
     * 构建历史消息：两组 user + assistant，各带一张图片
     */
    private static List<AiChatMessageDO> buildHistoryMessages() {
        List<AiChatMessageDO> messages = new ArrayList<>();
        messages.add(new AiChatMessageDO().setId(11L).setType(MessageType.USER.getValue())
                .setContent("旧问题").setAttachmentUrls(singletonList(OLD_IMAGE_URL)));
        messages.add(new AiChatMessageDO().setId(12L).setReplyId(11L).setType(MessageType.ASSISTANT.getValue())
                .setContent("旧回答"));
        messages.add(new AiChatMessageDO().setId(13L).setType(MessageType.USER.getValue())
                .setContent("上一问").setAttachmentUrls(singletonList(IMAGE_URL)));
        messages.add(new AiChatMessageDO().setId(14L).setReplyId(13L).setType(MessageType.ASSISTANT.getValue())
                .setContent("上一答"));
        return messages;
    }

    private Prompt captureCallPrompt() {
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        return promptCaptor.getValue();
    }

    private static UserMessage findUserMessage(Prompt prompt, String text) {
        UserMessage message = findUserMessageOrNull(prompt, text);
        assertNotNull(message, "未找到包含(" + text + ")的 UserMessage");
        return message;
    }

    private static UserMessage findUserMessageOrNull(Prompt prompt, String text) {
        for (Message message : prompt.getInstructions()) {
            if (message instanceof UserMessage && StrUtil.contains(message.getText(), text)) {
                return (UserMessage) message;
            }
        }
        return null;
    }

    private static void assertNoMedia(Prompt prompt) {
        for (Message message : prompt.getInstructions()) {
            if (message instanceof UserMessage) {
                assertTrue(CollUtil.isEmpty(((UserMessage) message).getMedia()));
            }
        }
    }

    private static ChatResponse buildChatResponse() {
        return new ChatResponse(singletonList(new Generation(new AssistantMessage("好的"))));
    }

    @Data
    public static class WeatherRequest {

        private String city;

    }

}
