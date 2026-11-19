package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧道具创建 / 修改 Request VO：关键帧由分镜生成，不经过这里")
@Data
public class AiDramaVisualAssetSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "资产名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "旧怀表")
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 128, message = "资产名称不能超过 128 个字符")
    private String title;

    @Schema(description = "出图提示词", example = "黄铜怀表，表盖有划痕")
    @Size(max = 2000, message = "出图提示词不能超过 2000 个字符")
    private String prompt;

    @Schema(description = "道具参考图", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "参考图地址不能超过 1024 个字符")
    private String referenceImageUrl;

    @Schema(description = "出现的镜头序号：分镜未单独指定道具时，按它匹配")
    private List<Integer> shotNumbers;

}
