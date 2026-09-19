package cn.iocoder.yudao.module.oa.controller.admin.task.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 任务反馈新增 Request VO")
@Data
public class OaTaskFeedbackReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "任务编号不能为空")
    private Long taskId;

    @Schema(description = "是否从发布管理反馈", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "反馈场景不能为空")
    private Boolean publisher;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "任务状态不能为空")
    @InEnum(value = OaTaskStatusEnum.class, message = "任务状态必须是 {value}")
    private Integer status;

    @Schema(description = "反馈内容", example = "已完成盘点，结果已提交")
    @Size(max = 1000, message = "反馈内容长度不能超过 1000 个字符")
    private String content;

}
