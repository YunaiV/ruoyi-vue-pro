package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 音乐修改 Request VO")
@Data
public class AiMusicUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15583")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "是否发布", example = "true")
    private Boolean publicStatus;

    // TODO @xin：得单独一个 vo。因为万一。。。模拟请求，就可以改 publicStatus 拉
    @Schema(description = "音乐名称", example = "夜空中最亮的星")
    private String title;

}