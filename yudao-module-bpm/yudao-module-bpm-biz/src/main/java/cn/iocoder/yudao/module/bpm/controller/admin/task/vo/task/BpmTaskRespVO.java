package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 流程任务的 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskRespVO extends BpmTaskDonePageItemRespVO {

    @Schema(description = "任务定义的标识", required = true, example = "user-001")
    private String definitionKey;

    /**
     * 审核的用户信息
     */
    private User assigneeUser;

    @Schema(description = "用户信息")
    @Data
    public static class User {

        @Schema(description = "用户编号", required = true, example = "1")
        private Long id;
        @Schema(description = "用户昵称", required = true, example = "芋艿")
        private String nickname;

        @Schema(description = "部门编号", required = true, example = "1")
        private Long deptId;
        @Schema(description = "部门名称", required = true, example = "研发部")
        private String deptName;

    }
}
