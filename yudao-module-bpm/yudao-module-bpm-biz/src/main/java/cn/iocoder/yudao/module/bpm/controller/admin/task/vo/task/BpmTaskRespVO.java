package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 流程任务的 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskRespVO extends BpmTaskDonePageItemRespVO {

    @ApiModelProperty(value = "任务定义的标识", required = true, example = "user-001")
    private String definitionKey;

    /**
     * 审核的用户信息
     */
    private User assigneeUser;

    @ApiModel("用户信息")
    @Data
    public static class User {

        @ApiModelProperty(value = "用户编号", required = true, example = "1")
        private Long id;
        @ApiModelProperty(value = "用户昵称", required = true, example = "芋艿")
        private String nickname;

        @ApiModelProperty(value = "部门编号", required = true, example = "1")
        private Long deptId;
        @ApiModelProperty(value = "部门名称", required = true, example = "研发部")
        private String deptName;

    }
}
