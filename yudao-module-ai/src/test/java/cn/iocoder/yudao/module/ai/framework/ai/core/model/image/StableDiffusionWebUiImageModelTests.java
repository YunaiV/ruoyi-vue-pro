package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiImageOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link StableDiffusionWebUiImageModel} 单元测试
 *
 * <p>使用 Mockito mock {@link StableDiffusionWebUiApi}，
 * 验证模型正确构建请求并解析 base64 响应，无需真实 SD WebUI 服务。</p>
 *
 * <p>集成测试（{@code @Disabled}）需本地或 RunPod 已运行 AUTOMATIC1111：
 * <pre>{@code
 * # RunPod TCP 直连
 * SDWEBUI_BASE_URL=http://103.196.86.126:15112
 * }</pre>
 * </p>
 *
 * @author deepay
 */
public class StableDiffusionWebUiImageModelTests {

    // ========== Mock 单元测试 ==========

    @Test
    public void testCall_returnsSingleImage() {
        // 1. mock API
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        StableDiffusionWebUiApi.Txt2ImgResponse fakeResponse = new StableDiffusionWebUiApi.Txt2ImgResponse();
        fakeResponse.setImages(List.of("base64imagedata=="));
        when(mockApi.txt2img(any())).thenReturn(ResponseEntity.ok(fakeResponse));

        StableDiffusionWebUiImageModel model = new StableDiffusionWebUiImageModel(mockApi);

        // 2. call
        ImageResponse response = model.call(new ImagePrompt("black luxury dress"));

        // 3. assert
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals("base64imagedata==", response.getResult().getOutput().getB64Json());
        assertNull(response.getResult().getOutput().getUrl());
        verify(mockApi, times(1)).txt2img(any());
    }

    @Test
    public void testCall_emptyImagesListReturnsEmptyResponse() {
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        StableDiffusionWebUiApi.Txt2ImgResponse fakeResponse = new StableDiffusionWebUiApi.Txt2ImgResponse();
        fakeResponse.setImages(List.of());
        when(mockApi.txt2img(any())).thenReturn(ResponseEntity.ok(fakeResponse));

        StableDiffusionWebUiImageModel model = new StableDiffusionWebUiImageModel(mockApi);
        ImageResponse response = model.call(new ImagePrompt("empty test"));

        assertNotNull(response);
        assertEquals(0, response.getResults().size());
    }

    @Test
    public void testCall_nullBodyReturnsEmptyResponse() {
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        when(mockApi.txt2img(any())).thenReturn(ResponseEntity.ok(null));

        StableDiffusionWebUiImageModel model = new StableDiffusionWebUiImageModel(mockApi);
        ImageResponse response = model.call(new ImagePrompt("null body test"));

        assertNotNull(response);
        assertEquals(0, response.getResults().size());
    }

    @Test
    public void testCall_withCustomOptions_passesParamsToApi() {
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        StableDiffusionWebUiApi.Txt2ImgResponse fakeResponse = new StableDiffusionWebUiApi.Txt2ImgResponse();
        fakeResponse.setImages(List.of("data"));
        when(mockApi.txt2img(any())).thenReturn(ResponseEntity.ok(fakeResponse));

        StableDiffusionWebUiImageModel model = new StableDiffusionWebUiImageModel(mockApi);
        StableDiffusionWebUiImageOptions options = StableDiffusionWebUiImageOptions.builder()
                .width(768).height(1024)
                .steps(30).cfgScale(7.5F)
                .negativePrompt("ugly, blurry")
                .samplerName("DPM++ 2M Karras")
                .build();

        ImageResponse response = model.call(new ImagePrompt("fashion model", options));

        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        // capture and verify request params
        var captor = org.mockito.ArgumentCaptor.forClass(StableDiffusionWebUiApi.Txt2ImgRequest.class);
        verify(mockApi).txt2img(captor.capture());
        StableDiffusionWebUiApi.Txt2ImgRequest captured = captor.getValue();
        assertEquals("fashion model", captured.getPrompt());
        assertEquals(768, captured.getWidth());
        assertEquals(1024, captured.getHeight());
        assertEquals(30, captured.getSteps());
        assertEquals("ugly, blurry", captured.getNegativePrompt());
        assertEquals("DPM++ 2M Karras", captured.getSamplerName());
    }

    @Test
    public void testConstants() {
        assertEquals("http://localhost:7860", StableDiffusionWebUiImageModel.DEFAULT_BASE_URL);
        assertEquals("/sdapi/v1/txt2img", StableDiffusionWebUiApi.TXT2IMG_PATH);
    }

    // ========== 集成测试（需要真实 SD WebUI 服务，默认跳过）==========

    @Test
    @Disabled("需要 RunPod TCP 直连 SD WebUI 才能执行：SDWEBUI_BASE_URL=http://103.196.86.126:15112")
    public void testCall_integration() {
        String baseUrl = System.getenv().getOrDefault("SDWEBUI_BASE_URL", "http://103.196.86.126:15112");

        StableDiffusionWebUiApi api = new StableDiffusionWebUiApi(baseUrl);
        StableDiffusionWebUiImageModel model = new StableDiffusionWebUiImageModel(api);

        StableDiffusionWebUiImageOptions options = StableDiffusionWebUiImageOptions.builder()
                .width(512).height(512).steps(20).build();

        ImageResponse response = model.call(new ImagePrompt("black luxury dress", options));

        System.out.println("[StableDiffusionWebUiImageModelTests] images count: " + response.getResults().size());
        assertFalse(response.getResults().isEmpty());
        assertNotNull(response.getResult().getOutput().getB64Json());
    }

}
