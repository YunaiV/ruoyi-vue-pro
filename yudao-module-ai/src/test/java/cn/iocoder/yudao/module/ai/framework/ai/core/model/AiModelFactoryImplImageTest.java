package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import com.sun.net.httpserver.HttpServer;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiModelFactoryImpl} 绘图相关的单元测试类
 *
 * 重点：
 * 1. Stability AI 的模型拼接在请求路径中，需要使用模型自身的标识创建客户端
 * 2. 豆包 Seedream 直接使用官方 ArkService：按 API 密钥、URL 复用，自定义 URL 生效
 *
 * 使用本地 {@link HttpServer} 模拟 Stability AI、火山方舟接口，不会请求真实的服务
 *
 * @author 芋道源码
 */
public class AiModelFactoryImplImageTest {

    private static final String RESPONSE = "{\"artifacts\":[{\"seed\":1,\"base64\":\"aGVsbG8=\",\"finishReason\":\"SUCCESS\"}]}";
    private static final String ARK_RESPONSE = "{\"model\":\"doubao-seedream-4-5-251128\",\"created\":1,"
            + "\"data\":[{\"url\":\"https://files.example.invalid/output.png\",\"size\":\"2048x2048\"}]}";

    private final AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

    private HttpServer server;
    private final List<String> requestPaths = new CopyOnWriteArrayList<>();
    private final List<String> requestBodies = new CopyOnWriteArrayList<>();

    @BeforeEach
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestPaths.add(exchange.getRequestURI().getPath());
            requestBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = (exchange.getRequestURI().getPath().endsWith("/images/generations") ? ARK_RESPONSE : RESPONSE)
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void testGetOrCreateImageModel_stabilityModelInPath() {
        // 准备参数：同一个 API 密钥、URL，两个不同的模型
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        ImageModel modelA = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABLE_DIFFUSION, "sk-test", url, "model-a");
        ImageModel modelB = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABLE_DIFFUSION, "sk-test", url, "model-b");

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
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        ImageModel imageModel = modelFactory.getOrCreateImageModel(AiPlatformEnum.STABLE_DIFFUSION, "sk-test", url, null);

        // 调用
        imageModel.call(new ImagePrompt("一只猫", StabilityAiImageOptions.builder().build()));
        // 断言：使用默认模型
        assertEquals(List.of("/v1/generation/" + StabilityAiApi.DEFAULT_IMAGE_MODEL + "/text-to-image"), requestPaths);
    }

    @Test
    public void testGetOrCreateArkService() {
        // 准备参数
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v3";

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

}
