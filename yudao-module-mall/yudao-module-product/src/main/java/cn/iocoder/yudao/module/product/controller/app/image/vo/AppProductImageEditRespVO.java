package cn.iocoder.yudao.module.product.controller.app.image.vo;

import cn.iocoder.yudao.module.product.service.image.ProductImageEditService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 服装图片编辑 Response VO")
@Data
public class AppProductImageEditRespVO {

    @Schema(description = "商品 SPU 编号")
    private Long spuId;

    @Schema(description = "原始图片 URL")
    private String originalPicUrl;

    @Schema(description = "规范化变换参数（供前端 Canvas/WebGL 消费）")
    private List<Map<String, Object>> transformParams;

    @Schema(description = "编辑历史")
    private List<Map<String, Object>> editHistory;

    @Schema(description = "提示信息")
    private String previewNote;

    @Schema(description = "虚拟试衣组件配置")
    private ProductImageEditService.VirtualTryOnConfigDTO virtualTryOnConfig;

    @Schema(description = "3D 展示配置（Three.js / Babylon.js）")
    private Map<String, Object> viewer3DConfig;

    @Schema(description = "批处理任务 ID（批量模式）")
    private String batchJobId;

}
