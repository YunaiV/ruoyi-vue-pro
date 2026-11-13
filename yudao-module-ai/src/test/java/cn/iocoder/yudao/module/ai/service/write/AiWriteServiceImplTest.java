package cn.iocoder.yudao.module.ai.service.write;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.requestlog.AiRequestLogMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

/**
 * {@link AiWriteServiceImpl} 的单元测试类
 *
 * 写作记录、请求日志使用真实 Mapper（H2），只 mock 模型
 *
 * @author 芋道源码
 */
@Import({AiWriteServiceImpl.class, AiRequestLogServiceImpl.class})
public class AiWriteServiceImplTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long MODEL_ID = 10L;
    private static final String MODEL = "gpt-4o-mini";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 字典标签：与现库 ai_write_language / ai_write_tone 的值一致
     */
    private static final Map<String, Map<Integer, String>> DICT_LABELS = Map.of(
            DictTypeConstants.AI_WRITE_LANGUAGE, Map.of(1, "自动", 2, "中文", 3, "英文", 4, "韩语", 5, "日语"),
            DictTypeConstants.AI_WRITE_TONE, Map.of(1, "自动", 5, "专业"),
            DictTypeConstants.AI_WRITE_FORMAT, Map.of(1, "自动"),
            DictTypeConstants.AI_WRITE_LENGTH, Map.of(1, "自动"));

    @Resource
    private AiWriteServiceImpl writeService;

    @Resource
    private AiWriteMapper writeMapper;
    @Resource
    private AiRequestLogMapper requestLogMapper;

    @MockitoBean
    private AiModelService modalService;

    private ChatModel chatModel;
    private MockedStatic<DictFrameworkUtils> dictMock;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
        AiModelDO model = AiModelDO.builder().id(MODEL_ID).platform(AiPlatformEnum.OPENAI.getPlatform())
                .model(MODEL).type(AiModelTypeEnum.CHAT.getType()).build();
        when(modalService.validateModel(MODEL_ID)).thenReturn(model);
        chatModel = mock(ChatModel.class);
        when(modalService.getChatModel(MODEL_ID)).thenReturn(chatModel);
        // 字典翻译：不依赖字典模块
        dictMock = mockStatic(DictFrameworkUtils.class);
        dictMock.when(() -> DictFrameworkUtils.parseDictDataLabel(anyString(), nullable(Integer.class)))
                .thenAnswer(invocation -> {
                    Integer value = invocation.getArgument(1);
                    return value == null ? null : DICT_LABELS.get(invocation.<String>getArgument(0)).get(value);
                });
    }

    @AfterEach
    public void after() {
        dictMock.close();
        TenantContextHolder.clear();
    }

    @Test
    public void testGenerate_writing() {
        // 调用：文章写作，原文传了也不保存
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.WRITING).setPrompt("田忌赛马").setOriginalContent("多余");
        assertValid(reqVO);
        List<CommonResult<AiWriteGenerateRespVO>> results = generate(reqVO);

        // 断言：返回内容
        assertEquals(1, results.size());
        assertEquals("正文", results.get(0).getData().getContent());
        assertEquals(2L, results.get(0).getData().getTotalTokens());
        // 断言：写作记录、请求日志
        AiWriteDO write = assertWrite(AiWriteTypeEnum.WRITING, "正文", null);
        assertEquals("田忌赛马", write.getPrompt());
        assertNull(write.getOriginalContent());
        assertNull(write.getSceneParams());
        assertEquals(1, write.getFormat());
        assertEquals(2L, write.getTotalTokens());
        assertRequestLog(write, AiRequestLogStatusEnum.SUCCESS, null);
        // 断言：系统提示词沿用“写作助手”的内置文本
        List<Message> messages = captureMessages();
        assertEquals(AiChatRoleEnum.AI_WRITE_ROLE.getSystemMessage(), messages.get(0).getText());
        assertEquals("请撰写一篇关于 [田忌赛马] 的文章。文章的内容格式：自动，语气：自动，语言：自动，长度：自动。"
                + "请确保涵盖主要内容，不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。", messages.get(1).getText());
        verify(modalService, never()).getRequiredDefaultModel(any());
    }

    @Test
    public void testGenerate_translate() {
        // 调用：翻译；输出语言、格式、长度、其它场景参数传了也丢弃
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.TRANSLATE).setOriginalContent("你好\n- 列表")
                .setSceneParams(translateParams(1, 3).setAudience("多余"));
        assertValid(reqVO);
        generate(reqVO);

        // 断言：只保存翻译的键（从数据库读回 scene_params）
        AiWriteDO write = assertWrite(AiWriteTypeEnum.TRANSLATE, "正文", null);
        assertEquals("", write.getPrompt());
        assertNull(write.getLanguage());
        assertNull(write.getFormat());
        assertNull(write.getLength());
        assertEquals(1, write.getTone());
        assertEquals(new AiWriteDO.SceneParams().setSourceLanguage(1).setTargetLanguage(3).setDomain("技术")
                .setPreserveFormat(true), write.getSceneParams());
        // 断言：提示词
        List<Message> messages = captureMessages();
        assertInstanceOf(SystemMessage.class, messages.get(0));
        assertEquals(AiWriteTypeEnum.TRANSLATE.getSystemMessage(), messages.get(0).getText());
        assertEquals("源语言：自动；目标语言：英文；领域：技术；语气：自动；保留格式（换行、列表、Markdown、代码、链接）：是。"
                + "术语 / 补充说明：[无]。原文：[你好\n- 列表]", messages.get(1).getText());
        // 断言：管理后台查询时，场景参数转换为 Response VO
        AiWriteRespVO respVO = BeanUtils.toBean(writeService.getWrite(write.getId()), AiWriteRespVO.class);
        assertEquals(reqVO.getSceneParams().setAudience(null), respVO.getSceneParams());
    }

    @Test
    public void testValidate_translateTargetLanguage() {
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.TRANSLATE).setOriginalContent("hello");
        // 目标语言与源语言相同
        assertInvalid(reqVO.setSceneParams(translateParams(3, 3)), "目标语言不能是自动，也不能与源语言相同");
        // 目标语言是自动
        assertInvalid(reqVO.setSceneParams(translateParams(1, 1)), "目标语言不能是自动，也不能与源语言相同");
        // 目标语言不存在
        assertInvalid(reqVO.setSceneParams(translateParams(1, 99)), "目标语言必须是 [1, 2, 3, 4, 5]");
        // 缺少场景参数
        assertInvalid(reqVO.setSceneParams(null), "源语言、目标语言、领域、是否保留格式不能为空");
    }

    @Test
    public void testGenerate_xiaohongshu() {
        // 调用：小红书；格式、语气、原文、其它场景参数传了也丢弃
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.XIAOHONGSHU).setPrompt("机械键盘").setOriginalContent("多余")
                .setSceneParams(new AiWriteGenerateReqVO.SceneParams().setAudience("程序员").setStyle("测评")
                        .setEmoji(true).setTagCount(5).setMode("润色"));
        assertValid(reqVO);
        generate(reqVO);

        // 断言
        AiWriteDO write = assertWrite(AiWriteTypeEnum.XIAOHONGSHU, "正文", null);
        assertNull(write.getOriginalContent());
        assertNull(write.getFormat());
        assertNull(write.getTone());
        assertEquals(1, write.getLanguage());
        assertEquals(new AiWriteDO.SceneParams().setAudience("程序员").setStyle("测评").setEmoji(true).setTagCount(5),
                write.getSceneParams());
        assertEquals("主题 / 产品：[机械键盘]。目标受众：程序员；风格：测评；使用 emoji：是；话题标签 5 个。语言：自动，长度：自动。",
                captureMessages().get(1).getText());
    }

    @Test
    public void testValidate_xiaohongshuAudienceRequired() {
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.XIAOHONGSHU).setPrompt("机械键盘")
                .setSceneParams(new AiWriteGenerateReqVO.SceneParams().setStyle("种草").setEmoji(true).setTagCount(5));
        assertInvalid(reqVO, "目标受众、风格、是否使用 emoji、话题标签数不能为空");
    }

    @Test
    public void testGenerate_rewriteKeepsLanguage() {
        // 调用：润色不使用输出语言
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.REWRITE).setOriginalContent("原文")
                .setSceneParams(new AiWriteGenerateReqVO.SceneParams().setMode("精简"));
        assertValid(reqVO);
        generate(reqVO);

        // 断言
        AiWriteDO write = assertWrite(AiWriteTypeEnum.REWRITE, "正文", null);
        assertNull(write.getLanguage());
        assertNull(write.getFormat());
        assertNull(write.getLength());
        assertEquals(new AiWriteDO.SceneParams().setMode("精简"), write.getSceneParams());
        assertEquals("处理方式：精简。语气：自动。补充要求：[无]。原文：[原文]", captureMessages().get(1).getText());
    }

    @Test
    public void testGenerate_weeklyReport() {
        // 调用：周报，语气为“专业”；周期为空时提示词写“无”
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.WEEKLY_REPORT).setOriginalContent("完成登录改造").setTone(5)
                .setSceneParams(new AiWriteGenerateReqVO.SceneParams().setRole("后端"));
        assertValid(reqVO);
        generate(reqVO);

        // 断言
        AiWriteDO write = assertWrite(AiWriteTypeEnum.WEEKLY_REPORT, "正文", null);
        assertNull(write.getFormat());
        assertEquals(new AiWriteDO.SceneParams().setRole("后端"), write.getSceneParams());
        assertEquals("周期：无；岗位 / 团队：后端。本周完成事项：[完成登录改造]。下周计划 / 问题与风险：[无]。语气：专业，语言：自动，长度：自动。",
                captureMessages().get(1).getText());
    }

    @Test
    public void testValidate_replyOriginalRequired() {
        AiWriteGenerateReqVO reqVO = buildReqVO(AiWriteTypeEnum.REPLY).setPrompt("不批");
        assertInvalid(reqVO, "原文不能为空");
    }

    @Test
    public void testGenerate_modelDisabled() {
        // mock 方法：所选模型已禁用
        when(modalService.validateModel(MODEL_ID)).thenThrow(exception(ErrorCodeConstants.MODEL_DISABLE, MODEL));

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = writeService.generateWriteContent(
                buildReqVO(AiWriteTypeEnum.WRITING).setPrompt("主题"), USER_ID).collectList().block(TIMEOUT);

        // 断言：原因作为错误帧返回，不回落其它模型、不插入记录、不调用模型
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(ErrorCodeConstants.MODEL_DISABLE.getCode(), results.get(0).getCode());
        verify(modalService, never()).getRequiredDefaultModel(any());
        verify(modalService, never()).getChatModel(any());
        assertEquals(0, writeMapper.selectCount());
        assertEquals(0, requestLogMapper.selectCount());
    }

    @Test
    public void testGenerate_streamError() {
        // mock 方法：上游报错
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.error(new RuntimeException("upstream-error")));

        // 调用
        List<CommonResult<AiWriteGenerateRespVO>> results = writeService.generateWriteContent(
                buildReqVO(AiWriteTypeEnum.WRITING).setPrompt("主题"), USER_ID).collectList().block(TIMEOUT);

        // 断言：错误帧带出上游原因，写作记录、请求日志保存错误信息
        assertNotNull(results);
        assertEquals(ErrorCodeConstants.WRITE_GENERATE_FAIL.getCode(), results.get(0).getCode());
        assertEquals("写作生成失败：upstream-error", results.get(0).getMsg());
        AiWriteDO write = assertWrite(AiWriteTypeEnum.WRITING, null, "upstream-error");
        assertRequestLog(write, AiRequestLogStatusEnum.FAIL, "upstream-error");
    }

    // ========== 调用 ==========

    private List<CommonResult<AiWriteGenerateRespVO>> generate(AiWriteGenerateReqVO reqVO) {
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk("正文")));
        List<CommonResult<AiWriteGenerateRespVO>> results = writeService.generateWriteContent(reqVO, USER_ID)
                .collectList().block(TIMEOUT);
        assertNotNull(results);
        assertTrue(results.stream().allMatch(CommonResult::isSuccess));
        return results;
    }

    /**
     * 请求：四个字典字段都传“自动”，由场景决定保留哪些
     */
    private static AiWriteGenerateReqVO buildReqVO(AiWriteTypeEnum type) {
        AiWriteGenerateReqVO reqVO = new AiWriteGenerateReqVO();
        reqVO.setType(type.getType());
        reqVO.setModelId(MODEL_ID);
        reqVO.setLength(1);
        reqVO.setFormat(1);
        reqVO.setTone(1);
        reqVO.setLanguage(1);
        return reqVO;
    }

    private static AiWriteGenerateReqVO.SceneParams translateParams(Integer sourceLanguage, Integer targetLanguage) {
        return new AiWriteGenerateReqVO.SceneParams().setSourceLanguage(sourceLanguage).setTargetLanguage(targetLanguage)
                .setDomain("技术").setPreserveFormat(true);
    }

    private static ChatResponse chunk(String text) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("ut-response").model(MODEL)
                .usage(new DefaultUsage(1, 1, 2)).build();
        return new ChatResponse(Collections.singletonList(new Generation(new AssistantMessage(text))), metadata);
    }

    // ========== 断言 ==========

    private static Set<String> validate(AiWriteGenerateReqVO reqVO) {
        return VALIDATOR.validate(reqVO).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private static void assertValid(AiWriteGenerateReqVO reqVO) {
        assertEquals(Set.of(), validate(reqVO));
    }

    private static void assertInvalid(AiWriteGenerateReqVO reqVO, String message) {
        assertEquals(Set.of(message), validate(reqVO));
    }

    /**
     * 断言数据库中唯一的写作记录
     */
    private AiWriteDO assertWrite(AiWriteTypeEnum type, String generatedContent, String errorMessage) {
        List<AiWriteDO> writes = writeMapper.selectList();
        assertEquals(1, writes.size());
        AiWriteDO write = writes.get(0);
        assertEquals(type.getType(), write.getType());
        assertEquals(USER_ID, write.getUserId());
        assertEquals(MODEL_ID, write.getModelId());
        assertEquals(MODEL, write.getModel());
        assertEquals(AiPlatformEnum.OPENAI.getPlatform(), write.getPlatform());
        assertNotNull(write.getRequestId());
        assertEquals(generatedContent, write.getGeneratedContent());
        assertEquals(errorMessage, write.getErrorMessage());
        return write;
    }

    /**
     * 断言数据库中唯一的请求日志，与写作记录对应
     */
    private void assertRequestLog(AiWriteDO write, AiRequestLogStatusEnum status, String errorMessage) {
        List<AiRequestLogDO> logs = requestLogMapper.selectList();
        assertEquals(1, logs.size());
        AiRequestLogDO requestLog = logs.get(0);
        assertEquals(write.getRequestId(), requestLog.getRequestId());
        assertEquals(AiRequestLogBizTypeEnum.WRITE.getType(), requestLog.getBizType());
        assertEquals(write.getId(), requestLog.getBizId());
        assertEquals(status.getStatus(), requestLog.getStatus());
        assertEquals(errorMessage, requestLog.getErrorMessage());
        assertEquals(write.getTotalTokens(), requestLog.getTotalTokens());
    }

    private List<Message> captureMessages() {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).stream(captor.capture());
        List<Message> messages = captor.getValue().getInstructions();
        assertEquals(2, messages.size());
        return messages;
    }

}
