package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 修改我的音乐 Request VO")
@Data
public class AiMusicUpdateMyReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10245")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "音乐名称", example = "夏夜晚风（Live 版）")
    @Size(max = 100, message = "音乐名称不能超过 100 个字符")
    private String title;

    @Schema(description = "封面地址，来自文件上传", example = "https://www.iocoder.cn/cover.png")
    @Size(max = 1024, message = "封面地址不能超过 1024 个字符")
    private String coverUrl;

    @Schema(description = "是否公开", example = "true")
    private Boolean publicStatus;

}
