package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 音乐修改名称 Request VO")
@Data
public class AiMusicUpdateTitleReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15583")
    private Long id;

    @Schema(description = "音乐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "夜空中最亮的星")
    @NotNull(message = "音乐名称不能为空")
    private String title;

}