package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaVisualAssetDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 短剧视觉资产 Response VO")
@Data
public class AiDramaVisualAssetRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "分镜编号：关键帧所属的镜头", example = "1")
    private Long storyboardId;

    @Schema(description = "当前资产编号：不为空表示历史版本", example = "1")
    private Long parentId;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer kind;

    @Schema(description = "资产名称", example = "旧怀表")
    private String title;

    @Schema(description = "出图提示词", example = "黄铜怀表")
    private String prompt;

    @Schema(description = "参考图与使用范围")
    private AiDramaVisualAssetDO.ReferenceImages referenceImages;

    @Schema(description = "结果图地址", example = "https://www.iocoder.cn/1.png")
    private String imageUrl;

    @Schema(description = "绘画模型编号", example = "1")
    private Long modelId;

    @Schema(description = "绘画模型标识", example = "doubao-seedream-4-0-250828")
    private String model;

    @Schema(description = "最近一次生成的状态", example = "40")
    private Integer status;

    @Schema(description = "失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
