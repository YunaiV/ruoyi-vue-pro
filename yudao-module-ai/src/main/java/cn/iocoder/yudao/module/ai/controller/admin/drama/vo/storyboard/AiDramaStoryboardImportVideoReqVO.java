package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧导入外部视频为镜头视频 Request VO")
@Data
public class AiDramaStoryboardImportVideoReqVO {

    @Schema(description = "分镜编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "分镜编号不能为空")
    private Long id;

    @Schema(description = "视频地址：先通过文件上传接口上传", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://www.iocoder.cn/1.mp4")
    @NotBlank(message = "视频地址不能为空")
    @Size(max = 1024, message = "视频地址不能超过 1024 个字符")
    private String fileUrl;

}
