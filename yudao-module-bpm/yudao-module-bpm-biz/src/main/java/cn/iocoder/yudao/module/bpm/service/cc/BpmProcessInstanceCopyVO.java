package cn.iocoder.yudao.module.bpm.service.cc;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;


/**
 * 流程抄送视图对象 wf_copy
 *
 * @author ruoyi
 * @date 2022-05-19
 */
@Data
public class BpmProcessInstanceCopyVO {

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

    @Schema(description = "流程实例的名字")
    private String processInstanceName;

    /**
     * 任务主键
     */
    @Schema(description = "发起抄送的任务编号")
    private String taskId;

    @Schema(description = "发起抄送的任务名称")
    private String taskName;
    /**
     * 用户主键
     */
    @Schema(description = "用户编号")
    private Long userId;

    @Schema(description = "用户别名")
    private Long userNickname;

    @Schema(description = "抄送原因")
    private String reason;

    @Schema(description = "抄送人")
    private String creator;

    @Schema(description = "抄送时间")
    private LocalDateTime createTime;
}
