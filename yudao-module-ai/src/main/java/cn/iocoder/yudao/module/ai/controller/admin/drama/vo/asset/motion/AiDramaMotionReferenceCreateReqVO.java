package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧动作参考创建 Request VO")
@Data
public class AiDramaMotionReferenceCreateReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "转身挥手")
    @NotBlank(message = "名称不能为空")
    @Size(max = 120, message = "名称不能超过 120 个字符")
    private String name;

    @Schema(description = "视频地址：先通过文件上传接口上传", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://www.iocoder.cn/1.mp4")
    @NotBlank(message = "视频地址不能为空")
    @Size(max = 1024, message = "视频地址不能超过 1024 个字符")
    private String fileUrl;

}
