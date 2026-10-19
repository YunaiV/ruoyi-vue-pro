package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 项目公告 Response VO")
@Data
public class PmsProjectAnnouncementRespVO {

    @Schema(description = "公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long projectId;

    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目启动")
    private String content;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/a.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "创建者用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long creatorUserId;

    @Schema(description = "创建者用户昵称", example = "芋道")
    private String creatorUserName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
