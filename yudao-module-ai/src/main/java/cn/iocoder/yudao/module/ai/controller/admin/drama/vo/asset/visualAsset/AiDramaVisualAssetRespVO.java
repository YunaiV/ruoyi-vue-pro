package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧视觉资产 Response VO")
@Data
public class AiDramaVisualAssetRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "资产类型", example = "prop")
    private String kind;

    @Schema(description = "资产名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "旧怀表")
    private String title;

    @Schema(description = "出图提示词", example = "黄铜怀表")
    private String prompt;

    @Schema(description = "结果图地址", example = "https://www.iocoder.cn/1.png")
    private String imageUrl;

    @Schema(description = "绘画模型编号", example = "1")
    private Long modelId;

    @Schema(description = "上游绘画任务编号", example = "")
    private String taskId;

    @Schema(description = "最近一次生成的状态，参见 AiDramaStatusEnum 枚举类", example = "40")
    private Integer status;

    @Schema(description = "失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
