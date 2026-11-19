package cn.iocoder.yudao.module.ai.service.ppt;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptRedrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptSlideImageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ppt.AiPptDO;
import cn.iocoder.yudao.module.ai.dal.mysql.ppt.AiPptMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptModeEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptSourceEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiDocumentUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiPptServiceImpl} 的单元测试类
 *
 * 测试环境没有开启 @Async，异步生成、重绘在调用线程内同步执行完
 *
 * @author 芋道源码
 */
@Import(AiPptServiceImpl.class)
public class AiPptServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long CHAT_MODEL_ID = 10L;
    private static final Long IMAGE_MODEL_ID = 20L;
    private static final Long IMAGE_KEY_ID = 30L;

    private static final String SLIDES_JSON = "```json\n{\"title\": \"新能源汽车市场分析\", \"slides\": ["
            + "{\"index\": 1, \"theme\": \"封面\", \"title\": \"新能源汽车市场分析\", \"points\": [\"2025 年度\"], \"imagePrompt\": \"[商务风] 封面\"},"
            + "{\"index\": 2, \"theme\": \"市场\", \"title\": \"市场规模\", \"points\": [\"渗透率 45%\", \" \"], \"imagePrompt\": \"[商务风] 市场\"},"
            + "{\"index\": 3, \"theme\": \"技术\", \"title\": \"技术路线\", \"points\": [], \"imagePrompt\": \"[商务风] 技术\"}"
            + "]}\n```";

    @Resource
    private AiPptServiceImpl pptService;

    @Resource
    private AiPptMapper pptMapper;

    @MockitoBean
    private AiModelService modelService;
    @MockitoBean
    private AiApiKeyService apiKeyService;
    @MockitoBean
    private FileApi fileApi;

    // ========== 创作：提交 ==========

    @Test
    public void testGeneratePpt_success() {
        // mock 方法（模型）
        ChatModel chatModel = mockChatModel(SLIDES_JSON);
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        // mock 方法（文件）
        when(fileApi.createFile(any(byte[].class)))
                .thenReturn("https://file/1.png", "https://file/2.png", "https://file/3.png");
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("# 新能源汽车\n- 市场规模");

        // 调用
        Long id = pptService.generatePpt(USER_ID, reqVO);
        // 断言：任务
        AiPptDO ppt = pptMapper.selectById(id);
        assertEquals(USER_ID, ppt.getUserId());
        assertEquals("新能源汽车市场分析", ppt.getTitle());
        assertEquals(AiPptSourceEnum.PASTE.getSource(), ppt.getSource());
        assertEquals("# 新能源汽车\n- 市场规模", ppt.getContent());
        assertNull(ppt.getFileUrl());
        assertEquals(CHAT_MODEL_ID, ppt.getChatModelId());
        assertEquals(IMAGE_MODEL_ID, ppt.getImageModelId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(1, ppt.getAttempt());
        assertEquals(3, ppt.getSlideCount());
        assertEquals(3, ppt.getFinishCount());
        assertEquals("https://file/1.png", ppt.getCoverUrl());
        assertNull(ppt.getErrorMessage());
        assertNotNull(ppt.getFinishTime());
        // 断言：每页
        assertEquals(3, ppt.getSlides().size());
        AiPptDO.Slide slide = ppt.getSlides().get(1);
        assertEquals(2, slide.getIndex());
        assertEquals("市场规模", slide.getTitle());
        assertEquals(singletonList("渗透率 45%"), slide.getPoints());
        assertEquals("https://file/2.png", slide.getImageUrl());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), slide.getStatus());
        assertEquals(1, slide.getImages().size());
        assertEquals("[商务风] 市场", slide.getImages().get(0).getPrompt());
        // 断言：调用
        verify(chatModel).call(any(Prompt.class));
        verify(imageModel, times(3)).call(any(ImagePrompt.class));
    }

    @Test
    public void testGeneratePpt_upload() {
        // mock 方法（材料）
        when(fileApi.presignGetUrlQuietly(eq("https://file/年度报告.md"))).thenReturn("https://file/年度报告.md?sign");
        // mock 方法（模型）
        mockChatModel(SLIDES_JSON);
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/page.png");
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setSource(AiPptSourceEnum.UPLOAD.getSource())
                .setFileUrl("https://file/年度报告.md");

        // 调用
        Long id;
        try (MockedStatic<AiDocumentUtils> documentUtilsMock = mockStatic(AiDocumentUtils.class)) {
            documentUtilsMock.when(() -> AiDocumentUtils.readUrl(eq("https://file/年度报告.md?sign"))).thenReturn("# 年度报告\n内容");
            id = pptService.generatePpt(USER_ID, reqVO);
        }
        // 断言
        AiPptDO ppt = pptMapper.selectById(id);
        assertEquals(AiPptSourceEnum.UPLOAD.getSource(), ppt.getSource());
        assertEquals("新能源汽车市场分析", ppt.getTitle());
        assertEquals("https://file/年度报告.md", ppt.getFileUrl());
        assertEquals("# 年度报告\n内容", ppt.getContent());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
    }

    @Test
    public void testGeneratePpt_slidesMoreThanPageCount() {
        // mock 方法：请求 3 页，模型返回 4 页
        mockChatModel("{\"title\": \"标题\", \"slides\": [{\"imagePrompt\": \"1\"}, {\"imagePrompt\": \"2\"},"
                + " {\"imagePrompt\": \"3\"}, {\"imagePrompt\": \"4\"}]}");
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/page.png");
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("大纲");

        // 调用
        Long id = pptService.generatePpt(USER_ID, reqVO);
        // 断言：多给的页丢弃
        AiPptDO ppt = pptMapper.selectById(id);
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(3, ppt.getSlideCount());
        assertEquals(3, ppt.getSlides().size());
        verify(imageModel, times(3)).call(any(ImagePrompt.class));
    }

    @Test
    public void testGeneratePpt_imageModelNotSupported() {
        // mock 方法：Midjourney 不在支持的平台里
        when(modelService.validateModel(eq(IMAGE_MODEL_ID))).thenReturn(new AiModelDO().setId(IMAGE_MODEL_ID)
                .setType(AiModelTypeEnum.IMAGE.getType()).setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()));
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("大纲");

        // 调用，并断言异常
        assertServiceException(() -> pptService.generatePpt(USER_ID, reqVO), PPT_IMAGE_MODEL_NOT_SUPPORTED);
        assertEquals(0L, pptMapper.selectCount());
    }

    @Test
    public void testGeneratePpt_imageModelPlatformMismatch() {
        // mock 方法：OpenAI 的模型绑定了硅基流动的密钥，实际会按硅基流动创建客户端，与 OpenAI 的生图参数不匹配
        mockImageModel();
        when(apiKeyService.validateApiKey(eq(IMAGE_KEY_ID))).thenReturn(new AiApiKeyDO().setId(IMAGE_KEY_ID)
                .setPlatform(AiPlatformEnum.SILICONFLOW.getPlatform()));
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("大纲");

        // 调用，并断言异常
        assertServiceException(() -> pptService.generatePpt(USER_ID, reqVO), PPT_IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.OPENAI.getPlatform(), AiPlatformEnum.SILICONFLOW.getPlatform());
        assertEquals(0L, pptMapper.selectCount());
        verify(modelService, never()).getImageModel(any());
    }

    @Test
    public void testGeneratePpt_slidesFail() {
        // mock 方法：模型返回的不是 JSON
        mockChatModel("抱歉，我无法完成");
        mockImageModel();
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("大纲");

        // 调用
        Long id = pptService.generatePpt(USER_ID, reqVO);
        // 断言：已失败，没有分镜
        AiPptDO ppt = pptMapper.selectById(id);
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt.getStatus());
        assertEquals("分镜生成失败：模型返回的分镜不是合法的 JSON", ppt.getErrorMessage());
        assertTrue(ppt.getSlides().isEmpty());
        assertEquals(0, ppt.getSlideCount());
        assertNotNull(ppt.getFinishTime());
    }

    @Test
    public void testGeneratePpt_slideImageFail() {
        // mock 方法：第 1 页成功，第 2 页失败
        mockChatModel(SLIDES_JSON);
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse())
                .thenThrow(new IllegalStateException("额度不足"));
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/1.png");
        // 准备参数
        AiPptGenerateReqVO reqVO = buildGenerateReqVO().setContent("大纲");

        // 调用
        Long id = pptService.generatePpt(USER_ID, reqVO);
        // 断言：任务已失败，第 1 页的图保留
        AiPptDO ppt = pptMapper.selectById(id);
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt.getStatus());
        assertEquals("第 2 页：额度不足", ppt.getErrorMessage());
        assertEquals(3, ppt.getSlideCount());
        assertEquals(1, ppt.getFinishCount());
        assertEquals("https://file/1.png", ppt.getCoverUrl());
        // 断言：第 2 页失败，第 3 页待生成
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt.getSlides().get(1).getStatus());
        assertEquals("额度不足", ppt.getSlides().get(1).getErrorMessage());
        assertNull(ppt.getSlides().get(2).getStatus());
        assertNull(ppt.getSlides().get(2).getImageUrl());
        verify(imageModel, times(2)).call(any(ImagePrompt.class));
    }

    @Test
    public void testResumePptMy_success() {
        // mock 数据：第 1 页有图，第 2 页失败
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, null, AiPptStatusEnum.FAIL.getStatus()).setErrorMessage("额度不足")));
        pptMapper.insert(dbPpt);
        // mock 方法
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/2.png");

        // 调用
        pptService.resumePptMy(dbPpt.getId(), USER_ID);
        // 断言：只补第 2 页，不重新生成分镜；开始了新一轮
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertNull(ppt.getErrorMessage());
        assertEquals(2, ppt.getFinishCount());
        assertEquals(1, ppt.getSlides().get(0).getImages().size());
        assertEquals("https://file/2.png", ppt.getSlides().get(1).getImageUrl());
        assertNull(ppt.getSlides().get(1).getErrorMessage());
        verify(imageModel, times(1)).call(any(ImagePrompt.class));
        verify(modelService, never()).getChatModel(any());
    }

    @Test
    public void testResumePptMy_slidesEmpty() {
        // mock 数据：分镜阶段失败
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), new ArrayList<>()).setErrorMessage("分镜生成失败：超时");
        pptMapper.insert(dbPpt);
        // mock 方法
        mockChatModel(SLIDES_JSON);
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/page.png");

        // 调用
        pptService.resumePptMy(dbPpt.getId(), USER_ID);
        // 断言：重新生成分镜，再逐页生图
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals("新能源汽车市场分析", ppt.getTitle());
        assertEquals(3, ppt.getSlideCount());
        assertEquals(3, ppt.getFinishCount());
    }

    @Test
    public void testResumePptMy_statusNotFail() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(),
                singletonList(buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);

        // 调用，并断言异常
        assertServiceException(() -> pptService.resumePptMy(dbPpt.getId(), USER_ID), PPT_STATUS_NOT_FAIL);
    }

    @Test
    public void testResumePptMy_notExists() {
        // mock 数据：其它用户的
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), new ArrayList<>()).setUserId(2L);
        pptMapper.insert(dbPpt);

        // 调用，并断言异常
        assertServiceException(() -> pptService.resumePptMy(dbPpt.getId(), USER_ID), PPT_NOT_EXISTS);
    }

    @Test
    public void testRedrawSlideMy_success() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, "https://file/2.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // mock 方法
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse());
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/1-v2.png");
        // 准备参数
        AiPptRedrawReqVO reqVO = new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(1).setPrompt("[商务风] 封面，标题改为蓝色");

        // 调用
        pptService.redrawSlideMy(reqVO, USER_ID);
        // 断言：追加版本、设为当前，封面随之变化；开始了新一轮
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertEquals("https://file/1-v2.png", ppt.getCoverUrl());
        AiPptDO.Slide slide = ppt.getSlides().get(0);
        assertEquals("https://file/1-v2.png", slide.getImageUrl());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), slide.getStatus());
        assertEquals(2, slide.getImages().size());
        assertEquals("[商务风] 封面，标题改为蓝色", slide.getImages().get(1).getPrompt());
        verify(imageModel).call(argThat((ImagePrompt prompt) ->
                "[商务风] 封面，标题改为蓝色".equals(prompt.getInstructions().get(0).getText())));
    }

    @Test
    public void testRedrawSlideMy_fail() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, "https://file/2.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // mock 方法
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenThrow(new IllegalStateException("内容审核未通过"));
        // 准备参数
        AiPptRedrawReqVO reqVO = new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(2).setPrompt("新提示词");

        // 调用
        pptService.redrawSlideMy(reqVO, USER_ID);
        // 断言：本页记录失败原因，当前图不变；每页都有图，任务回到已完成
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertNull(ppt.getErrorMessage());
        AiPptDO.Slide slide = ppt.getSlides().get(1);
        assertEquals("https://file/2.png", slide.getImageUrl());
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), slide.getStatus());
        assertEquals("内容审核未通过", slide.getErrorMessage());
        assertEquals(1, slide.getImages().size());
    }

    @Test
    public void testRedrawSlideMy_imageModelPlatformMismatch() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(),
                singletonList(buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // mock 方法：创建后，模型改绑了硅基流动的密钥
        ImageModel imageModel = mockImageModel();
        when(apiKeyService.validateApiKey(eq(IMAGE_KEY_ID))).thenReturn(new AiApiKeyDO().setId(IMAGE_KEY_ID)
                .setPlatform(AiPlatformEnum.SILICONFLOW.getPlatform()));
        // 准备参数
        AiPptRedrawReqVO reqVO = new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(1).setPrompt("新提示词");

        // 调用
        pptService.redrawSlideMy(reqVO, USER_ID);
        // 断言：不调用绘图模型；本页记录平台不一致，当前图不变，任务回到已完成
        verify(imageModel, never()).call(any(ImagePrompt.class));
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        AiPptDO.Slide slide = ppt.getSlides().get(0);
        assertEquals("https://file/1.png", slide.getImageUrl());
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), slide.getStatus());
        assertTrue(slide.getErrorMessage().contains("不一致"), slide.getErrorMessage());
    }

    @Test
    public void testRedrawSlideMy_statusInProgress() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                singletonList(buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // 准备参数
        AiPptRedrawReqVO reqVO = new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(1).setPrompt("新提示词");

        // 调用，并断言异常
        assertServiceException(() -> pptService.redrawSlideMy(reqVO, USER_ID), PPT_STATUS_IN_PROGRESS);
    }

    @Test
    public void testRedrawSlideMy_slideImageNotExists() {
        // mock 数据：第 2 页没有图
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, null, AiPptStatusEnum.FAIL.getStatus())));
        pptMapper.insert(dbPpt);
        // 准备参数
        AiPptRedrawReqVO reqVO = new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(2).setPrompt("新提示词");

        // 调用，并断言异常
        assertServiceException(() -> pptService.redrawSlideMy(reqVO, USER_ID), PPT_SLIDE_IMAGE_NOT_EXISTS, 2);
        // 断言：状态未变化
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), pptMapper.selectById(dbPpt.getId()).getStatus());
    }

    @Test
    public void testUpdateSlideImageMy_success() {
        // mock 数据：第 1 页有两个版本，当前为第 2 个
        AiPptDO.Slide slide = buildSlide(1, "https://file/1-v2.png", AiPptStatusEnum.SUCCESS.getStatus());
        slide.getImages().add(0, new AiPptDO.SlideImage().setUrl("https://file/1.png").setPrompt("旧提示词")
                .setCreateTime(LocalDateTime.now().minusMinutes(5)));
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(), singletonList(slide));
        pptMapper.insert(dbPpt);
        // 准备参数
        AiPptSlideImageReqVO reqVO = new AiPptSlideImageReqVO().setId(dbPpt.getId()).setSlideIndex(1)
                .setImageUrl("https://file/1.png");

        // 调用
        pptService.updateSlideImageMy(reqVO, USER_ID);
        // 断言：切换当前图，不新增版本，封面随之变化
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals("https://file/1.png", ppt.getSlides().get(0).getImageUrl());
        assertEquals(2, ppt.getSlides().get(0).getImages().size());
        assertEquals("https://file/1.png", ppt.getCoverUrl());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
    }

    @Test
    public void testUpdateSlideImageMy_concurrent() {
        // mock 数据：两页各有两个版本，当前都为第 1 个
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, "https://file/2.png", AiPptStatusEnum.SUCCESS.getStatus())));
        dbPpt.getSlides().forEach(slide -> slide.getImages().add(new AiPptDO.SlideImage()
                .setUrl("https://file/" + slide.getIndex() + "-v2.png").setCreateTime(LocalDateTime.now())));
        pptMapper.insert(dbPpt);
        // mock 方法：请求一读取后、写入前，请求二切换了第 2 页
        AiPptMapper spyMapper = spy(pptMapper);
        AtomicBoolean intercepted = new AtomicBoolean();
        doAnswer(invocation -> {
            AiPptDO snapshot = pptMapper.selectById(dbPpt.getId());
            if (intercepted.compareAndSet(false, true)) {
                pptService.updateSlideImageMy(new AiPptSlideImageReqVO().setId(dbPpt.getId()).setSlideIndex(2)
                        .setImageUrl("https://file/2-v2.png"), USER_ID);
            }
            return snapshot;
        }).when(spyMapper).selectById(eq(dbPpt.getId()));
        ReflectionTestUtils.setField(pptService, "pptMapper", spyMapper);

        try {
            // 调用，并断言异常：请求一基于旧数据，拒绝
            assertServiceException(() -> pptService.updateSlideImageMy(new AiPptSlideImageReqVO().setId(dbPpt.getId())
                    .setSlideIndex(1).setImageUrl("https://file/1-v2.png"), USER_ID), PPT_UPDATE_CONFLICT);
        } finally {
            ReflectionTestUtils.setField(pptService, "pptMapper", pptMapper);
        }
        // 断言：请求二已成功的切换保留，第 1 页未变
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals("https://file/1.png", ppt.getSlides().get(0).getImageUrl());
        assertEquals("https://file/2-v2.png", ppt.getSlides().get(1).getImageUrl());
        assertEquals(2, ppt.getAttempt());
    }

    @Test
    public void testUpdateSlideImageMy_versionNotExists() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(),
                singletonList(buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // 准备参数
        AiPptSlideImageReqVO reqVO = new AiPptSlideImageReqVO().setId(dbPpt.getId()).setSlideIndex(1)
                .setImageUrl("https://other/1.png");

        // 调用，并断言异常
        assertServiceException(() -> pptService.updateSlideImageMy(reqVO, USER_ID), PPT_SLIDE_VERSION_NOT_EXISTS, 1);
    }

    @Test
    public void testUpdateSlideImageMy_previewUrl() {
        // mock 数据：库里保存的是上传时返回的预签名地址
        String url = "https://bucket/ai/ppt/1.png?X-Amz-Signature=old";
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(),
                singletonList(buildSlide(1, url, AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // 准备参数：展示用的重新签名地址，不是版本的身份
        AiPptSlideImageReqVO reqVO = new AiPptSlideImageReqVO().setId(dbPpt.getId()).setSlideIndex(1)
                .setImageUrl("https://bucket/ai/ppt/1.png?X-Amz-Signature=new");

        // 调用，并断言异常
        assertServiceException(() -> pptService.updateSlideImageMy(reqVO, USER_ID), PPT_SLIDE_VERSION_NOT_EXISTS, 1);
        // 断言：库里的地址未变化
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(url, ppt.getSlides().get(0).getImageUrl());
        assertEquals(url, ppt.getCoverUrl());
    }

    // ========== 我的 PPT ==========

    @Test
    public void testDeletePptMy_success() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), new ArrayList<>());
        pptMapper.insert(dbPpt);

        // 调用
        pptService.deletePptMy(dbPpt.getId(), USER_ID);
        // 断言
        assertNull(pptMapper.selectById(dbPpt.getId()));
    }

    @Test
    public void testDeletePptMy_statusInProgress() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(), new ArrayList<>());
        pptMapper.insert(dbPpt);

        // 调用，并断言异常
        assertServiceException(() -> pptService.deletePptMy(dbPpt.getId(), USER_ID), PPT_STATUS_IN_PROGRESS);
        assertNotNull(pptMapper.selectById(dbPpt.getId()));
    }

    @Test
    public void testExportPpt_statusNotSuccess() {
        // 准备参数
        AiPptDO ppt = buildPpt(AiPptStatusEnum.FAIL.getStatus(), new ArrayList<>());

        // 调用，并断言异常
        assertServiceException(() -> pptService.exportPpt(ppt, "pptx"), PPT_STATUS_NOT_SUCCESS);
    }

    @Test
    public void testExportPpt_formatInvalid() {
        // 准备参数
        AiPptDO ppt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(), new ArrayList<>());

        // 调用，并断言异常
        assertServiceException(() -> pptService.exportPpt(ppt, "docx"), PPT_EXPORT_FORMAT_INVALID);
    }

    // ========== 超时 ==========

    @Test
    public void testTimeoutPpt() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(AiPptServiceImpl.TIMEOUT_MINUTES + 1);
        // mock 数据：首次生成超时，第 2 页生成中
        AiPptDO dbPpt01 = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(), Arrays.asList(
                buildSlide(1, "https://file/1.png", AiPptStatusEnum.SUCCESS.getStatus()),
                buildSlide(2, null, AiPptStatusEnum.IN_PROGRESS.getStatus())));
        dbPpt01.setUpdateTime(timeoutTime);
        pptMapper.insert(dbPpt01);
        // mock 数据：重绘超时，每页都有图
        AiPptDO dbPpt02 = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                singletonList(buildSlide(1, "https://file/1.png", AiPptStatusEnum.IN_PROGRESS.getStatus())));
        dbPpt02.setUpdateTime(timeoutTime);
        pptMapper.insert(dbPpt02);
        // mock 数据：生成中，未超时
        AiPptDO dbPpt03 = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                singletonList(buildSlide(1, null, AiPptStatusEnum.IN_PROGRESS.getStatus())));
        pptMapper.insert(dbPpt03);

        // 调用
        int count = pptService.timeoutPpt();
        // 断言
        assertEquals(2, count);
        // 断言：首次生成超时，已失败
        AiPptDO ppt01 = pptMapper.selectById(dbPpt01.getId());
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt01.getStatus());
        assertEquals("生成超时", ppt01.getErrorMessage());
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt01.getSlides().get(1).getStatus());
        assertEquals("生成超时", ppt01.getSlides().get(1).getErrorMessage());
        assertEquals(1, ppt01.getFinishCount());
        // 断言：重绘超时，回到已完成，本页记录失败原因
        AiPptDO ppt02 = pptMapper.selectById(dbPpt02.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt02.getStatus());
        assertNull(ppt02.getErrorMessage());
        assertEquals(AiPptStatusEnum.FAIL.getStatus(), ppt02.getSlides().get(0).getStatus());
        assertEquals("https://file/1.png", ppt02.getSlides().get(0).getImageUrl());
        // 断言：未超时的不变
        assertEquals(AiPptStatusEnum.IN_PROGRESS.getStatus(), pptMapper.selectById(dbPpt03.getId()).getStatus());
    }

    @Test
    public void testTimeoutPpt_progressAfterQuery() {
        // mock 数据：生成中、已超时，第 1 页生成中
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(), Arrays.asList(
                buildSlide(1, null, AiPptStatusEnum.IN_PROGRESS.getStatus()), buildSlide(2, null, null)));
        dbPpt.setUpdateTime(LocalDateTime.now().minusMinutes(AiPptServiceImpl.TIMEOUT_MINUTES + 1));
        pptMapper.insert(dbPpt);
        // mock 方法：超时查询之后、写入之前，生成线程保存了第 1 页（更新时间刷新为当前）
        AiPptMapper spyMapper = spy(pptMapper);
        doAnswer(invocation -> {
            List<AiPptDO> list = pptMapper.selectListByStatusAndUpdateTimeLt(invocation.getArgument(0), invocation.getArgument(1));
            AiPptDO progress = pptMapper.selectById(dbPpt.getId());
            progress.getSlides().get(0).setImageUrl("https://file/1.png").setStatus(AiPptStatusEnum.SUCCESS.getStatus());
            progress.setUpdateTime(null);
            pptMapper.updateById(progress);
            return list;
        }).when(spyMapper).selectListByStatusAndUpdateTimeLt(anyInt(), any(LocalDateTime.class));
        ReflectionTestUtils.setField(pptService, "pptMapper", spyMapper);

        // 调用
        int count;
        try {
            count = pptService.timeoutPpt();
        } finally {
            ReflectionTestUtils.setField(pptService, "pptMapper", pptMapper);
        }
        // 断言：不结束，保留刚保存的进度
        assertEquals(0, count);
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.IN_PROGRESS.getStatus(), ppt.getStatus());
        assertEquals("https://file/1.png", ppt.getSlides().get(0).getImageUrl());
    }

    // ========== 迟到的上一轮 ==========
    // 下面的用例在 mock 的模型调用里执行超时、继续生成或再次重绘，模拟上一轮的模型调用迟迟不返回；
    // 测试环境没有开启 @Async，新一轮在调用线程内同步执行

    @Test
    public void testExecuteGeneratePpt_attemptChanged() {
        // mock 数据：已经是第 2 轮
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                singletonList(buildSlide(1, null, null))).setAttempt(2);
        pptMapper.insert(dbPpt);

        // 调用：第 1 轮的执行者才开始
        pptService.executeGeneratePpt(dbPpt.getId(), 1);
        pptService.executeRedrawSlide(dbPpt.getId(), 1, 1, "提示词");
        // 断言：不调用模型，不修改
        verify(modelService, never()).getImageModel(any());
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.IN_PROGRESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertNull(ppt.getSlides().get(0).getStatus());
    }

    @Test
    public void testExecuteGeneratePpt_staleImageWhileResumeRunning() {
        // mock 数据：第 1 轮生成中
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                Arrays.asList(buildSlide(1, null, null), buildSlide(2, null, null)));
        pptMapper.insert(dbPpt);
        // mock 方法：第 1 轮生图期间超时，用户继续生成，第 2 轮正在生成第 1 页
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenAnswer(invocation -> {
            timeoutPpt(dbPpt.getId());
            List<AiPptDO.Slide> slides = pptMapper.selectById(dbPpt.getId()).getSlides();
            slides.get(0).setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setErrorMessage(null);
            pptMapper.updateById(new AiPptDO().setId(dbPpt.getId()).setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus())
                    .setAttempt(2).setSlides(slides));
            return buildImageResponse("old");
        });
        mockCreateFile();

        // 调用：第 1 轮的图迟到返回
        pptService.executeGeneratePpt(dbPpt.getId(), 1);
        // 断言：不写入第 2 轮，也不结束第 2 轮
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.IN_PROGRESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertEquals(0, ppt.getFinishCount());
        assertNull(ppt.getCoverUrl());
        assertNull(ppt.getSlides().get(0).getImageUrl());
        assertTrue(ppt.getSlides().get(0).getImages().isEmpty());
        assertEquals(AiPptStatusEnum.IN_PROGRESS.getStatus(), ppt.getSlides().get(0).getStatus());
        verify(imageModel, times(1)).call(any(ImagePrompt.class));
    }

    @Test
    public void testExecuteGeneratePpt_staleImageFailAfterResume() {
        // mock 数据：第 1 轮生成中
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(),
                Arrays.asList(buildSlide(1, null, null), buildSlide(2, null, null)));
        pptMapper.insert(dbPpt);
        // mock 方法：第 1 轮生图期间超时、继续生成，第 2 轮全部完成后，第 1 轮才报错返回
        ImageModel imageModel = mockImageModel();
        AtomicInteger callCount = new AtomicInteger();
        when(imageModel.call(any(ImagePrompt.class))).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() > 0) {
                return buildImageResponse("new");
            }
            timeoutPpt(dbPpt.getId());
            pptService.resumePptMy(dbPpt.getId(), USER_ID);
            throw new IllegalStateException("上一轮超时报错");
        });
        mockCreateFile();

        // 调用
        pptService.executeGeneratePpt(dbPpt.getId(), 1);
        // 断言：第 1 轮的失败不会把已完成的第 2 轮改为失败
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertNull(ppt.getErrorMessage());
        assertEquals(2, ppt.getFinishCount());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getSlides().get(0).getStatus());
        assertNull(ppt.getSlides().get(0).getErrorMessage());
        assertEquals("https://file/new.png", ppt.getSlides().get(0).getImageUrl());
        assertEquals(1, ppt.getSlides().get(0).getImages().size());
    }

    @Test
    public void testExecuteGeneratePpt_staleSlidesAfterResume() {
        // mock 数据：第 1 轮分镜中
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(), new ArrayList<>());
        pptMapper.insert(dbPpt);
        // mock 方法：第 1 轮分镜期间超时、继续生成，第 2 轮全部完成后，第 1 轮的分镜才返回
        ChatModel chatModel = mockChatModel(SLIDES_JSON);
        AtomicInteger callCount = new AtomicInteger();
        when(chatModel.call(any(Prompt.class))).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() > 0) {
                return buildChatResponse(SLIDES_JSON);
            }
            timeoutPpt(dbPpt.getId());
            pptService.resumePptMy(dbPpt.getId(), USER_ID);
            return buildChatResponse("{\"title\": \"旧标题\", \"slides\": [{\"title\": \"旧页\", \"imagePrompt\": \"旧提示词\"}]}");
        });
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse("new"));
        mockCreateFile();

        // 调用
        pptService.executeGeneratePpt(dbPpt.getId(), 1);
        // 断言：保留第 2 轮的分镜，第 1 轮不再生图
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertEquals("新能源汽车市场分析", ppt.getTitle());
        assertEquals(3, ppt.getSlideCount());
        assertEquals(3, ppt.getFinishCount());
        verify(imageModel, times(3)).call(any(ImagePrompt.class));
    }

    @Test
    public void testExecuteGeneratePpt_staleSlidesFailAfterResume() {
        // mock 数据：第 1 轮分镜中
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.IN_PROGRESS.getStatus(), new ArrayList<>());
        pptMapper.insert(dbPpt);
        // mock 方法：第 1 轮分镜期间超时、继续生成，第 2 轮全部完成后，第 1 轮才报错返回
        ChatModel chatModel = mockChatModel(SLIDES_JSON);
        AtomicInteger callCount = new AtomicInteger();
        when(chatModel.call(any(Prompt.class))).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() > 0) {
                return buildChatResponse(SLIDES_JSON);
            }
            timeoutPpt(dbPpt.getId());
            pptService.resumePptMy(dbPpt.getId(), USER_ID);
            throw new IllegalStateException("上一轮超时报错");
        });
        ImageModel imageModel = mockImageModel();
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(buildImageResponse("new"));
        mockCreateFile();

        // 调用
        pptService.executeGeneratePpt(dbPpt.getId(), 1);
        // 断言：第 1 轮的失败不会把已完成的第 2 轮改为失败
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(2, ppt.getAttempt());
        assertNull(ppt.getErrorMessage());
        assertEquals(3, ppt.getFinishCount());
    }

    @Test
    public void testExecuteRedrawSlide_staleAfterRedrawAgain() {
        // mock 数据
        AiPptDO dbPpt = buildPpt(AiPptStatusEnum.SUCCESS.getStatus(),
                singletonList(buildSlide(1, "https://file/v1.png", AiPptStatusEnum.SUCCESS.getStatus())));
        pptMapper.insert(dbPpt);
        // mock 方法：第 1 次重绘期间超时（回到已完成），用户再次重绘并完成后，第 1 次重绘的图才返回
        ImageModel imageModel = mockImageModel();
        AtomicInteger callCount = new AtomicInteger();
        when(imageModel.call(any(ImagePrompt.class))).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() > 0) {
                return buildImageResponse("v3");
            }
            timeoutPpt(dbPpt.getId());
            pptService.redrawSlideMy(new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(1)
                    .setPrompt("第二次重绘"), USER_ID);
            return buildImageResponse("v2");
        });
        mockCreateFile();

        // 调用：第 1 次重绘
        pptService.redrawSlideMy(new AiPptRedrawReqVO().setId(dbPpt.getId()).setSlideIndex(1)
                .setPrompt("第一次重绘"), USER_ID);
        // 断言：只有第二次重绘的图，第 1 次的迟到结果被丢弃
        AiPptDO ppt = pptMapper.selectById(dbPpt.getId());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), ppt.getStatus());
        assertEquals(3, ppt.getAttempt());
        AiPptDO.Slide slide = ppt.getSlides().get(0);
        assertEquals("https://file/v3.png", slide.getImageUrl());
        assertEquals(AiPptStatusEnum.SUCCESS.getStatus(), slide.getStatus());
        assertNull(slide.getErrorMessage());
        assertEquals(Arrays.asList("https://file/v1.png", "https://file/v3.png"),
                convertList(slide.getImages(), AiPptDO.SlideImage::getUrl));
        assertEquals("第二次重绘", slide.getImages().get(1).getPrompt());
    }

    // ========== 工具方法 ==========

    @Test
    public void testParseSlides_object() {
        // 调用
        AiPptDO plan = AiPptServiceImpl.parseSlides(SLIDES_JSON);
        // 断言
        assertEquals("新能源汽车市场分析", plan.getTitle());
        assertEquals(3, plan.getSlides().size());
        assertEquals(3, plan.getSlides().get(2).getIndex());
        assertEquals("[商务风] 技术", plan.getSlides().get(2).getImagePrompt());
        assertTrue(plan.getSlides().get(2).getImages().isEmpty());
        assertNull(plan.getSlides().get(2).getStatus());
    }

    @Test
    public void testParseSlides_reindex() {
        // 准备参数：前有说明文字，页码乱序，带有分镜以外的字段
        String text = "分镜如下：{\"slides\": [{\"index\": 5, \"title\": \"封面\", \"imagePrompt\": \"封面图\","
                + " \"imageUrl\": \"https://other/1.png\", \"status\": 20},"
                + "{\"index\": 9, \"title\": \"结尾\", \"imagePrompt\": \"结尾图\"}]}";

        // 调用
        AiPptDO plan = AiPptServiceImpl.parseSlides(text);
        // 断言：页码按顺序重排，只保留分镜字段
        assertNull(plan.getTitle());
        assertEquals(2, plan.getSlides().size());
        assertEquals(1, plan.getSlides().get(0).getIndex());
        assertEquals("封面图", plan.getSlides().get(0).getImagePrompt());
        assertNull(plan.getSlides().get(0).getImageUrl());
        assertNull(plan.getSlides().get(0).getStatus());
        assertEquals(2, plan.getSlides().get(1).getIndex());
    }

    @Test
    public void testParseSlides_notJson() {
        // 调用，并断言异常：模型输出被截断
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AiPptServiceImpl.parseSlides("{\"title\": \"标题\", \"slides\": [{\"imagePrompt\": \"封面图\""));
        assertEquals("模型返回的分镜不是合法的 JSON", ex.getMessage());
    }

    @Test
    public void testParseSlides_imagePromptEmpty() {
        // 准备参数
        String text = "{\"title\": \"标题\", \"slides\": [{\"index\": 1, \"title\": \"封面\", \"imagePrompt\": \"封面图\"},"
                + "{\"index\": 2, \"title\": \"目录\"}]}";

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> AiPptServiceImpl.parseSlides(text));
        assertEquals("第 2 页缺少生图提示词", ex.getMessage());
    }

    @Test
    public void testParseSlides_slidesEmpty() {
        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AiPptServiceImpl.parseSlides("{\"title\": \"标题\", \"slides\": []}"));
        assertEquals("模型返回的分镜为空", ex.getMessage());
    }

    @Test
    public void testBuildTitle() {
        assertEquals("新能源汽车市场分析", AiPptServiceImpl.buildTitle("\n  \n## 新能源汽车市场分析\n- 市场规模"));
        assertEquals("要点", AiPptServiceImpl.buildTitle("- 要点"));
        assertEquals("未命名演示文稿", AiPptServiceImpl.buildTitle("#  \n---"));
    }

    @Test
    public void testBuildImageOptions() {
        // 断言：各平台取接近 16:9 的宽高
        ImageOptions gptImage = AiPptServiceImpl.buildImageOptions(new AiModelDO()
                .setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("gpt-image-1"));
        assertEquals(1536, gptImage.getWidth());
        assertEquals(1024, gptImage.getHeight());
        ImageOptions dallE = AiPptServiceImpl.buildImageOptions(new AiModelDO()
                .setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("dall-e-3"));
        assertEquals(1792, dallE.getWidth());
        assertEquals(1024, dallE.getHeight());
        ImageOptions siliconFlow = AiPptServiceImpl.buildImageOptions(new AiModelDO()
                .setPlatform(AiPlatformEnum.SILICONFLOW.getPlatform()).setModel("Qwen/Qwen-Image"));
        assertEquals(1664, siliconFlow.getWidth());
        assertEquals(928, siliconFlow.getHeight());
        ImageOptions tongYi = AiPptServiceImpl.buildImageOptions(new AiModelDO()
                .setPlatform(AiPlatformEnum.ALIBABA.getPlatform()).setModel("wanx2.1-t2i-turbo"));
        assertEquals(1280, tongYi.getWidth());
        assertEquals(720, tongYi.getHeight());
    }

    // ========== 准备数据 ==========

    private AiPptGenerateReqVO buildGenerateReqVO() {
        return new AiPptGenerateReqVO().setSource(AiPptSourceEnum.PASTE.getSource()).setPrompt("极简商务风")
                .setMode(AiPptModeEnum.SLIDES.getMode()).setLanguage("zh-CN").setPageCount(3)
                .setImageModelId(IMAGE_MODEL_ID);
    }

    private ChatModel mockChatModel(String text) {
        AiModelDO model = new AiModelDO().setId(CHAT_MODEL_ID).setType(AiModelTypeEnum.CHAT.getType())
                .setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("gpt-4o");
        when(modelService.getRequiredDefaultModel(eq(AiModelTypeEnum.CHAT.getType()))).thenReturn(model);
        when(modelService.validateModel(eq(CHAT_MODEL_ID))).thenReturn(model);
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(eq(CHAT_MODEL_ID))).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(buildChatResponse(text));
        return chatModel;
    }

    private static ChatResponse buildChatResponse(String text) {
        return new ChatResponse(singletonList(new Generation(new AssistantMessage(text))));
    }

    private ImageModel mockImageModel() {
        AiModelDO model = new AiModelDO().setId(IMAGE_MODEL_ID).setType(AiModelTypeEnum.IMAGE.getType())
                .setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("gpt-image-1").setKeyId(IMAGE_KEY_ID);
        when(modelService.validateModel(eq(IMAGE_MODEL_ID))).thenReturn(model);
        when(apiKeyService.validateApiKey(eq(IMAGE_KEY_ID))).thenReturn(new AiApiKeyDO().setId(IMAGE_KEY_ID)
                .setPlatform(AiPlatformEnum.OPENAI.getPlatform()));
        ImageModel imageModel = mock(ImageModel.class);
        when(modelService.getImageModel(eq(IMAGE_MODEL_ID))).thenReturn(imageModel);
        return imageModel;
    }

    private static ImageResponse buildImageResponse() {
        return new ImageResponse(singletonList(new ImageGeneration(new Image(null, "aW1hZ2U="))));
    }

    /**
     * 图片内容为 name 的生图结果，配合 {@link #mockCreateFile()} 得到 https://file/{name}.png
     */
    private static ImageResponse buildImageResponse(String name) {
        String b64Json = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8));
        return new ImageResponse(singletonList(new ImageGeneration(new Image(null, b64Json))));
    }

    /**
     * mock 保存整页图：地址由图片内容决定，用于区分是哪一轮生成的图
     */
    private void mockCreateFile() {
        when(fileApi.createFile(any(byte[].class))).thenAnswer(invocation ->
                "https://file/" + new String((byte[]) invocation.getArgument(0), StandardCharsets.UTF_8) + ".png");
    }

    /**
     * 模拟超时：把更新时间改到超时之前，再执行超时处理
     */
    private void timeoutPpt(Long id) {
        AiPptDO updateObj = new AiPptDO().setId(id);
        updateObj.setUpdateTime(LocalDateTime.now().minusMinutes(AiPptServiceImpl.TIMEOUT_MINUTES + 1));
        pptMapper.updateById(updateObj);
        assertEquals(1, pptService.timeoutPpt());
    }

    private static AiPptDO buildPpt(Integer status, List<AiPptDO.Slide> slides) {
        List<AiPptDO.Slide> list = new ArrayList<>(slides);
        long finishCount = list.stream().filter(slide -> slide.getImageUrl() != null).count();
        return new AiPptDO().setUserId(USER_ID).setTitle("新能源汽车市场分析").setSource(AiPptSourceEnum.PASTE.getSource())
                .setContent("# 新能源汽车").setPrompt("").setMode(AiPptModeEnum.DETAILED.getMode()).setLanguage("zh-CN")
                .setPageCount(Math.max(list.size(), 3)).setChatModelId(CHAT_MODEL_ID).setImageModelId(IMAGE_MODEL_ID)
                .setSlides(list).setSlideCount(list.size()).setFinishCount((int) finishCount)
                .setCoverUrl(list.isEmpty() ? null : list.get(0).getImageUrl()).setStatus(status).setAttempt(1);
    }

    private static AiPptDO.Slide buildSlide(Integer index, String imageUrl, Integer status) {
        List<AiPptDO.SlideImage> images = new ArrayList<>();
        if (imageUrl != null) {
            images.add(new AiPptDO.SlideImage().setUrl(imageUrl).setPrompt("第 " + index + " 页提示词")
                    .setCreateTime(LocalDateTime.now()));
        }
        return new AiPptDO.Slide().setIndex(index).setTheme("主题").setTitle("第 " + index + " 页")
                .setPoints(Collections.singletonList("要点")).setImagePrompt("第 " + index + " 页提示词")
                .setImageUrl(imageUrl).setStatus(status).setImages(images);
    }

}
