package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程实例抄送的分页 Item Response VO")
@Data
public class BpmProcessInstanceCCPageItemRespVO {

    // TODO @kyle：如果已经写了 swagger 注解，可以不用写 java 注释哈；
    /**
     * 编号
     */
    @Schema(description = "抄送主键")
    private Long id;

    /**
     * 发起人Id
     */
    @Schema(description = "发起人Id")
    private Long startUserId;

    @Schema(description = "发起人别名")
    private String startUserNickname;

    /**
     * 流程主键
     */
    @Schema(description = "流程实例的主键")
    private String processInstanceId;

    @Schema(description = "流程实例的名称")
    private String processInstanceName;
    /**
     * 任务主键
     */
    @Schema(description = "发起抄送的任务编号")
    private String taskId;

    @Schema(description = "发起抄送的任务名称")
    private String taskName;

    @Schema(description = "抄送原因")
    private String reason;

    @Schema(description = "抄送人")
    private String creator;

    @Schema(description = "抄送人别名")
    private String creatorNickname;

    @Schema(description = "抄送时间")
    private LocalDateTime createTime;

}
