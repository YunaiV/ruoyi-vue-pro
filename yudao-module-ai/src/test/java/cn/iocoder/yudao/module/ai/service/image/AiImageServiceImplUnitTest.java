package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageOptimizePromptReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageModeEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.sun.net.httpserver.HttpServer;
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * {@link AiImageServiceImpl} 的单元测试类
 *
 * 重点覆盖：
 * 1. 请求 VO 的显式字段，逐个写入 Options，并按平台映射到真实的上游请求参数
 * 2. Seedream 直接使用 ArkService 的请求、结果处理
 * 3. Midjourney 提示词拼接、子任务（按钮动作、局部重绘）、提交失败的记录
 * 4. Midjourney 同步按 API 密钥分组；状态只更新进行中的记录；超时
 * 5. 本人、公开成功等业务校验
 *
 * @author 芋道源码
 */
public class AiImageServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long IMAGE_ID = 10L;
    private static final Long MODEL_ID = 101L;
    private static final Long API_KEY_ID = 201L;
    private static final Long OTHER_API_KEY_ID = 202L;
    private static final String CUSTOM_ID = "MJ::JOB::upsample::1::task-a";
    private static final String INPAINT_CUSTOM_ID = "MJ::Inpaint::1::task-a::SOLO";
    private static final String NEW_TASK_ID = "task-new";
    private static final Integer IN_PROGRESS = AiImageStatusEnum.IN_PROGRESS.getStatus();

    @InjectMocks
    private AiImageServiceImpl imageService;

    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiImageMapper imageMapper;
    @Mock
    private FileApi fileApi;

    @Mock
    private MidjourneyApi midjourneyApi;
    @Mock
    private MidjourneyApi otherMidjourneyApi;

    /**
     * 本地图片服务：用于需要下载图片的场景（转 base64、转存）
     */
    private HttpServer imageServer;

    @AfterEach
    public void tearDown() {
        if (imageServer != null) {
            imageServer.stop(0);
        }
    }

    // ========== drawImage ==========

    @Test
    public void testDrawImage_siliconFlow() {
        // mock 方法
        AiModelDO model = mockModel(AiPlatformEnum.SILICONFLOW, "Kwai-Kolors/Kolors");
        ImageModel imageModel = mock(ImageModel.class);
        when(modelService.getImageModel(MODEL_ID)).thenReturn(imageModel);
        AiImageServiceImpl self = mock(AiImageServiceImpl.class);
        // 准备参数
        AiImageDrawReqVO reqVO = buildDrawReqVO().setNegativePrompt("文字").setSeed(42L).setSteps(30).setCfgScale(7.5)
                .setAspectRatio("4:3").setReferenceImageUrls(Collections.singletonList("https://file/ref.png"));

        // 调用
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(AiImageServiceImpl.class)).thenReturn(self);
            imageService.drawImage(USER_ID, reqVO);
        }
        // 断言：请求参数逐个写入 Options，并记录 API 密钥
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getUserId(), USER_ID)
                && Objects.equals(image.getMode(), AiImageModeEnum.DRAW.getMode())
                && Objects.equals(image.getModelId(), MODEL_ID) && Objects.equals(image.getModel(), model.getModel())
                && Objects.equals(image.getApiKeyId(), API_KEY_ID)
                && Objects.equals(image.getNegativePrompt(), "文字")
                && Objects.equals(image.getWidth(), 1024) && Objects.equals(image.getHeight(), 768)
                && Objects.equals(image.getInputImages(), Collections.singletonList(
                        new AiImageDO.InputImage(AiImageInputRoleEnum.REFERENCE.getRole(), "https://file/ref.png")))
                && Objects.equals(image.getOptions().getSeed(), 42L) && Objects.equals(image.getOptions().getSteps(), 30)
                && Objects.equals(image.getOptions().getCfgScale(), 7.5)
                && Objects.equals(image.getOptions().getAspectRatio(), "4:3")
                && image.getSeedreamOptions() == null
                && Objects.equals(image.getStatus(), IN_PROGRESS) && Objects.equals(image.getProgress(), 0)
                && Boolean.FALSE.equals(image.getPublicStatus())));
        verify(self).executeDrawImage(any(AiImageDO.class), eq(imageModel));
    }

    @Test
    public void testDrawImage_seedream() {
        // mock 方法
        mockModel(AiPlatformEnum.BYTEDANCE, "doubao-seedream-4-5-251128");
        ArkService arkService = mock(ArkService.class);
        when(modelService.getArkService(MODEL_ID)).thenReturn(arkService);
        AiImageServiceImpl self = mock(AiImageServiceImpl.class);

        // 调用
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(AiImageServiceImpl.class)).thenReturn(self);
            imageService.drawImage(USER_ID, buildDrawReqVO().setWidth(null).setHeight(null).setSizeLevel("2K"));
        }
        // 断言：Seedream 只写 seedreamOptions；宽高未知记为 0（ai_image.width、height 不允许为空）
        verify(imageMapper).insert(argThat((AiImageDO image) -> image.getOptions() == null
                && Objects.equals(image.getWidth(), 0) && Objects.equals(image.getHeight(), 0)
                && Objects.equals(image.getSeedreamOptions().getSize(), "2K")
                && Objects.equals(image.getSeedreamOptions().getOptimizePromptMode(), "standard")
                && Boolean.FALSE.equals(image.getSeedreamOptions().getWatermark())));
        verify(self).executeSeedream(any(AiImageDO.class), eq(arkService));
        verify(modelService, never()).getImageModel(any());
    }

    @Test
    public void testDrawImage_midjourneyModel() {
        // mock 方法
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");

        // 调用，并断言异常
        assertServiceException(() -> imageService.drawImage(USER_ID, buildDrawReqVO()), IMAGE_MODEL_NOT_SUPPORTED,
                "绘图模型", "Midjourney 模型请使用 Midjourney 生成");
        verifyNoInteractions(imageMapper);
    }

    @Test
    public void testDrawImage_sizeEmpty() {
        // mock 方法：通用模型
        mockModel(AiPlatformEnum.SILICONFLOW, "Kwai-Kolors/Kolors");

        // 调用，并断言异常：通用模型的宽高必填
        assertServiceException(() -> imageService.drawImage(USER_ID, buildDrawReqVO().setHeight(null)),
                IMAGE_OPTION_INVALID, "width/height", "图片宽高不能为空");
        // 断言：不保存 image，也不创建客户端
        verifyNoInteractions(imageMapper);
        verify(modelService, never()).getImageModel(any());
    }

    @Test
    public void testDrawImage_modelPlatformMismatch() {
        // mock 方法：模型的平台为 OpenAI，API 密钥的平台为硅基流动
        mockModel(AiPlatformEnum.OPENAI, "dall-e-3");
        when(modelService.getImageModel(MODEL_ID)).thenThrow(exception(IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.OPENAI.getPlatform(), AiPlatformEnum.SILICONFLOW.getPlatform()));

        // 调用，并断言异常
        assertServiceException(() -> imageService.drawImage(USER_ID, buildDrawReqVO()), IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.OPENAI.getPlatform(), AiPlatformEnum.SILICONFLOW.getPlatform());
        // 断言：不保存 image，更不会请求绘图平台
        verifyNoInteractions(imageMapper, fileApi);
    }

    @Test
    public void testExecuteDrawImage_siliconFlowRequest() {
        // mock 硅基流动接口：断言实际请求体
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        server.expect(requestTo(endsWith("v1/images/generations")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"prompt\":\"一只猫\",\"model\":\"Kwai-Kolors/Kolors\",\"image_size\":\"1024x768\","
                        + "\"negative_prompt\":\"文字\",\"seed\":42,\"num_inference_steps\":30,\"guidance_scale\":7.5,"
                        + "\"image\":\"https://file/ref.png\"}"))
                .andRespond(withSuccess("{\"created\":1,\"data\":[{\"b64_json\":\"aGVsbG8=\"}]}",
                        MediaType.APPLICATION_JSON));
        SiliconFlowImageModel imageModel = new SiliconFlowImageModel(
                new SiliconFlowImageApi("http://127.0.0.1", "sk-test", restClientBuilder));
        // mock 数据
        AiImageDO image = new AiImageDO().setId(IMAGE_ID).setPlatform(AiPlatformEnum.SILICONFLOW.getPlatform())
                .setModel("Kwai-Kolors/Kolors").setPrompt("一只猫").setNegativePrompt("文字").setWidth(1024).setHeight(768)
                .setInputImages(Collections.singletonList(
                        new AiImageDO.InputImage(AiImageInputRoleEnum.REFERENCE.getRole(), "https://file/ref.png")))
                .setOptions(new AiImageDO.Options().setSeed(42L).setSteps(30).setCfgScale(7.5));
        // mock 方法
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/1.png");

        // 调用
        imageService.executeDrawImage(image, imageModel);
        // 断言：只更新进行中的记录
        server.verify();
        verify(imageMapper).updateByIdAndStatus(eq(IMAGE_ID), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())
                        && Objects.equals(update.getProgress(), 100)
                        && Objects.equals(update.getPicUrl(), "https://file/1.png") && update.getFinishTime() != null));
    }

    @Test
    public void testExecuteDrawImage_fail() {
        // mock 方法
        ImageModel imageModel = mock(ImageModel.class);
        when(imageModel.call(any())).thenThrow(new IllegalStateException("上游超时"));
        AiImageDO image = new AiImageDO().setId(IMAGE_ID).setPlatform(AiPlatformEnum.OPENAI.getPlatform())
                .setModel("dall-e-3").setPrompt("一只猫").setWidth(1024).setHeight(1024);

        // 调用
        imageService.executeDrawImage(image, imageModel);
        // 断言
        verifyImageFail(IMAGE_ID, "上游超时");
    }

    @Test
    public void testBuildImageOptions_openAi() {
        AiImageDO image = new AiImageDO().setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("dall-e-3")
                .setWidth(1024).setHeight(1792).setOptions(new AiImageDO.Options().setQuality("hd").setStyle("vivid"));

        OpenAiImageOptions options = (OpenAiImageOptions) AiImageUtils.buildImageOptions(image);
        assertEquals("dall-e-3", options.getModel());
        assertEquals("hd", options.getQuality());
        assertEquals("b64_json", options.getResponseFormat());
        assertEquals(1024, options.getWidth());
        assertEquals(1792, options.getHeight());
        assertNull(options.getStyle()); // 历史 style 不提交
    }

    @Test
    public void testBuildImageOptions_gptImage() {
        AiImageDO image = new AiImageDO().setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("gpt-image-1")
                .setWidth(1536).setHeight(1024).setOptions(new AiImageDO.Options().setQuality("high"));

        OpenAiImageOptions options = (OpenAiImageOptions) AiImageUtils.buildImageOptions(image);
        assertEquals("gpt-image-1", options.getModel());
        assertEquals("high", options.getQuality());
        assertNull(options.getResponseFormat()); // gpt-image 系列不支持 response_format
    }

    @Test
    public void testBuildImageOptions_stabilityDefault() {
        AiImageDO image = new AiImageDO().setPlatform(AiPlatformEnum.STABILITYAI.getPlatform())
                .setModel("stable-diffusion-xl-1024-v1-0").setWidth(1024).setHeight(1024);

        StabilityAiImageOptions options = (StabilityAiImageOptions) AiImageUtils.buildImageOptions(image);
        assertEquals(0L, options.getSeed());
        assertEquals(30, options.getSteps());
        assertEquals(7F, options.getCfgScale());
        assertEquals("K_DPMPP_2M", options.getSampler());
        assertEquals("NONE", options.getClipGuidancePreset());
    }

    @Test
    public void testBuildImageOptions_stability() {
        AiImageDO image = new AiImageDO().setPlatform(AiPlatformEnum.STABILITYAI.getPlatform())
                .setModel("stable-diffusion-xl-1024-v1-0").setWidth(1024).setHeight(1024)
                .setOptions(new AiImageDO.Options().setSeed(7L).setSteps(40).setCfgScale(12.0)
                        .setSampler("DDIM").setStylePreset("anime").setClipGuidancePreset("FAST_BLUE"));

        StabilityAiImageOptions options = (StabilityAiImageOptions) AiImageUtils.buildImageOptions(image);
        assertEquals(7L, options.getSeed());
        assertEquals(40, options.getSteps());
        assertEquals(12F, options.getCfgScale());
        assertEquals("DDIM", options.getSampler());
        assertEquals("anime", options.getStylePreset());
        assertEquals("FAST_BLUE", options.getClipGuidancePreset());
    }

    // ========== Seedream ==========

    @Test
    public void testBuildSeedreamRequest() {
        AiImageDO image = buildSeedreamImage(Arrays.asList("https://file/1.png", "https://file/2.png"));

        GenerateImagesRequest request = AiImageUtils.buildSeedreamRequest(image);
        assertEquals("doubao-seedream-4-5-251128", request.getModel());
        assertEquals("一只猫", request.getPrompt());
        assertEquals("2K", request.getSize());
        assertEquals(Boolean.FALSE, request.getWatermark());
        assertEquals(ResponseFormat.Url, request.getResponseFormat());
        assertNull(request.getSequentialImageGeneration()); // 不传组图参数：官方默认单图，Seedream 5.0 flash 不支持该参数
        assertEquals("standard", request.getOptimizePromptOptions().getMode());
        assertEquals(Arrays.asList("https://file/1.png", "https://file/2.png"), request.getImage());
    }

    @Test
    public void testBuildSeedreamRequest_noReference() {
        GenerateImagesRequest request = AiImageUtils.buildSeedreamRequest(buildSeedreamImage(null));
        assertNull(request.getImage());
    }

    @Test
    public void testExecuteSeedream_success() throws IOException {
        // mock 方法
        String imageUrl = startImageServer() + "/result.png";
        ArkService arkService = mock(ArkService.class);
        when(arkService.generateImages(any(GenerateImagesRequest.class))).thenReturn(buildSeedreamResponse(imageUrl));
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/seedream.png");

        // 调用
        imageService.executeSeedream(buildSeedreamImage(null), arkService);
        // 断言：转存结果，并回写尺寸
        verify(imageMapper).updateByIdAndStatus(eq(IMAGE_ID), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())
                        && Objects.equals(update.getPicUrl(), "https://file/seedream.png")
                        && Objects.equals(update.getWidth(), 2048) && Objects.equals(update.getHeight(), 2048)
                        && Objects.equals(update.getSeedreamOptions().getResultSize(), "2048x2048")));
    }

    @Test
    public void testExecuteSeedream_error() {
        // mock 方法：上游返回错误
        ArkService arkService = mock(ArkService.class);
        ImagesResponse.Error error = new ImagesResponse.Error();
        error.setCode("InputTextSensitiveContentDetected");
        error.setMessage("内容不合规");
        ImagesResponse response = new ImagesResponse();
        response.setError(error);
        when(arkService.generateImages(any(GenerateImagesRequest.class))).thenReturn(response);

        // 调用
        imageService.executeSeedream(buildSeedreamImage(null), arkService);
        // 断言：记录错误码与错误信息，不转存
        verify(imageMapper).updateByIdAndStatus(eq(IMAGE_ID), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && Objects.equals(update.getErrorMessage(), "内容不合规")
                        && Objects.equals(update.getSeedreamOptions().getErrorCode(), "InputTextSensitiveContentDetected")));
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testExecuteSeedream_transferFail() throws IOException {
        // mock 方法：上游成功，但转存失败
        String imageUrl = startImageServer() + "/result.png";
        ArkService arkService = mock(ArkService.class);
        when(arkService.generateImages(any(GenerateImagesRequest.class))).thenReturn(buildSeedreamResponse(imageUrl));
        when(fileApi.createFile(any(byte[].class))).thenThrow(new IllegalStateException("存储不可用"));

        // 调用
        imageService.executeSeedream(buildSeedreamImage(null), arkService);
        // 断言：置为失败，不回退到临时地址
        verifyImageFail(IMAGE_ID, "结果转存失败：存储不可用");
    }

    private static ImagesResponse buildSeedreamResponse(String imageUrl) {
        ImagesResponse.Image item = new ImagesResponse.Image();
        item.setUrl(imageUrl);
        item.setSize("2048x2048");
        ImagesResponse response = new ImagesResponse();
        response.setData(Collections.singletonList(item));
        return response;
    }

    private static AiImageDO buildSeedreamImage(List<String> referenceUrls) {
        List<AiImageDO.InputImage> inputImages = referenceUrls == null ? null : referenceUrls.stream()
                .map(url -> new AiImageDO.InputImage(AiImageInputRoleEnum.REFERENCE.getRole(), url)).toList();
        return new AiImageDO().setId(IMAGE_ID).setPlatform(AiPlatformEnum.BYTEDANCE.getPlatform())
                .setModel("doubao-seedream-4-5-251128").setPrompt("一只猫").setInputImages(inputImages)
                .setSeedreamOptions(new AiImageDO.SeedreamOptions().setSize("2K").setWatermark(false)
                        .setOptimizePromptMode("standard"));
    }

    // ========== midjourneyImagine ==========

    @Test
    public void testMidjourneyImagine_success() {
        // mock 方法
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.imagine(any(MidjourneyApi.ImagineRequest.class))).thenReturn(submitSuccess());
        mockInsertId();

        // 调用
        Long id = imageService.midjourneyImagine(USER_ID, buildImagineReqVO().setStylize(250).setSeed(-1L));
        // 断言：拼接后的提示词提交到中转；随机种子不拼接
        assertEquals(IMAGE_ID, id);
        verify(midjourneyApi).imagine(argThat(request -> Objects.equals(request.getPrompt(), "一只猫 --ar 4:3 --v 7 --s 250")
                && request.getBase64Array().isEmpty() && request.getNotifyHook() == null));
        // 断言：保存原始提示词、参数与拼接结果
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getPrompt(), "一只猫")
                && Objects.equals(image.getMode(), AiImageModeEnum.MIDJOURNEY_IMAGINE.getMode())
                && Objects.equals(image.getApiKeyId(), API_KEY_ID)
                && Objects.equals(image.getWidth(), 4) && Objects.equals(image.getHeight(), 3)
                && Objects.equals(image.getMidjourneyOptions().getVersion(), "--v 7")
                && Objects.equals(image.getMidjourneyOptions().getStylize(), 250)
                && image.getMidjourneyOptions().getSeed() == null
                && Objects.equals(image.getMidjourneyOptions().getFinalPrompt(), "一只猫 --ar 4:3 --v 7 --s 250")));
        verify(imageMapper).updateById(argThat((AiImageDO update) -> Objects.equals(update.getId(), IMAGE_ID)
                && Objects.equals(update.getTaskId(), NEW_TASK_ID)));
    }

    @Test
    public void testMidjourneyImagine_imagePrompt() throws IOException {
        // mock 方法
        String baseUrl = startImageServer();
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.imagine(any(MidjourneyApi.ImagineRequest.class))).thenReturn(submitSuccess());
        mockInsertId();

        // 调用
        imageService.midjourneyImagine(USER_ID, buildImagineReqVO().setMode(AiImageModeEnum.MIDJOURNEY_IMAGE.getMode())
                .setReferenceImageUrls(Arrays.asList(baseUrl + "/1.png", baseUrl + "/2.png")).setImageWeight(1.5));
        // 断言：参考图全部转 base64 提交，--iw 拼接到提示词
        verify(midjourneyApi).imagine(argThat(request -> request.getBase64Array().size() == 2
                && request.getBase64Array().get(0).startsWith("data:image/png;base64,")
                && request.getPrompt().endsWith("--v 7 --iw 1.5")));
        verify(imageMapper).insert(argThat((AiImageDO image) -> image.getInputImages().size() == 2
                && image.getInputImages().stream().allMatch(item ->
                        AiImageInputRoleEnum.REFERENCE.getRole().equals(item.getRole()))));
    }

    @Test
    public void testMidjourneyImagine_imagePromptWithoutReference() {
        assertServiceException(() -> imageService.midjourneyImagine(USER_ID, buildImagineReqVO()
                .setMode(AiImageModeEnum.MIDJOURNEY_IMAGE.getMode())), IMAGE_REFERENCE_COUNT_EXCEED, "1-5", 0);
        verifyNoInteractions(imageMapper, modelService);
    }

    @Test
    public void testMidjourneyImagine_crefNotConsistency() {
        assertServiceException(() -> imageService.midjourneyImagine(USER_ID, buildImagineReqVO()
                .setCrefUrl("https://file/cref.png")), IMAGE_OPTION_INVALID,
                "crefUrl/srefUrl", "只有一致性需要、且至少需要 1 张角色或风格参考");
        verifyNoInteractions(imageMapper, modelService);
    }

    @Test
    public void testMidjourneyImagine_invalidMode() {
        assertServiceException(() -> imageService.midjourneyImagine(USER_ID, buildImagineReqVO()
                .setMode(AiImageModeEnum.MIDJOURNEY_BLEND.getMode())), IMAGE_OPTION_INVALID,
                "mode", "只支持文生图、图生图、一致性");
        verifyNoInteractions(imageMapper, modelService);
    }

    @Test
    public void testMidjourneyImagine_submitFail() {
        // mock 方法：Midjourney Proxy 拒绝
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.imagine(any(MidjourneyApi.ImagineRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse("4", "quota_not_enough", null, null));
        mockInsertId();

        // 调用：不抛出异常，保留失败记录
        assertEquals(IMAGE_ID, imageService.midjourneyImagine(USER_ID, buildImagineReqVO()));
        // 断言
        verifyImageFail(IMAGE_ID, "Midjourney 提交失败：账户余额不足");
        verify(imageMapper, never()).updateById(any(AiImageDO.class));
    }

    @Test
    public void testBuildMidjourneyPrompt_allParams() {
        AiMidjourneyImagineReqVO reqVO = buildImagineReqVO().setMode(AiImageModeEnum.MIDJOURNEY_CONSISTENCY.getMode())
                .setVersion("--niji 6").setQuality(0.5).setTile(true).setRaw(true).setHd(true)
                .setChaos(10).setStylize(100).setSeed(123L)
                .setCrefUrl("https://file/c.png").setCharacterWeight(80)
                .setSrefUrl("https://file/s.png").setStyleWeight(200).setStyleVersion(4)
                .setNegativePrompt("文字");

        assertEquals("一只猫 --ar 4:3 --niji 6 --q 0.5 --tile --raw --hd --c 10 --s 100 --seed 123"
                        + " --cref https://file/c.png --cw 80 --sref https://file/s.png --sw 200 --sv 4 --no 文字",
                AiImageUtils.buildMidjourneyPrompt(reqVO));
    }

    @Test
    public void testBuildMidjourneyPrompt_arInPrompt() {
        assertEquals("一只猫 --ar 16:9 --v 7", AiImageUtils.buildMidjourneyPrompt(
                buildImagineReqVO().setPrompt("一只猫 --ar 16:9")));
    }

    private static AiMidjourneyImagineReqVO buildImagineReqVO() {
        return new AiMidjourneyImagineReqVO().setModelId(MODEL_ID).setMode(AiImageModeEnum.MIDJOURNEY_IMAGINE.getMode())
                .setPrompt("一只猫").setVersion("--v 7").setAspectRatio("4:3");
    }

    // ========== midjourneyBlend、midjourneySwapFace ==========

    @Test
    public void testMidjourneyBlend_success() throws IOException {
        // mock 方法
        String baseUrl = startImageServer();
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.blend(any(MidjourneyApi.BlendRequest.class))).thenReturn(submitSuccess());
        mockInsertId();

        // 调用
        imageService.midjourneyBlend(USER_ID, new AiMidjourneyBlendReqVO().setModelId(MODEL_ID)
                .setImageUrls(Arrays.asList(baseUrl + "/1.png", baseUrl + "/2.png")).setDimensions("PORTRAIT"));
        // 断言
        verify(midjourneyApi).blend(argThat(request -> request.base64Array().size() == 2
                && Objects.equals(request.dimensions(), "PORTRAIT")));
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getMode(), AiImageModeEnum.MIDJOURNEY_BLEND.getMode())
                && Objects.equals(image.getMidjourneyOptions().getAspectRatio(), "2:3")
                && Objects.equals(image.getWidth(), 2) && Objects.equals(image.getHeight(), 3)
                && image.getInputImages().stream().allMatch(item -> AiImageInputRoleEnum.BLEND.getRole().equals(item.getRole()))));
    }

    @Test
    public void testMidjourneySwapFace_downloadFail() {
        // mock 方法
        mockModel(AiPlatformEnum.MIDJOURNEY, "midjourney");
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        mockInsertId();

        // 调用：图片下载失败，记录失败原因
        imageService.midjourneySwapFace(USER_ID, new AiMidjourneySwapFaceReqVO().setModelId(MODEL_ID)
                .setSourceUrl("http://127.0.0.1:1/source.png").setTargetUrl("http://127.0.0.1:1/target.png"));
        // 断言：输入图片的顺序为来源人脸 → 目标图；没有比例，宽高记为 0
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getMode(), AiImageModeEnum.MIDJOURNEY_SWAP_FACE.getMode())
                && Objects.equals(image.getWidth(), 0) && Objects.equals(image.getHeight(), 0)
                && Objects.equals(image.getInputImages().get(0).getRole(), AiImageInputRoleEnum.SWAP_SOURCE.getRole())
                && Objects.equals(image.getInputImages().get(1).getRole(), AiImageInputRoleEnum.SWAP_TARGET.getRole())));
        verify(imageMapper).updateByIdAndStatus(eq(IMAGE_ID), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && update.getErrorMessage().startsWith("Midjourney 提交失败：")));
        verifyNoInteractions(midjourneyApi);
    }

    // ========== midjourneyAction ==========

    @Test
    public void testMidjourneyAction_success() {
        // mock 数据
        AiImageDO source = buildSourceImage(USER_ID);
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(source);
        // mock 方法：沿用来源记录的 API 密钥
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        when(midjourneyApi.action(any(MidjourneyApi.ActionRequest.class))).thenReturn(submitSuccess());

        // 调用
        imageService.midjourneyAction(USER_ID, buildActionReqVO());
        // 断言：提交来源记录的任务编号与按钮
        verify(midjourneyApi).action(argThat(request -> Objects.equals(request.getTaskId(), "task-a")
                && Objects.equals(request.getCustomId(), CUSTOM_ID)));
        // 断言：子任务继承来源的模型、密钥、请求参数
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getParentId(), IMAGE_ID)
                && Objects.equals(image.getMode(), AiImageModeEnum.MIDJOURNEY_ACTION.getMode())
                && Objects.equals(image.getModelId(), MODEL_ID) && Objects.equals(image.getApiKeyId(), API_KEY_ID)
                && Objects.equals(image.getPrompt(), "一只猫")
                && Objects.equals(image.getMidjourneyOptions().getVersion(), "--v 7")
                && Objects.equals(image.getMidjourneyOptions().getCustomId(), CUSTOM_ID)
                && Objects.equals(image.getMidjourneyOptions().getActionLabel(), "U1")
                && image.getMidjourneyOptions().getButtons() == null
                && Objects.equals(image.getStatus(), IN_PROGRESS)));
        verify(modelService, never()).getMidjourneyApi(any(Long.class));
    }

    @Test
    public void testMidjourneyAction_legacyImageWithoutApiKey() {
        // mock 数据：历史记录没有绑定 API 密钥
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID).setApiKeyId(null));
        // mock 方法：沿用模型当前的密钥
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(OTHER_API_KEY_ID));
        mockMidjourneyApiByKey(OTHER_API_KEY_ID, otherMidjourneyApi);
        when(otherMidjourneyApi.action(any(MidjourneyApi.ActionRequest.class))).thenReturn(submitSuccess());

        // 调用
        imageService.midjourneyAction(USER_ID, buildActionReqVO());
        // 断言
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getApiKeyId(), OTHER_API_KEY_ID)));
    }

    @Test
    public void testMidjourneyAction_notOwner() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID + 1));

        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO()), IMAGE_NOT_EXISTS);
        verify(imageMapper, never()).insert(any(AiImageDO.class));
        verifyNoInteractions(modelService, apiKeyService);
    }

    @Test
    public void testMidjourneyAction_sourceNotSuccess() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID).setStatus(IN_PROGRESS));

        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO()), IMAGE_MIDJOURNEY_SOURCE_INVALID);
        verify(imageMapper, never()).insert(any(AiImageDO.class));
    }

    @Test
    public void testMidjourneyAction_customIdNotExists() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID));

        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO().setCustomId("MJ::JOB::other")),
                IMAGE_CUSTOM_ID_NOT_EXISTS, "MJ::JOB::other");
        verify(imageMapper, never()).insert(any(AiImageDO.class));
        verifyNoInteractions(midjourneyApi);
    }

    @Test
    public void testMidjourneyAction_inpaintNotSupported() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID));

        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO().setCustomId(INPAINT_CUSTOM_ID)),
                IMAGE_MIDJOURNEY_ACTION_NOT_SUPPORTED, "Vary (Region)");
        verify(imageMapper, never()).insert(any(AiImageDO.class));
    }

    // ========== midjourneyModal ==========

    @Test
    public void testMidjourneyModal_actionThenModal() throws IOException {
        // mock 数据
        String maskUrl = startImageServer() + "/mask.png";
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID));
        // mock 方法：先执行 Inpaint 按钮，再用返回的任务编号提交蒙版
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        when(midjourneyApi.action(any(MidjourneyApi.ActionRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse("21", "窗口等待", null, "task-modal"));
        when(midjourneyApi.modal(any(MidjourneyApi.ModalRequest.class))).thenReturn(submitSuccess());
        mockInsertId();

        // 调用
        imageService.midjourneyModal(USER_ID, new AiMidjourneyModalReqVO().setId(IMAGE_ID)
                .setPrompt("换成红色的帽子").setMaskUrl(maskUrl));
        // 断言：提交顺序与参数
        verify(midjourneyApi).action(argThat(request -> Objects.equals(request.getTaskId(), "task-a")
                && Objects.equals(request.getCustomId(), INPAINT_CUSTOM_ID)));
        verify(midjourneyApi).modal(argThat(request -> Objects.equals(request.taskId(), "task-modal")
                && Objects.equals(request.prompt(), "换成红色的帽子")
                && request.maskBase64().startsWith("data:image/png;base64,")));
        // 断言：子任务记录蒙版与流程快照
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getParentId(), IMAGE_ID)
                && Objects.equals(image.getMode(), AiImageModeEnum.MIDJOURNEY_MODAL.getMode())
                && Objects.equals(image.getInputImages(), Collections.singletonList(
                        new AiImageDO.InputImage(AiImageInputRoleEnum.MASK.getRole(), maskUrl)))
                && Objects.equals(image.getMidjourneyOptions().getModalProtocol(),
                        MidjourneyApi.ModalProtocolEnum.ACTION_THEN_MODAL.name())));
        verify(imageMapper).updateById(argThat((AiImageDO update) -> Objects.equals(update.getTaskId(), NEW_TASK_ID)));
    }

    @Test
    public void testMidjourneyModal_noInpaintButton() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildSourceImage(USER_ID).setMidjourneyOptions(
                new AiImageDO.MidjourneyOptions().setButtons(Collections.singletonList(
                        new MidjourneyApi.Button(CUSTOM_ID, null, "U1", null, null)))));

        assertServiceException(() -> imageService.midjourneyModal(USER_ID, new AiMidjourneyModalReqVO().setId(IMAGE_ID)
                .setPrompt("换成红色的帽子").setMaskUrl("https://file/mask.png")), IMAGE_CUSTOM_ID_NOT_EXISTS, "Inpaint");
        verify(imageMapper, never()).insert(any(AiImageDO.class));
    }

    private AiImageDO buildSourceImage(Long userId) {
        return new AiImageDO().setId(IMAGE_ID).setUserId(userId).setPrompt("一只猫")
                .setMode(AiImageModeEnum.MIDJOURNEY_IMAGINE.getMode())
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setModelId(MODEL_ID).setModel("midjourney")
                .setApiKeyId(API_KEY_ID).setWidth(4).setHeight(3)
                .setStatus(AiImageStatusEnum.SUCCESS.getStatus()).setPublicStatus(true).setTaskId("task-a")
                .setMidjourneyOptions(new AiImageDO.MidjourneyOptions().setVersion("--v 7").setAspectRatio("4:3")
                        .setButtons(Arrays.asList(new MidjourneyApi.Button(CUSTOM_ID, null, "U1", null, null),
                                new MidjourneyApi.Button(INPAINT_CUSTOM_ID, "🖌️", "Vary (Region)", null, null))));
    }

    private static AiMidjourneyActionReqVO buildActionReqVO() {
        return new AiMidjourneyActionReqVO().setId(IMAGE_ID).setCustomId(CUSTOM_ID);
    }

    // ========== midjourneySync ==========

    @Test
    public void testMidjourneySync_empty() {
        mockInProgressImages(buildSyncImage(1L, API_KEY_ID, null)); // 没有任务编号（提交失败前）的跳过

        assertEquals(0, imageService.midjourneySync());
        verifyNoInteractions(modelService, apiKeyService);
    }

    @Test
    public void testMidjourneySync_groupByApiKey() {
        // mock 数据：两个 API 密钥，相同的任务编号
        mockInProgressImages(buildSyncImage(1L, API_KEY_ID, "task-same"), buildSyncImage(2L, OTHER_API_KEY_ID, "task-same"));
        // mock 方法：两个中转返回不同的结果
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-same"))).thenReturn(Collections.singletonList(
                buildNotify("task-same", "IN_PROGRESS", "45%", null)));
        mockMidjourneyApiByKey(OTHER_API_KEY_ID, otherMidjourneyApi);
        when(otherMidjourneyApi.getTaskList(Set.of("task-same"))).thenReturn(Collections.singletonList(
                buildNotify("task-same", "FAILURE", null, "被拒绝")));

        // 调用，并断言：各自使用自己中转的结果，不串
        assertEquals(2, imageService.midjourneySync());
        verify(imageMapper).updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                update.getStatus() == null && Objects.equals(update.getProgress(), 45)
                        && Objects.equals(update.getMidjourneyOptions().getProviderStatus(), "IN_PROGRESS")));
        verify(imageMapper).updateByIdAndStatus(eq(2L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && Objects.equals(update.getErrorMessage(), "被拒绝")));
    }

    @Test
    public void testMidjourneySync_legacyWithoutApiKey() {
        // mock 数据：历史记录没有 API 密钥；其中一条的模型已不存在（例如说 model_id = 0）
        mockInProgressImages(buildSyncImage(1L, null, "task-a").setModelId(MODEL_ID),
                buildSyncImage(2L, null, "task-b").setModelId(0L));
        when(modelService.getModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(API_KEY_ID));
        when(modelService.getModel(0L)).thenReturn(null);
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a"))).thenReturn(Collections.singletonList(
                buildNotify("task-a", "SUBMITTED", null, null)));

        // 调用，并断言：模型不存在的跳过，交给超时处理
        assertEquals(1, imageService.midjourneySync());
        verify(imageMapper).updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), any(AiImageDO.class));
        verify(imageMapper, never()).updateByIdAndStatus(eq(2L), any(), any());
    }

    @Test
    public void testMidjourneySync_apiKeyFail() {
        // mock 数据
        mockInProgressImages(buildSyncImage(1L, API_KEY_ID, "task-a"), buildSyncImage(2L, OTHER_API_KEY_ID, "task-b"));
        // mock 方法：API_KEY_ID 的中转不可用
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID);
        when(apiKeyService.validateApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getMidjourneyApi(apiKey)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a"))).thenThrow(new IllegalStateException("[midjourney-api] 调用失败！"));
        mockMidjourneyApiByKey(OTHER_API_KEY_ID, otherMidjourneyApi);
        when(otherMidjourneyApi.getTaskList(Set.of("task-b"))).thenReturn(Collections.singletonList(
                buildNotify("task-b", "MODAL", null, null)));

        // 调用，并断言：不影响其它密钥；MODAL 仍在进行中
        assertEquals(1, imageService.midjourneySync());
        verify(imageMapper).updateByIdAndStatus(eq(2L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                update.getStatus() == null && update.getProgress() == null
                        && Objects.equals(update.getMidjourneyOptions().getProviderStatus(), "MODAL")));
        verify(imageMapper, never()).updateByIdAndStatus(eq(1L), any(), any());
    }

    @Test
    public void testMidjourneySync_cancelAndUnknownStatus() {
        // mock 数据
        mockInProgressImages(buildSyncImage(1L, API_KEY_ID, "task-cancel"), buildSyncImage(2L, API_KEY_ID, "task-unknown"));
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-cancel", "task-unknown"))).thenReturn(Arrays.asList(
                buildNotify("task-cancel", "CANCEL", null, null),
                buildNotify("task-unknown", "SOMETHING_NEW", null, null)));

        // 调用，并断言：CANCEL 视为失败；未知状态不更新状态
        assertEquals(2, imageService.midjourneySync());
        verify(imageMapper).updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && Objects.equals(update.getErrorMessage(), "任务已取消")));
        verify(imageMapper).updateByIdAndStatus(eq(2L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                update.getStatus() == null && update.getFinishTime() == null));
    }

    @Test
    public void testMidjourneySync_successTransfer() throws IOException {
        // mock 数据
        String imageUrl = startImageServer() + "/mj.png";
        mockInProgressImages(buildSyncImage(1L, API_KEY_ID, "task-a"));
        mockMidjourneyApiByKey(API_KEY_ID, midjourneyApi);
        List<MidjourneyApi.Button> buttons = Collections.singletonList(new MidjourneyApi.Button(CUSTOM_ID, null, "U1", null, null));
        when(midjourneyApi.getTaskList(Set.of("task-a"))).thenReturn(Collections.singletonList(new MidjourneyApi.Notify(
                "task-a", null, "SUCCESS", null, null, null, null, null, null, null, imageUrl, "100%", null, buttons)));
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://file/mj.png");

        // 调用，并断言：转存图片，并记录按钮
        assertEquals(1, imageService.midjourneySync());
        verify(imageMapper).updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())
                        && Objects.equals(update.getProgress(), 100)
                        && Objects.equals(update.getPicUrl(), "https://file/mj.png")
                        && Objects.equals(update.getMidjourneyOptions().getButtons(), buttons)));
    }

    // ========== midjourneyNotify ==========

    @Test
    public void testMidjourneyNotify_sameApiKey() {
        // mock 数据：同一 API 密钥复用了任务编号（中转返回 21）
        when(imageMapper.selectListByTaskId("task-a")).thenReturn(Arrays.asList(
                buildSyncImage(1L, API_KEY_ID, "task-a"), buildSyncImage(2L, API_KEY_ID, "task-a")));

        // 调用
        imageService.midjourneyNotify(buildNotify("task-a", "FAILURE", "0%", "生成失败"));
        // 断言：同一个上游任务，一起更新
        verify(imageMapper).updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && Objects.equals(update.getErrorMessage(), "生成失败")));
        verify(imageMapper).updateByIdAndStatus(eq(2L), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())));
    }

    @Test
    public void testMidjourneyNotify_multipleApiKeys() {
        // mock 数据：不同 API 密钥（例如说：不同租户）的记录，任务编号相同
        when(imageMapper.selectListByTaskId("task-a")).thenReturn(Arrays.asList(
                buildSyncImage(1L, API_KEY_ID, "task-a"), buildSyncImage(2L, OTHER_API_KEY_ID, "task-a")));

        // 调用
        imageService.midjourneyNotify(buildNotify("task-a", "FAILURE", "0%", "生成失败"));
        // 断言：无法确定归属，不更新任何记录（由按密钥同步的 Job 更新）
        verify(imageMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testMidjourneyNotify_otherApiKeyFinished() {
        // mock 数据：其它 API 密钥的同一任务编号已结束，本密钥的记录仍在进行中
        when(imageMapper.selectListByTaskId("task-a")).thenReturn(Arrays.asList(
                buildSyncImage(1L, API_KEY_ID, "task-a").setStatus(AiImageStatusEnum.SUCCESS.getStatus()),
                buildSyncImage(2L, OTHER_API_KEY_ID, "task-a")));

        // 调用：已结束任务的迟到回调
        imageService.midjourneyNotify(buildNotify("task-a", "FAILURE", "0%", "生成失败"));
        // 断言：无法确定归属，不更新进行中的记录
        verify(imageMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testMidjourneyNotify_notInProgress() {
        // mock 数据：已超时失败的记录
        when(imageMapper.selectListByTaskId("task-a")).thenReturn(Collections.singletonList(
                buildSyncImage(1L, API_KEY_ID, "task-a").setStatus(AiImageStatusEnum.FAIL.getStatus())));

        // 调用：迟到的回调
        imageService.midjourneyNotify(buildNotify("task-a", "SUCCESS", "100%", null));
        // 断言：不再改变状态
        verify(imageMapper, never()).updateByIdAndStatus(any(), any(), any());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testMidjourneyNotify_notExists() {
        // 调用：不存在的任务
        imageService.midjourneyNotify(buildNotify("task-x", "SUCCESS", "100%", null));

        verify(imageMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testParseProgress() {
        assertEquals(45, AiImageUtils.parseProgress("45%"));
        assertEquals(0, AiImageUtils.parseProgress("0%"));
        assertNull(AiImageUtils.parseProgress(null));
        assertNull(AiImageUtils.parseProgress("abc"));
    }

    private void mockInProgressImages(AiImageDO... images) {
        when(imageMapper.selectListByStatusAndPlatform(IN_PROGRESS, AiPlatformEnum.MIDJOURNEY.getPlatform()))
                .thenReturn(Arrays.asList(images));
    }

    private static AiImageDO buildSyncImage(Long id, Long apiKeyId, String taskId) {
        return new AiImageDO().setId(id).setApiKeyId(apiKeyId).setTaskId(taskId)
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setStatus(IN_PROGRESS);
    }

    private static MidjourneyApi.Notify buildNotify(String taskId, String status, String progress, String failReason) {
        return new MidjourneyApi.Notify(taskId, null, status, null, null, null, null,
                null, null, null, null, progress, failReason, null);
    }

    // ========== timeoutImage ==========

    @Test
    public void testTimeoutImage() {
        // mock 数据
        when(imageMapper.selectListByStatusAndCreateTimeLt(eq(IN_PROGRESS), any(LocalDateTime.class))).thenReturn(Arrays.asList(
                new AiImageDO().setId(1L), new AiImageDO().setId(2L)));
        when(imageMapper.updateByIdAndStatus(eq(1L), eq(IN_PROGRESS), any(AiImageDO.class))).thenReturn(1);
        when(imageMapper.updateByIdAndStatus(eq(2L), eq(IN_PROGRESS), any(AiImageDO.class))).thenReturn(0); // 已被回调更新

        // 调用，并断言
        assertEquals(1, imageService.timeoutImage());
        verify(imageMapper).selectListByStatusAndCreateTimeLt(eq(IN_PROGRESS), argThat((LocalDateTime time) ->
                time.isBefore(LocalDateTime.now().minusMinutes(9))));
        verifyImageFail(1L, "生成超时：超过 10 分钟未完成");
    }

    // ========== 我的、公开、管理 ==========

    @Test
    public void testGetImagePublic_notPublic() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setPublicStatus(false)
                .setStatus(AiImageStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> imageService.getImagePublic(IMAGE_ID), IMAGE_PUBLIC_NOT_EXISTS);
    }

    @Test
    public void testGetImagePublic_notSuccess() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setPublicStatus(true)
                .setStatus(AiImageStatusEnum.FAIL.getStatus()));

        assertServiceException(() -> imageService.getImagePublic(IMAGE_ID), IMAGE_PUBLIC_NOT_EXISTS);
    }

    @Test
    public void testUpdateImagePublicMy_success() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setUserId(USER_ID)
                .setStatus(AiImageStatusEnum.SUCCESS.getStatus()));

        imageService.updateImagePublicMy(USER_ID, new AiImageUpdateReqVO().setId(IMAGE_ID).setPublicStatus(true));
        verify(imageMapper).updateById(argThat((AiImageDO update) -> Objects.equals(update.getId(), IMAGE_ID)
                && Boolean.TRUE.equals(update.getPublicStatus()) && update.getStatus() == null));
    }

    @Test
    public void testUpdateImagePublicMy_notSuccess() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setUserId(USER_ID)
                .setStatus(IN_PROGRESS));

        assertServiceException(() -> imageService.updateImagePublicMy(USER_ID,
                new AiImageUpdateReqVO().setId(IMAGE_ID).setPublicStatus(true)), IMAGE_PUBLIC_STATUS_FAIL);
        verify(imageMapper, never()).updateById(any(AiImageDO.class));
    }

    @Test
    public void testUpdateImagePublicMy_notOwner() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setUserId(USER_ID + 1)
                .setStatus(AiImageStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> imageService.updateImagePublicMy(USER_ID,
                new AiImageUpdateReqVO().setId(IMAGE_ID).setPublicStatus(true)), IMAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteImageMy_notOwner() {
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(new AiImageDO().setId(IMAGE_ID).setUserId(USER_ID + 1));

        assertServiceException(() -> imageService.deleteImageMy(IMAGE_ID, USER_ID), IMAGE_NOT_EXISTS);
        verify(imageMapper, never()).deleteById(any(Long.class));
    }

    // ========== optimizePrompt ==========

    @Test
    public void testOptimizePrompt_success() {
        // mock 方法
        mockDefaultChatModel();
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(MODEL_ID)).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(Collections.singletonList(
                new Generation(new AssistantMessage("  雨后的小巷里，一只橘猫，电影感光线  ")))));

        // 调用，并断言
        assertEquals("雨后的小巷里，一只橘猫，电影感光线",
                imageService.optimizePrompt(USER_ID, new AiImageOptimizePromptReqVO().setPrompt("一只猫在雨后的小巷")));
        verify(chatModel).call(argThat((Prompt prompt) -> prompt.getInstructions().size() == 2
                && Objects.equals(prompt.getInstructions().get(0).getText(), AiImageServiceImpl.OPTIMIZE_PROMPT_SYSTEM)
                && Objects.equals(prompt.getInstructions().get(1).getText(), "一只猫在雨后的小巷")));
    }

    @Test
    public void testOptimizePrompt_fail() {
        // mock 方法
        mockDefaultChatModel();
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(MODEL_ID)).thenReturn(chatModel);
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("上游超时"));

        // 调用，并断言异常
        assertServiceException(() -> imageService.optimizePrompt(USER_ID,
                new AiImageOptimizePromptReqVO().setPrompt("一只猫")), IMAGE_OPTIMIZE_PROMPT_FAIL);
    }

    private void mockDefaultChatModel() {
        when(modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(new AiModelDO()
                .setId(MODEL_ID).setPlatform(AiPlatformEnum.OPENAI.getPlatform()).setModel("gpt-4o-mini")
                .setTemperature(0.7).setMaxTokens(1024));
    }

    // ========== 公共方法 ==========

    private AiModelDO mockModel(AiPlatformEnum platform, String model) {
        AiModelDO modelDO = new AiModelDO().setId(MODEL_ID).setName("绘图模型").setModel(model)
                .setPlatform(platform.getPlatform()).setKeyId(API_KEY_ID);
        when(modelService.validateModel(MODEL_ID)).thenReturn(modelDO);
        return modelDO;
    }

    private void mockMidjourneyApiByKey(Long apiKeyId, MidjourneyApi api) {
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(apiKeyId);
        when(apiKeyService.validateApiKey(apiKeyId)).thenReturn(apiKey);
        when(modelService.getMidjourneyApi(apiKey)).thenReturn(api);
    }

    private void mockInsertId() {
        doAnswer(invocation -> {
            invocation.<AiImageDO>getArgument(0).setId(IMAGE_ID);
            return 1;
        }).when(imageMapper).insert(any(AiImageDO.class));
    }

    private static MidjourneyApi.SubmitResponse submitSuccess() {
        return new MidjourneyApi.SubmitResponse(MidjourneyApi.SubmitCodeEnum.SUBMIT_SUCCESS.getCode(), "提交成功", null, NEW_TASK_ID);
    }

    private void verifyImageFail(Long id, String errorMessage) {
        verify(imageMapper).updateByIdAndStatus(eq(id), eq(IN_PROGRESS), argThat((AiImageDO update) ->
                Objects.equals(update.getStatus(), AiImageStatusEnum.FAIL.getStatus())
                        && Objects.equals(update.getErrorMessage(), errorMessage) && update.getFinishTime() != null));
    }

    private static AiImageDrawReqVO buildDrawReqVO() {
        return new AiImageDrawReqVO().setModelId(MODEL_ID).setPrompt("一只猫").setWidth(1024).setHeight(768);
    }

    /**
     * 启动本地图片服务：任意路径都返回一张图片的字节
     *
     * @return 服务地址
     */
    private String startImageServer() throws IOException {
        imageServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        imageServer.createContext("/", exchange -> {
            byte[] body = "image-bytes".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        imageServer.start();
        return "http://127.0.0.1:" + imageServer.getAddress().getPort();
    }

}
