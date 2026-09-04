package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 项目公告保存 Request VO")
@Data
public class PmsProjectAnnouncementSaveReqVO {

    @Schema(description = "公告编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 5000, message = "公告内容不能超过 5000 个字符")
    private String content;

    @Schema(description = "附件地址列表")
    private List<String> fileUrls;

}
