package cn.iocoder.yudao.module.product.service.image;

import java.util.List;
import java.util.Map;

/**
 * 智能服装图片编辑服务
 * <p>
 * 对应 Python IntelligentImageEditor + AugmentedRealityFitting + Interactive3DViewer +
 * BatchImageProcessor + InteractiveSharing._create_virtual_tryon_component()
 * <p>
 * 后端提供变换参数计算与配置生成，实际像素渲染由前端 WebGL / Canvas 或外部 AI 服务完成。
 *
 * @author deepay
 */
public interface ProductImageEditService {

    /**
     * 生成图片编辑任务参数
     * <p>
     * 接受编辑操作列表，返回规范化的变换参数供前端渲染。
     *
     * @param spuId  商品 SPU 编号（用于获取原图）
     * @param edits  编辑操作列表（type: change_color / change_pattern / adjust_fit / change_background）
     * @return 编辑任务结果 DTO
     */
    ImageEditResultDTO buildEditTask(Long spuId, List<Map<String, Object>> edits);

    /**
     * 生成虚拟试衣配置（AR + 3D）
     *
     * @param spuId             商品 SPU 编号
     * @param bodyMeasurements  身体参数（height_cm / weight_kg / chest_cm 等）
     * @return 虚拟试衣组件配置
     */
    VirtualTryOnConfigDTO buildVirtualTryOnConfig(Long spuId, Map<String, Object> bodyMeasurements);

    /**
     * 生成 3D 交互展示配置
     *
     * @param spuId 商品 SPU 编号
     * @return Three.js / Babylon.js 渲染配置 Map
     */
    Map<String, Object> build3DViewerConfig(Long spuId);

    /**
     * 批量图片编辑任务（异步）
     *
     * @param spuIds 商品 SPU 编号列表
     * @param edits  统一应用的编辑操作
     * @return 批次任务 ID
     */
    String submitBatchEditJob(List<Long> spuIds, List<Map<String, Object>> edits);

    // ===== 内部 DTO =====

    class ImageEditResultDTO {
        public Long spuId;
        public String originalPicUrl;
        /** 规范化变换参数（JSON，供前端 Canvas/WebGL 消费） */
        public List<Map<String, Object>> transformParams;
        /** 编辑历史（for version management） */
        public List<Map<String, Object>> editHistory;
        /** 预览说明 */
        public String previewNote;
    }

    class VirtualTryOnConfigDTO {
        public Long spuId;
        public String clothingModelUrl;
        /** 姿态估计端点（外部 AI 服务） */
        public String poseEstimationEndpoint;
        /** 支持的交互特性 */
        public List<String> features;
        /** UI 配置 */
        public Map<String, Object> uiConfig;
        /** 颜色预设 */
        public List<Map<String, Object>> colorPresets;
        /** 图案预设 */
        public List<Map<String, Object>> patternPresets;
        /** 调整后的身体适配参数 */
        public Map<String, Object> bodyFitParams;
    }

}
