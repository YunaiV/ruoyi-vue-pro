package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - AI 服装素材库 Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装素材库 Response VO")
@Data
public class AiFashionModelLibraryRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "标题", example = "Vogue Runway 2024春夏时装秀 造型1")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "品类（dress/suit/jacket...）", example = "dress")
    private String category;

    @Schema(description = "风格标签（JSON 数组）", example = "[\"runway\",\"haute_couture\"]")
    private List<String> styleTags;

    @Schema(description = "颜色标签（JSON 数组）", example = "[\"red\",\"black\"]")
    private List<String> colorTags;

    @Schema(description = "品牌", example = "Vogue")
    private String brand;

    @Schema(description = "季节", example = "spring_summer_2024")
    private String season;

    @Schema(description = "来源类型", example = "fashion_show")
    private String sourceType;

    @Schema(description = "采集源 ID", example = "vogue_runway")
    private String collectionSourceId;

    @Schema(description = "图片宽度（像素）", example = "1200")
    private Integer width;

    @Schema(description = "图片高度（像素）", example = "1800")
    private Integer height;

    @Schema(description = "文件格式", example = "png")
    private String fileFormat;

    @Schema(description = "是否含模特", example = "true")
    private Boolean isModel;

    @Schema(description = "模特姿势", example = "walking")
    private String modelPose;

    @Schema(description = "模特体型", example = "slim")
    private String modelBodyType;

    @Schema(description = "本地/OSS 路径（可用于展示）", example = "/fashion/runway/FSHOW_abc123.png")
    private String localPath;

    @Schema(description = "索引时间")
    private LocalDateTime indexedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ---- 关联模特特征（仅 isModel=true 时填充） ----

    @Schema(description = "身高（厘米）", example = "175")
    private Integer heightCm;

    @Schema(description = "胸围（厘米）", example = "88")
    private Integer bustCm;

    @Schema(description = "腰围（厘米）", example = "62")
    private Integer waistCm;

    @Schema(description = "臀围（厘米）", example = "92")
    private Integer hipsCm;

    @Schema(description = "肤色", example = "fair")
    private String skinTone;

    @Schema(description = "发色", example = "black")
    private String hairColor;

    @Schema(description = "发长", example = "long")
    private String hairLength;

    @Schema(description = "姿势类型", example = "walking")
    private String poseType;

}
