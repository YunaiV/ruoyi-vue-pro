package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.baidu.BaiduTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.deepseek.DeepSeekTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.tencent.TencentTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.tongyi.TongYiTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu.BaiduSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.deepseek.DeepSeekSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.TencentSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.baidu.BaiduOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.siliconflow.SiliconFlowOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tencent.TencentOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tongyi.TongYiOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.volcengine.VolcEngineOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.reasoning.AiRequestBodyPatcher;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link AiModelFactoryImpl} 的单元测试类：按平台创建、按配置缓存
 *
 * 语音识别、文字识别、语音合成只创建对象，不发起请求；
 * 绘图、DeepSeek 和 OpenAI 对话使用本机 {@link HttpServer} 模拟 Stability AI、火山方舟、DeepSeek、OpenAI 接口，不会请求真实的服务
 *
 * @author 芋道源码
 */
public class AiModelFactoryImplTest {

    /**
     * 不会被请求的地址：只创建对象的用例使用
     */
    private static final String URL = "http://127.0.0.1:9";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String STABILITY_RESPONSE = "{\"artifacts\":[{\"seed\":1,\"base64\":\"aGVsbG8=\",\"finishReason\":\"SUCCESS\"}]}";
    private static final String ARK_RESPONSE = "{\"model\":\"doubao-seedream-4-5-251128\",\"created\":1,"
            + "\"data\":[{\"url\":\"https://files.example.invalid/output.png\",\"size\":\"2048x2048\"}]}";
    private static final String DEEPSEEK_RESPONSE = "{\"id\":\"r1\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"deepseek-chat\","
            + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},\"finish_reason\":\"stop\"}],"
            + "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1,\"total_tokens\":2}}";

    private final AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

    private HttpServer server;
    private String baseUrl;
    /**
     * 收到的请求的路径
     */
    private final List<String> requestPaths = new CopyOnWriteArrayList<>();
    /**
     * 收到的请求体
     */
    private final List<String> requestBodies = new CopyOnWriteArrayList<>();
    /**
     * 收到的请求的 Accept-Encoding
     */
    private final List<String> acceptEncodings = new CopyOnWriteArrayList<>();

    @BeforeEach
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            requestPaths.add(path);
            try (InputStream in = exchange.getRequestBody()) {
                requestBodies.add(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            acceptEncodings.add(String.valueOf(exchange.getRequestHeaders().getFirst("Accept-Encoding")));
            // 按路径返回：DeepSeek、OpenAI 对话，火山方舟出图，其它为 Stability AI 出图
            String response = path.endsWith("/chat/completions") ? DEEPSEEK_RESPONSE
                    : path.endsWith("/images/generations") ? ARK_RESPONSE : STABILITY_RESPONSE;
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    // ========== 语音识别 ==========

    @Test
    public void testGetOrCreateTranscriptionModel() {
        TranscriptionModel model = modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.DEEPSEEK, "asr-factory-key-1", URL, "deepseek-asr");
        assertInstanceOf(DeepSeekTranscriptionModel.class, model);
        assertSame(model, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.DEEPSEEK, "asr-factory-key-1", URL, "deepseek-asr"));
        assertInstanceOf(TongYiTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.ALIBABA, "asr-factory-key-1", URL, "qwen3-asr-flash"));
        assertInstanceOf(TencentTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.TENCENT, "AKIDasr|asrKey", null, "16k_zh"));
        assertInstanceOf(BaiduTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.BAIDU, "asr-factory-key-1", URL, "1537"));
        // 模型不正确：腾讯云不是 16k 引擎、百度不是 dev_pid
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.TENCENT, "AKIDasr|asrKey", null, "8k_zh"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.BAIDU, "asr-factory-key-1", URL, "baidu-asr"));
        // 不支持的平台
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.OPENAI, "sk", URL, "whisper-1"));
    }

    // ========== 文字识别 ==========

    @Test
    public void testGetOrCreateOcrModel() {
        assertInstanceOf(TongYiOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.ALIBABA, "ocr-factory-key-1", null, "qwen-vl-ocr"));
        assertInstanceOf(SiliconFlowOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.SILICONFLOW, "ocr-factory-key-1", URL, "deepseek-ai/DeepSeek-OCR"));
        assertInstanceOf(TencentOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.TENCENT, "AKIDfactory|factoryKey", null, null));
        assertInstanceOf(BaiduOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.BAIDU, "bce-v3/ALTAK-factory/factoryKey", URL, "accurate_basic"));
        assertInstanceOf(VolcEngineOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.BYTEDANCE, "AKfactory|SKfactory", null, null));
    }

    @Test
    public void testGetOrCreateOcrModel_cache() {
        // 相同配置复用实例
        OcrModel model = modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, null);
        assertSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, null));
        // 密钥、地址、模型任一变化，都是新实例，不复用旧凭证
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret-2", URL, null));
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", "http://127.0.0.1:10", null));
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, "accurate_basic"));
    }

    @Test
    public void testGetOrCreateOcrModel_unsupported() {
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.OPENAI, "sk", URL, "gpt-4o"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.MOONSHOT, "sk", URL, "kimi-latest"));
        // DeepSeek 支持文字识别，但模型标识只能是 purpose
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> modelFactory.getOrCreateOcrModel(AiPlatformEnum.DEEPSEEK, "sk", URL, "deepseek-chat")).getType());
    }

    @Test
    public void testGetOrCreateOcrModel_invalidConfig() {
        // 组合密钥不是两段：不发请求，直接拒绝，且消息不回显密钥
        OcrException ex = assertThrows(OcrException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.TENCENT, "only-one-secret-part", null, null));
        assertEquals(OcrException.Type.INVALID_CREDENTIAL, ex.getType());
        assertFalse(ex.getMessage().contains("only-one-secret-part"));
        // 非 OCR 模型标识
        ex = assertThrows(OcrException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.ALIBABA, "ocr-factory-key-2", null, "qwen-plus"));
        assertEquals(OcrException.Type.INVALID_CONFIG, ex.getType());
    }

    // ========== 语音合成 ==========

    @Test
    public void testGetOrCreateTextToSpeechModel() {
        assertInstanceOf(TongYiSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.ALIBABA, "speech-factory-key-1", URL, "qwen3-tts-flash"));
        assertInstanceOf(MiniMaxSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-1", URL, "speech-2.8-hd"));
        assertInstanceOf(DouBaoSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.BYTEDANCE, "speech-factory-key-1", URL, "seed-tts-2.0"));
        assertInstanceOf(BaiduSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.BAIDU, "speech-factory-key-1", URL, "baidu-tts"));
        // 腾讯云：原生为异步任务，由 TencentSpeechModel 统一为“提交并得到音频”
        assertInstanceOf(TencentSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.TENCENT, "AKIDspeech|speechKey", null, "tencent-tts"));
        assertInstanceOf(DeepSeekSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.DEEPSEEK, "speech-factory-key-1", URL, "deepseek-tts"));

        // 相同配置复用实例；不同模型是不同实例，模型绑定在实例上
        AbstractAiSpeechModel hd = (AbstractAiSpeechModel) modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-hd");
        assertSame(hd, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-hd"));
        AiSpeechModel turbo = modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-turbo");
        assertNotSame(hd, turbo);
        assertEquals("speech-2.8-hd", hd.getModel());
        assertEquals("speech-2.8-hd", hd.getDefaultOptions().getModel());
        // 请求不能切换已绑定的模型（在发请求前拒绝）
        assertThrows(IllegalArgumentException.class, () -> hd.call(new TextToSpeechPrompt("你好",
                TextToSpeechOptions.builder().model("speech-2.8-turbo").voice("v").build())));
    }

    @Test
    public void testGetOrCreateTextToSpeechModel_unsupported() {
        // 腾讯云的密钥格式不正确：创建时拒绝
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.TENCENT, "bad-key", null, "tencent-tts"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.OPENAI, "sk", URL, "tts-1"));
        // 不在支持列表的模型
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.ALIBABA, "speech-factory-key-3", URL, "cosyvoice-v2"));
    }

    @Test
    public void testGetOrCreateTencentTtsApi() {
        TencentTtsApi api = modelFactory.getOrCreateTencentTtsApi("AKIDfactory|factoryKey", null);
        assertSame(api, modelFactory.getOrCreateTencentTtsApi("AKIDfactory|factoryKey", null));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTencentTtsApi("bad-key", null));
    }

    // ========== 绘图 ==========
    // 1. Stability AI 的模型拼接在请求路径中，需要使用模型自身的标识创建客户端
    // 2. 豆包 Seedream 直接使用官方 ArkService：按 API 密钥、URL 复用，自定义 URL 生效

    @Test
    public void testGetOrCreateImageModel_stabilityModelInPath() {
        // 准备参数：同一个 API 密钥、URL，两个不同的模型
        String url = baseUrl + "/v1";
        ImageModel modelA = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABILITYAI, "sk-test", url, "model-a");
        ImageModel modelB = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABILITYAI, "sk-test", url, "model-b");

        // 调用
        modelA.call(new ImagePrompt("一只猫", StabilityAiImageOptions.builder().model("model-a").build()));
        modelB.call(new ImagePrompt("一只狗", StabilityAiImageOptions.builder().model("model-b").build()));
        // 断言：各自使用自己的模型拼接请求路径，不会串用
        assertEquals(List.of("/v1/generation/model-a/text-to-image", "/v1/generation/model-b/text-to-image"),
                requestPaths);
    }

    @Test
    public void testGetOrCreateImageModel_stabilityBlankModel() {
        // 准备参数：未配置模型
        String url = baseUrl + "/v1";
        ImageModel imageModel = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABILITYAI, "sk-test", url, null);

        // 调用
        imageModel.call(new ImagePrompt("一只猫", StabilityAiImageOptions.builder().build()));
        // 断言：使用默认模型
        assertEquals(List.of("/v1/generation/" + StabilityAiApi.DEFAULT_IMAGE_MODEL + "/text-to-image"), requestPaths);
    }

    @Test
    public void testGetOrCreateArkService() {
        // 准备参数
        String url = baseUrl + "/api/v3";

        // 调用：相同的 API 密钥、URL，复用同一个客户端；不同的 API 密钥，不复用
        ArkService arkService = modelFactory.getOrCreateArkService("sk-ark-test", url);
        assertSame(arkService, modelFactory.getOrCreateArkService("sk-ark-test", url));
        assertNotSame(arkService, modelFactory.getOrCreateArkService("sk-ark-other", url));
        AiImageDO image = new AiImageDO().setModel("doubao-seedream-5-0-flash-260915").setPrompt("一只猫")
                .setInputImages(List.of(new AiImageDO.InputImage(AiImageInputRoleEnum.REFERENCE.getRole(), "https://file/1.png")))
                .setSeedreamOptions(new AiImageDO.SeedreamOptions().setSize("2K").setWatermark(false));
        ImagesResponse response = arkService.generateImages(AiImageUtils.buildSeedreamRequest(image));
        // 断言：请求自定义 URL 的路径，并解析返回
        assertEquals(List.of("/api/v3/images/generations"), requestPaths);
        assertTrue(requestBodies.get(0).contains("\"model\":\"doubao-seedream-5-0-flash-260915\""));
        assertTrue(requestBodies.get(0).contains("\"size\":\"2K\""));
        assertTrue(requestBodies.get(0).contains("\"image\":[\"https://file/1.png\"]"));
        assertTrue(requestBodies.get(0).contains("\"response_format\":\"url\""));
        assertTrue(requestBodies.get(0).contains("\"watermark\":false"));
        assertTrue(requestBodies.get(0).contains("\"optimize_prompt_options\":{\"mode\":\"standard\"}"));
        // 断言：不传组图参数（Seedream 5.0 flash 不支持，传了会 400）
        assertFalse(requestBodies.get(0).contains("sequential_image_generation"));
        assertEquals("https://files.example.invalid/output.png", response.getData().get(0).getUrl());
        assertEquals("2048x2048", response.getData().get(0).getSize());
    }

    // ========== DeepSeek 对话 ==========
    // Spring AI 真实的 DeepSeekApi / DeepSeekChatModel：校验请求不压缩的响应

    @Test
    public void testDeepSeekCall_identity() {
        // 准备参数
        DeepSeekChatModel model = buildDeepSeekModel(AiModelFactoryImpl.buildDeepSeekApiBuilder("test-key")
                .baseUrl(baseUrl).build());

        // 调用
        ChatResponse response = model.call(new Prompt(new UserMessage("你好")));
        // 断言：请求不压缩的响应，完整解析
        assertEquals("ok", response.getResult().getOutput().getText());
        assertEquals(List.of("identity"), acceptEncodings);
    }

    @Test
    public void testDeepSeekCall_identityWithPatcher() throws Exception {
        // 准备参数：思考参数走 HTTP 层补丁，替换了 RestClient.Builder，请求头仍保留
        AiRequestBodyPatcher patcher = new AiRequestBodyPatcher("{\"thinking\":{\"type\":\"enabled\"}}");
        DeepSeekChatModel model = buildDeepSeekModel(AiRequestBodyPatcher.configure(
                AiModelFactoryImpl.buildDeepSeekApiBuilder("test-key").baseUrl(baseUrl), patcher).build());

        // 调用
        ChatResponse response = model.call(new Prompt(new UserMessage("你好")));
        // 断言
        assertEquals("ok", response.getResult().getOutput().getText());
        assertEquals(List.of("identity"), acceptEncodings);
        assertEquals("enabled", MAPPER.readTree(requestBodies.get(0)).at("/thinking/type").asText());
    }

    // ========== OpenAI 对话 ==========
    // OpenAI 官方 SDK 的请求路径为 baseUrl + /chat/completions：已配置的 url 不含 /v1 时补上，含 /v1 时不重复补

    @Test
    public void testOpenAiCall_baseUrl() {
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(ToolCallingManager.class))
                    .thenReturn(DefaultToolCallingManager.builder().build());
            for (String url : new String[]{baseUrl, baseUrl + "/", baseUrl + "/v1", baseUrl + "/v1/"}) {
                // 准备参数
                requestPaths.clear();
                ChatModel model = modelFactory.getOrCreateChatModel(AiPlatformEnum.OPENAI, "test-openai-key", url, null);

                // 调用
                ChatResponse response = model.call(new Prompt(new UserMessage("你好")));
                // 断言
                assertEquals("ok", response.getResult().getOutput().getText(), url);
                assertEquals(List.of("/v1/chat/completions"), requestPaths, url);
            }
        }
    }

    private static DeepSeekChatModel buildDeepSeekModel(DeepSeekApi api) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .options(DeepSeekChatOptions.builder().model("deepseek-chat").temperature(0.7).build())
                .toolCallingManager(DefaultToolCallingManager.builder().build())
                .retryTemplate(RetryTemplate.builder().maxAttempts(1).build())
                .build();
    }

}
