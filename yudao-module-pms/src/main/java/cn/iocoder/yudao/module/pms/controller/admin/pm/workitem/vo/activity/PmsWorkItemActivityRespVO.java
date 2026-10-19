package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 工作项动态响应 VO")
@Data
@Accessors(chain = true)
public class PmsWorkItemActivityRespVO {

    @Schema(description = "动态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long workItemId;

    @Schema(description = "操作人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long operatorUserId;

    @Schema(description = "操作人姓名", example = "芋道")
    private String operatorUserName;

    @Schema(description = "操作人头像")
    private String operatorUserAvatar;

    @Schema(description = "动态内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "更新了工作项")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
