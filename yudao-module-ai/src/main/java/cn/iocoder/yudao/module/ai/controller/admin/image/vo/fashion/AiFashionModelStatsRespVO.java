package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - 模特库统计 Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 模特库统计 Response VO")
@Data
public class AiFashionModelStatsRespVO {

    @Schema(description = "素材库图片总数", example = "1500")
    private Long totalImages;

    @Schema(description = "含模特的图片数", example = "800")
    private Long modelImages;

    @Schema(description = "时装秀图片数", example = "400")
    private Long fashionShowImages;

    @Schema(description = "品牌图片数", example = "600")
    private Long brandImages;

    @Schema(description = "模特机构图片数", example = "500")
    private Long modelAgencyImages;

    @Schema(description = "品类分布（key=category, value=count）", example = "{\"dress\":300,\"suit\":150}")
    private Map<String, Long> categoryDistribution;

    @Schema(description = "体型分布（key=body_type, value=count）", example = "{\"slim\":200,\"curvy\":100}")
    private Map<String, Long> bodyTypeDistribution;

    @Schema(description = "肤色分布（key=skin_tone, value=count）", example = "{\"fair\":100,\"medium\":150}")
    private Map<String, Long> skinToneDistribution;

    @Schema(description = "姿势分布（key=pose_type, value=count）", example = "{\"walking\":200,\"front\":300}")
    private Map<String, Long> poseDistribution;

    @Schema(description = "品牌分布（key=brand, value=count）", example = "{\"Gucci\":50,\"Chanel\":60}")
    private Map<String, Long> brandDistribution;

    @Schema(description = "活跃采集源数量", example = "25")
    private Long activeSources;

    @Schema(description = "统计时间戳")
    private String statsAt;

}
