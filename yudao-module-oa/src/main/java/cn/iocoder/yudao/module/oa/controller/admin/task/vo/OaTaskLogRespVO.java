package cn.iocoder.yudao.module.oa.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OA 任务反馈日志 Response VO")
@Data
public class OaTaskLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "反馈人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "反馈人昵称", example = "芋道")
    private String userName;

    @Schema(description = "变更后的状态", example = "3")
    private Integer status;

    @Schema(description = "反馈内容", example = "已完成盘点，结果已提交")
    private String content;

    @Schema(description = "反馈时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
