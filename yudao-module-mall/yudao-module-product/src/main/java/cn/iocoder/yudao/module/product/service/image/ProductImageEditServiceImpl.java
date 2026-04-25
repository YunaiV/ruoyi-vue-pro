package cn.iocoder.yudao.module.product.service.image;

import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 智能服装图片编辑服务实现
 * <p>
 * 返回规范化变换参数和组件配置；像素级渲染由前端或外部 AI 服务完成。
 *
 * @author deepay
 */
@Slf4j
@Service
public class ProductImageEditServiceImpl implements ProductImageEditService {

    @Resource
    private ProductSpuMapper productSpuMapper;

    /** 模拟批处理任务存储（生产环境应持久化到数据库） */
    private final Map<String, Object> batchJobStore = new LinkedHashMap<>();

    // ==================== 公开接口 ====================

    @Override
    public ImageEditResultDTO buildEditTask(Long spuId, List<Map<String, Object>> edits) {
        ProductSpuDO spu = productSpuMapper.selectById(spuId);
        ImageEditResultDTO dto = new ImageEditResultDTO();
        dto.spuId = spuId;
        dto.originalPicUrl = spu != null ? spu.getPicUrl() : null;
        dto.editHistory = new ArrayList<>(edits);
        dto.transformParams = normalizeEdits(edits);
        dto.previewNote = "变换参数已生成，前端 Canvas/WebGL 负责像素渲染";
        return dto;
    }

    @Override
    public VirtualTryOnConfigDTO buildVirtualTryOnConfig(Long spuId, Map<String, Object> bodyMeasurements) {
        ProductSpuDO spu = productSpuMapper.selectById(spuId);
        VirtualTryOnConfigDTO dto = new VirtualTryOnConfigDTO();
        dto.spuId = spuId;
        dto.clothingModelUrl = spu != null
                ? spu.getPicUrl()  // 生产时替换为 3D .glb 模型 URL
                : "/models/" + spuId + ".glb";
        dto.poseEstimationEndpoint = "/api/v1/clothing/virtual-tryon/" + spuId;
        dto.features = Arrays.asList(
                "change_color", "change_pattern", "change_size",
                "virtual_fitting", "mix_and_match");
        dto.uiConfig = buildUiConfig();
        dto.colorPresets = buildColorPresets();
        dto.patternPresets = buildPatternPresets();
        dto.bodyFitParams = computeBodyFitParams(bodyMeasurements);
        return dto;
    }

    @Override
    public Map<String, Object> build3DViewerConfig(Long spuId) {
        ProductSpuDO spu = productSpuMapper.selectById(spuId);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("modelUrl", "/models/" + spuId + ".glb");
        config.put("thumbnailUrl", spu != null ? spu.getPicUrl() : null);
        config.put("renderer", "three.js");
        config.put("controls", Arrays.asList("rotate", "zoom", "pan"));
        config.put("lighting", buildLightingConfig());
        config.put("colorControls", buildColorControlConfig());
        config.put("animations", Arrays.asList(
                Map.of("name", "idle", "loop", true),
                Map.of("name", "rotate360", "loop", true),
                Map.of("name", "catwalk", "loop", false)));
        config.put("comparisonView", Map.of("enabled", true, "splitMode", "vertical"));
        config.put("shareConfig", Map.of(
                "canCapture", true,
                "shareEndpoint", "/product/share/create",
                "platforms", Arrays.asList("wechat", "weibo", "general")));
        return config;
    }

    @Override
    public String submitBatchEditJob(List<Long> spuIds, List<Map<String, Object>> edits) {
        String jobId = "BATCH_EDIT_" + System.currentTimeMillis();
        Map<String, Object> job = new LinkedHashMap<>();
        job.put("jobId", jobId);
        job.put("spuIds", spuIds);
        job.put("edits", edits);
        job.put("status", "QUEUED");
        job.put("totalCount", spuIds.size());
        job.put("processedCount", 0);
        job.put("createdAt", new Date().toString());
        batchJobStore.put(jobId, job);
        log.info("批量图片编辑任务已提交 jobId={} count={}", jobId, spuIds.size());
        return jobId;
    }

    // ==================== 私有方法 ====================

    private List<Map<String, Object>> normalizeEdits(List<Map<String, Object>> edits) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> edit : edits) {
            String type = (String) edit.get("type");
            Map<String, Object> params = edit.get("params") instanceof Map
                    ? (Map<String, Object>) edit.get("params")
                    : Collections.emptyMap();
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("operation", type);

            switch (type != null ? type : "") {
                case "change_color":
                    normalized.put("hueShift", params.getOrDefault("hue", 0));
                    normalized.put("saturationMultiplier", params.getOrDefault("saturation", 1.0));
                    normalized.put("brightnessMultiplier", params.getOrDefault("brightness", 1.0));
                    normalized.put("applyToClothingOnly", true);
                    break;
                case "change_pattern":
                    normalized.put("patternUrl", params.get("pattern"));
                    normalized.put("blendMode", params.getOrDefault("blend_mode", "overlay"));
                    normalized.put("opacity", params.getOrDefault("opacity", 0.5));
                    break;
                case "adjust_fit":
                    normalized.put("tightenFactor", params.getOrDefault("tighten", 0));
                    normalized.put("loosenFactor", params.getOrDefault("loosen", 0));
                    normalized.put("lengthenFactor", params.getOrDefault("lengthen", 0));
                    normalized.put("shortenFactor", params.getOrDefault("shorten", 0));
                    break;
                case "change_background":
                    normalized.put("backgroundUrl", params.get("background"));
                    normalized.put("removeOriginalBg", true);
                    break;
                default:
                    normalized.putAll(params);
            }
            result.add(normalized);
        }
        return result;
    }

    private Map<String, Object> buildUiConfig() {
        Map<String, Object> ui = new LinkedHashMap<>();
        ui.put("showOriginal", true);
        ui.put("showModified", true);
        ui.put("allowComparison", true);
        ui.put("maxModifications", 5);
        ui.put("realTimePreview", true);
        return ui;
    }

    private List<Map<String, Object>> buildColorPresets() {
        return Arrays.asList(
                Map.of("name", "经典黑", "hex", "#000000", "hue", 0, "saturation", 0.0),
                Map.of("name", "纯白",   "hex", "#FFFFFF", "hue", 0, "saturation", 0.0),
                Map.of("name", "中国红", "hex", "#DC143C", "hue", 348, "saturation", 0.86),
                Map.of("name", "深海蓝", "hex", "#1E90FF", "hue", 210, "saturation", 1.0),
                Map.of("name", "森林绿", "hex", "#228B22", "hue", 120, "saturation", 0.61),
                Map.of("name", "玫瑰金", "hex", "#B76E79", "hue", 351, "saturation", 0.28),
                Map.of("name", "卡其色", "hex", "#C3B091", "hue", 37,  "saturation", 0.31)
        );
    }

    private List<Map<String, Object>> buildPatternPresets() {
        return Arrays.asList(
                Map.of("name", "素色", "patternUrl", ""),
                Map.of("name", "条纹", "patternUrl", "/patterns/stripes.png"),
                Map.of("name", "格纹", "patternUrl", "/patterns/plaid.png"),
                Map.of("name", "波点", "patternUrl", "/patterns/polka-dot.png"),
                Map.of("name", "迷彩", "patternUrl", "/patterns/camo.png"),
                Map.of("name", "碎花", "patternUrl", "/patterns/floral.png")
        );
    }

    private Map<String, Object> buildLightingConfig() {
        Map<String, Object> light = new LinkedHashMap<>();
        light.put("ambient", Map.of("color", "#FFFFFF", "intensity", 0.6));
        light.put("directional", Map.of("color", "#FFFFFF", "intensity", 3.0,
                "position", Arrays.asList(1, 2, 3)));
        return light;
    }

    private Map<String, Object> buildColorControlConfig() {
        Map<String, Object> ctrl = new LinkedHashMap<>();
        ctrl.put("type", "color_picker");
        ctrl.put("hueRange", Arrays.asList(0, 360));
        ctrl.put("saturationRange", Arrays.asList(0, 1));
        ctrl.put("brightnessRange", Arrays.asList(0, 1));
        ctrl.put("presets", buildColorPresets());
        ctrl.put("realtimePreview", true);
        return ctrl;
    }

    private Map<String, Object> computeBodyFitParams(Map<String, Object> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            return Map.of("size", "M", "fitType", "regular");
        }
        double heightCm = ((Number) measurements.getOrDefault("height_cm", 165)).doubleValue();
        double weightKg = ((Number) measurements.getOrDefault("weight_kg", 55)).doubleValue();
        double bmi = weightKg / ((heightCm / 100.0) * (heightCm / 100.0));

        String size = bmi < 18.5 ? "XS" : bmi < 23 ? "S" : bmi < 25 ? "M" : bmi < 28 ? "L" : "XL";
        String fitType = bmi < 20 ? "slim" : bmi < 25 ? "regular" : "relaxed";
        Map<String, Object> params = new LinkedHashMap<>(measurements);
        params.put("recommendedSize", size);
        params.put("fitType", fitType);
        params.put("scaleX", 1.0 + (bmi - 22) * 0.01);
        params.put("scaleY", heightCm / 165.0);
        return params;
    }

}
