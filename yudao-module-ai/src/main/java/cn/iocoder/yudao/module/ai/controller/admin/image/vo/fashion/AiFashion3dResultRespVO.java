package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - AI 服装 3D 结果 Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装 3D 结果 Response VO")
@Data
public class AiFashion3dResultRespVO {

    @Schema(description = "资产编号", example = "1001")
    private Long id;

    @Schema(description = "处理状态（PROCESSING/SUCCESS/FAIL）", example = "SUCCESS")
    private String status;

    @Schema(description = "OBJ 网格文件地址", example = "https://example.com/mesh.obj")
    private String objFileUrl;

    @Schema(description = "MTL 材质文件地址", example = "https://example.com/fashion.mtl")
    private String mtlFileUrl;

    @Schema(description = "纹理图片地址", example = "https://example.com/texture.png")
    private String textureUrl;

    @Schema(description = "深度图地址", example = "https://example.com/depth.png")
    private String depthMapUrl;

    @Schema(description = "多角度预览图（角度 → 地址）", example = "{\"0\":\"https://example.com/angle_0.png\"}")
    private Map<String, String> previewUrls;

    @Schema(description = "旋转合成图/GIF 地址", example = "https://example.com/rotation.png")
    private String rotationGifUrl;

    @Schema(description = "网格顶点数", example = "4096")
    private Integer verticesCount;

    @Schema(description = "网格面片数", example = "8192")
    private Integer facesCount;

    @Schema(description = "网格精度", example = "64")
    private Integer gridResolution;

    @Schema(description = "颜色变换 Hex", example = "#FF0000")
    private String colorChange;

    @Schema(description = "处理总耗时（毫秒）", example = "3500")
    private Long durationMs;

    @Schema(description = "失败原因（status=FAIL 时有值）", example = "图片下载失败")
    private String errorMessage;

}
