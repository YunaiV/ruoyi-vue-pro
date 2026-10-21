package cn.iocoder.yudao.module.ai.service.chat;

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

    @Data
    public static class WeatherRequest {

        private String city;

    }

}
