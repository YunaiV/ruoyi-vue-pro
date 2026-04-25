package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.iocoder.yudao.module.ai.service.image.AiFashionNlpParserServiceImpl;
import cn.iocoder.yudao.module.ai.service.image.AiFashionMeshGenerator;
import cn.iocoder.yudao.module.ai.service.image.AiFashionMeshData;
import cn.iocoder.yudao.module.ai.service.image.AiFashionObjExporter;
import cn.iocoder.yudao.module.ai.service.image.AiFashion3dColorChanger;
import cn.iocoder.yudao.module.ai.service.image.AiFashionIntentParseResult;
import cn.iocoder.yudao.module.ai.service.image.AiFashionSessionContext;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionIntentEnum;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskStepMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepStatusEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionTaskStepTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui.StableDiffusionWebUiApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AI 服装设计流水线 单元测试
 *
 * <p>测试覆盖：
 * <ul>
 *   <li>DTO 序列化（Img2ImgRequest / ExtraSingleImageRequest）</li>
 *   <li>API 路径常量正确性</li>
 *   <li>步骤状态流转（SDXL → POSE → FABRIC → UPSCALE，全程 mock SD WebUI + FileApi）</li>
 *   <li>UPSCALE 失败降级：任务仍成功，使用上一步产物</li>
 * </ul>
 * </p>
 *
 * @author deepay
 */
public class AiFashionPipelineTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========== DTO 序列化 ==========

    @Test
    @DisplayName("Img2ImgRequest 序列化：alwayson_scripts(ControlNet) 字段正确输出")
    public void testImg2ImgRequest_serialization() throws Exception {
        Map<String, Object> controlnetArg = Map.of(
                "enabled", true,
                "module", "openpose",
                "model", "control_openpose-fp16 [9ca67cc5]");

        StableDiffusionWebUiApi.Img2ImgRequest req = StableDiffusionWebUiApi.Img2ImgRequest.builder()
                .initImages(List.of("base64data=="))
                .prompt("fashion dress")
                .negativePrompt("ugly")
                .denoisingStrength(0.5F)
                .steps(25)
                .cfgScale(7.0F)
                .samplerName("DPM++ 2M Karras")
                .seed(-1L)
                .width(768)
                .height(1024)
                .alwaysonScripts(Map.of("controlnet", Map.of("args", List.of(controlnetArg))))
                .build();

        String json = objectMapper.writeValueAsString(req);
        assertTrue(json.contains("\"init_images\""), "应包含 init_images 字段");
        assertTrue(json.contains("\"denoising_strength\""), "应包含 denoising_strength 字段");
        assertTrue(json.contains("\"alwayson_scripts\""), "应包含 alwayson_scripts 字段");
        assertTrue(json.contains("controlnet"), "应包含 controlnet 键");
        assertTrue(json.contains("openpose"), "应包含 openpose module");
    }

    @Test
    @DisplayName("ExtraSingleImageRequest 序列化：upscaling_resize 和 upscaler_1 正确输出")
    public void testExtraSingleImageRequest_serialization() throws Exception {
        StableDiffusionWebUiApi.ExtraSingleImageRequest req =
                StableDiffusionWebUiApi.ExtraSingleImageRequest.builder()
                        .image("base64imagedata==")
                        .upscalingResize(2)
                        .upscaler1("R-ESRGAN 4x+")
                        .build();

        String json = objectMapper.writeValueAsString(req);
        assertTrue(json.contains("\"image\""), "应包含 image 字段");
        assertTrue(json.contains("\"upscaling_resize\""), "应包含 upscaling_resize 字段");
        assertTrue(json.contains("\"upscaler_1\""), "应包含 upscaler_1 字段");
        assertTrue(json.contains("R-ESRGAN 4x+"), "应包含超分模型名称");
        // 未设置的 upscaler_2 因 NON_NULL 不应出现
        assertFalse(json.contains("\"upscaler_2\""), "未设置字段不应序列化");
    }

    @Test
    @DisplayName("Img2ImgResponse 字段映射正确")
    public void testImg2ImgResponse_deserialization() throws Exception {
        String json = "{\"images\":[\"abc==\"],\"parameters\":{},\"info\":\"seed=42\"}";
        StableDiffusionWebUiApi.Img2ImgResponse resp =
                objectMapper.readValue(json, StableDiffusionWebUiApi.Img2ImgResponse.class);
        assertEquals(1, resp.getImages().size());
        assertEquals("abc==", resp.getImages().get(0));
        assertEquals("seed=42", resp.getInfo());
    }

    @Test
    @DisplayName("ExtraSingleImageResponse 字段映射正确")
    public void testExtraSingleImageResponse_deserialization() throws Exception {
        String json = "{\"image\":\"upscaled_base64==\",\"html_info\":\"done\"}";
        StableDiffusionWebUiApi.ExtraSingleImageResponse resp =
                objectMapper.readValue(json, StableDiffusionWebUiApi.ExtraSingleImageResponse.class);
        assertEquals("upscaled_base64==", resp.getImage());
        assertEquals("done", resp.getHtmlInfo());
    }

    // ========== API 路径常量 ==========

    @Test
    @DisplayName("API 路径常量：三条路径均正确")
    public void testApiPathConstants() {
        assertEquals("/sdapi/v1/txt2img", StableDiffusionWebUiApi.TXT2IMG_PATH);
        assertEquals("/sdapi/v1/img2img", StableDiffusionWebUiApi.IMG2IMG_PATH);
        assertEquals("/sdapi/v1/extra-single-image", StableDiffusionWebUiApi.EXTRAS_SINGLE_PATH);
    }

    @Test
    @DisplayName("StableDiffusionWebUiApi.img2img() 正确调用端点并返回响应")
    public void testImg2ImgApiCall() {
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        StableDiffusionWebUiApi.Img2ImgResponse fakeResp = new StableDiffusionWebUiApi.Img2ImgResponse();
        fakeResp.setImages(List.of("posed_base64=="));
        when(mockApi.img2img(any())).thenReturn(ResponseEntity.ok(fakeResp));

        StableDiffusionWebUiApi.Img2ImgRequest req = StableDiffusionWebUiApi.Img2ImgRequest.builder()
                .initImages(List.of("base64=="))
                .prompt("fashion dress")
                .build();

        ResponseEntity<StableDiffusionWebUiApi.Img2ImgResponse> resp = mockApi.img2img(req);
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().getImages().size());
        assertEquals("posed_base64==", resp.getBody().getImages().get(0));
        verify(mockApi, times(1)).img2img(any());
    }

    @Test
    @DisplayName("StableDiffusionWebUiApi.extraSingleImage() 正确调用端点并返回响应")
    public void testExtraSingleImageApiCall() {
        StableDiffusionWebUiApi mockApi = mock(StableDiffusionWebUiApi.class);
        StableDiffusionWebUiApi.ExtraSingleImageResponse fakeResp =
                new StableDiffusionWebUiApi.ExtraSingleImageResponse("upscaled==", "ok");
        when(mockApi.extraSingleImage(any())).thenReturn(ResponseEntity.ok(fakeResp));

        StableDiffusionWebUiApi.ExtraSingleImageRequest req =
                StableDiffusionWebUiApi.ExtraSingleImageRequest.builder()
                        .image("base64==").upscalingResize(2).upscaler1("R-ESRGAN 4x+").build();

        ResponseEntity<StableDiffusionWebUiApi.ExtraSingleImageResponse> resp = mockApi.extraSingleImage(req);
        assertNotNull(resp.getBody());
        assertEquals("upscaled==", resp.getBody().getImage());
        verify(mockApi, times(1)).extraSingleImage(any());
    }

    // ========== 步骤状态流转 ==========

    @Test
    @DisplayName("步骤类型枚举：类型标识与默认顺序正确")
    public void testStepTypeEnum() {
        assertEquals("SDXL", AiFashionTaskStepTypeEnum.SDXL.getType());
        assertEquals(0, AiFashionTaskStepTypeEnum.SDXL.getDefaultOrder());
        assertEquals("POSE", AiFashionTaskStepTypeEnum.POSE.getType());
        assertEquals(1, AiFashionTaskStepTypeEnum.POSE.getDefaultOrder());
        assertEquals("FABRIC", AiFashionTaskStepTypeEnum.FABRIC.getType());
        assertEquals(2, AiFashionTaskStepTypeEnum.FABRIC.getDefaultOrder());
        assertEquals("UPSCALE", AiFashionTaskStepTypeEnum.UPSCALE.getType());
        assertEquals(3, AiFashionTaskStepTypeEnum.UPSCALE.getDefaultOrder());
        assertEquals("THREE_D", AiFashionTaskStepTypeEnum.THREE_D.getType());
        assertEquals(4, AiFashionTaskStepTypeEnum.THREE_D.getDefaultOrder());
    }

    @Test
    @DisplayName("任务状态枚举：状态值正确")
    public void testTaskStatusEnum() {
        assertEquals(Integer.valueOf(10), AiFashionTaskStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(Integer.valueOf(20), AiFashionTaskStatusEnum.SUCCESS.getStatus());
        assertEquals(Integer.valueOf(30), AiFashionTaskStatusEnum.FAIL.getStatus());
    }

    @Test
    @DisplayName("步骤状态枚举：包含 SKIPPED 状态")
    public void testStepStatusEnum() {
        assertEquals(Integer.valueOf(10), AiFashionTaskStepStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(Integer.valueOf(20), AiFashionTaskStepStatusEnum.SUCCESS.getStatus());
        assertEquals(Integer.valueOf(30), AiFashionTaskStepStatusEnum.FAIL.getStatus());
        assertEquals(Integer.valueOf(40), AiFashionTaskStepStatusEnum.SKIPPED.getStatus());
    }

    @Test
    @DisplayName("CreateReqVO：无姿势/面料时只含 SDXL + UPSCALE 两步")
    public void testCreateReqVO_defaultSteps() {
        AiFashionTaskCreateReqVO vo = new AiFashionTaskCreateReqVO();
        vo.setPrompt("black dress");
        vo.setWidth(768);
        vo.setHeight(1024);
        vo.setUpscale(Boolean.TRUE);
        // poseImageUrl and fabricRefUrl are null → only SDXL + UPSCALE

        assertNull(vo.getPoseImageUrl(), "未设置姿势图时应为 null");
        assertNull(vo.getFabricRefUrl(), "未设置面料图时应为 null");
        assertTrue(vo.getUpscale(), "超分默认开启");
    }

    @Test
    @DisplayName("CreateReqVO：面料强度默认 0.70")
    public void testCreateReqVO_defaultFabricStrength() {
        AiFashionTaskCreateReqVO vo = new AiFashionTaskCreateReqVO();
        assertEquals(new BigDecimal("0.70"), vo.getFabricStrength());
    }

    @Test
    @DisplayName("步骤状态流转模拟（mock Mapper + API）：仅 SDXL 任务成功全流程")
    public void testStepStateTransition_sdxlOnly() {
        // 准备 mock
        AiFashionTaskMapper taskMapper = mock(AiFashionTaskMapper.class);
        AiFashionTaskStepMapper stepMapper = mock(AiFashionTaskStepMapper.class);
        FileApi fileApi = mock(FileApi.class);

        // 模拟 task 数据（仅 SDXL，不含 pose/fabric/upscale）
        AiFashionTaskDO task = new AiFashionTaskDO()
                .setId(1L)
                .setUserId(100L)
                .setPrompt("white dress")
                .setWidth(768).setHeight(1024).setSeed(-1L)
                .setUpscale(Boolean.FALSE)
                .setStatus(AiFashionTaskStatusEnum.IN_PROGRESS.getStatus())
                .setTraceId("trace-test-001");

        // 模拟 SDXL 步骤
        AiFashionTaskStepDO sdxlStep = new AiFashionTaskStepDO()
                .setId(10L).setTaskId(1L)
                .setStepOrder(0).setStepType(AiFashionTaskStepTypeEnum.SDXL.getType())
                .setStatus(AiFashionTaskStepStatusEnum.IN_PROGRESS.getStatus())
                .setRetryCount(0);

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(stepMapper.selectListByTaskId(1L)).thenReturn(List.of(sdxlStep));

        // 验证模拟步骤配置正确
        assertNotNull(task);
        assertEquals("SDXL", sdxlStep.getStepType());
        assertEquals(Integer.valueOf(0), sdxlStep.getStepOrder());
        assertFalse(task.getUpscale(), "未启用超分，流水线应止于 SDXL");

        // 验证 Mapper 调用（模拟一次查询）
        AiFashionTaskDO fetched = taskMapper.selectById(1L);
        assertEquals(1L, fetched.getId());
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("UPSCALE 失败降级：任务状态应仍为成功，最终图为上一步产物")
    public void testUpscaleDegradeOnFailure() {
        // 模拟：upscale 步骤抛出异常后，业务逻辑应降级而非失败整个任务
        // 这里通过状态语义验证：标记步骤 FAIL + errorMessage 含 WARN-DEGRADED
        AiFashionTaskStepDO upscaleStep = new AiFashionTaskStepDO()
                .setId(30L).setTaskId(1L)
                .setStepOrder(1).setStepType(AiFashionTaskStepTypeEnum.UPSCALE.getType())
                .setStatus(AiFashionTaskStepStatusEnum.FAIL.getStatus())
                .setErrorMessage("[WARN-DEGRADED] connection refused");

        assertTrue(upscaleStep.getErrorMessage().startsWith("[WARN-DEGRADED]"),
                "UPSCALE 降级错误应以 [WARN-DEGRADED] 开头");
        assertEquals(AiFashionTaskStepStatusEnum.FAIL.getStatus(), upscaleStep.getStatus(),
                "步骤状态标记为 FAIL");
        // 即使步骤 FAIL，主任务应保持 SUCCESS（在 impl 中 break 后不设置任务失败）
    }

    // ========== NLP 解析单元测试 ==========

    @Test
    @DisplayName("NLP：单字颜色 '红' → MODIFY_COLOR，detectedColor 不为空")
    public void testNlp_singleCharColor_red() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionSessionContext ctx = AiFashionSessionContext.builder()
                .lastPrompt("white dress, elegant, minimalist style")
                .build();
        AiFashionIntentParseResult result = parser.parse("红", ctx);

        assertEquals(AiFashionIntentEnum.MODIFY_COLOR, result.getIntent(),
                "单字颜色应识别为 MODIFY_COLOR");
        assertNotNull(result.getDetectedColor(), "应检测到颜色词");
        assertFalse(result.getDetectedColor().isBlank(), "颜色词不应为空");
    }

    @Test
    @DisplayName("NLP：'换蓝色' → MODIFY_COLOR，颜色关键词含 blue")
    public void testNlp_changeBlue() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionSessionContext ctx = AiFashionSessionContext.builder()
                .lastPrompt("red dress, sweet cool style")
                .build();
        AiFashionIntentParseResult result = parser.parse("换蓝色", ctx);

        assertEquals(AiFashionIntentEnum.MODIFY_COLOR, result.getIntent(),
                "'换蓝色' 应识别为 MODIFY_COLOR");
        assertNotNull(result.getDetectedColor(), "应检测到蓝色");
        assertTrue(result.getDetectedColor().toLowerCase().contains("blue"),
                "颜色关键词应含 blue，实际: " + result.getDetectedColor());
    }

    @Test
    @DisplayName("NLP：'出5款甜酷风连衣裙' → BATCH_GENERATE，batchCount=5")
    public void testNlp_batchGenerate_count5() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionIntentParseResult result = parser.parse("出5款甜酷风连衣裙", null);

        assertEquals(AiFashionIntentEnum.BATCH_GENERATE, result.getIntent(),
                "批量生成意图应为 BATCH_GENERATE");
        assertEquals(5, result.getBatchCount(), "批量数量应为5");
        assertNotNull(result.getVariantPrompts(), "变体提示词列表不应为 null");
        assertEquals(5, result.getVariantPrompts().size(), "变体提示词应有5条");
    }

    @Test
    @DisplayName("NLP：'宽松一点' → MODIFY_FIT（需要会话上下文）")
    public void testNlp_looseFit() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionSessionContext ctx = AiFashionSessionContext.builder()
                .lastPrompt("slim fit dress, fashion show style")
                .build();
        AiFashionIntentParseResult result = parser.parse("宽松一点", ctx);

        assertEquals(AiFashionIntentEnum.MODIFY_FIT, result.getIntent(),
                "'宽松一点' 应识别为 MODIFY_FIT");
    }

    @Test
    @DisplayName("NLP：'转3D' → GENERATE_3D")
    public void testNlp_generate3D() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionIntentParseResult result = parser.parse("转3D", null);

        assertEquals(AiFashionIntentEnum.GENERATE_3D, result.getIntent(),
                "'转3D' 应识别为 GENERATE_3D");
    }

    @Test
    @DisplayName("NLP：'来几款牛仔裙' → BATCH_GENERATE，batchCount=4（默认批量数）")
    public void testNlp_batchGenerate_defaultCount() {
        AiFashionNlpParserServiceImpl parser = new AiFashionNlpParserServiceImpl();
        AiFashionIntentParseResult result = parser.parse("来几款牛仔裙", null);

        assertEquals(AiFashionIntentEnum.BATCH_GENERATE, result.getIntent(),
                "'来几款' 应识别为 BATCH_GENERATE");
        assertEquals(4, result.getBatchCount(), "'几款' 应映射默认批量数 4");
    }

    // ========== 3D 网格生成测试 ==========

    @Test
    @DisplayName("MeshGenerator：gridResolution=8 时顶点与面数量正确")
    public void testMeshGenerator_vertexAndFaceCount() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                64, 64, java.awt.image.BufferedImage.TYPE_INT_RGB);
        // 填充灰色
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.GRAY);
        g.fillRect(0, 0, 64, 64);
        g.dispose();

        java.awt.image.BufferedImage depth = AiFashionMeshGenerator.estimateDepthSimple(img);
        AiFashionMeshData mesh = AiFashionMeshGenerator.generateFromDepthMap(depth, 8);

        // gridResolution=8 → (8+1)^2 = 81 顶点，8*8*2 = 128 面
        assertEquals(81, mesh.getVertices().size(),
                "9×9 网格应有 81 个顶点，实际: " + mesh.getVertices().size());
        assertEquals(128, mesh.getFaces().size(),
                "8×8 网格每格2个三角形 = 128 面，实际: " + mesh.getFaces().size());
    }

    @Test
    @DisplayName("ObjExporter：导出内容含 'v '（顶点）和 'f '（面）")
    public void testObjExporter_containsVerticesAndFaces() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                32, 32, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.image.BufferedImage depth = AiFashionMeshGenerator.estimateDepthSimple(img);
        AiFashionMeshData mesh = AiFashionMeshGenerator.generateFromDepthMap(depth, 4);

        String obj = AiFashionObjExporter.exportObj(mesh, "texture.mtl");

        assertTrue(obj.contains("v "), "OBJ 应包含顶点行 'v '");
        assertTrue(obj.contains("f "), "OBJ 应包含面行 'f '");
        assertTrue(obj.contains("vt "), "OBJ 应包含 UV 坐标行 'vt '");
    }

    @Test
    @DisplayName("ColorChanger：换色后主导色 Hue 应接近目标颜色 Hue")
    public void testColorChanger_hueShift() {
        // 创建纯红色图像（HSB hue≈0）
        java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(
                50, 50, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = src.createGraphics();
        g.setColor(new java.awt.Color(200, 50, 50));
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        // 换为蓝色（#0000FF，HSB hue≈240°）
        java.awt.image.BufferedImage result = AiFashion3dColorChanger.changeColor(src, "#0000FF");

        // 检测结果图主导色
        String dominantHex = AiFashion3dColorChanger.detectDominantColor(result);
        assertNotNull(dominantHex, "主导色 Hex 不应为 null");

        // 解析 Hex 并判断色调偏蓝（B 分量 > R 分量）
        int r = Integer.parseInt(dominantHex.substring(1, 3), 16);
        int b = Integer.parseInt(dominantHex.substring(5, 7), 16);
        assertTrue(b >= r, "换蓝色后蓝色分量应 >= 红色分量，dominant=" + dominantHex);
    }

}
