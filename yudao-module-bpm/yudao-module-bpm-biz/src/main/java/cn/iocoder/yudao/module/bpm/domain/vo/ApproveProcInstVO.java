package cn.iocoder.yudao.module.bpm.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 审批流程VO
 *
 * @author kemengkai
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveProcInstVO {

    @ApiModelProperty("任务id")
    private String id;
    @ApiModelProperty("任务部署key")
    private String taskDefKey;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("审批人登录名")
    private String assignee;
    @ApiModelProperty("审批人姓名")
    private String assigneeName;
    @ApiModelProperty("审批回复")
    private String taskComment;
    @ApiModelProperty("审批完成时间")
    private LocalDate endTime;
}
