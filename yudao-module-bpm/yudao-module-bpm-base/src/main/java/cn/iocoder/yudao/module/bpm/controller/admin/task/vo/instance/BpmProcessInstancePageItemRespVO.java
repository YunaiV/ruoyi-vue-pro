package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel("管理后台 - 流程实例的分页 Item Response VO")
@Data
public class BpmProcessInstancePageItemRespVO {

    @ApiModelProperty(value = "流程实例的编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "流程定义的编号", required = true, example = "2048")
    private String processDefinitionId;

    @ApiModelProperty(value = "流程分类", required = true, notes = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @ApiModelProperty(value = "流程实例的状态", required = true, notes = "参见 bpm_process_instance_status", example = "1")
    private Integer status;

    @ApiModelProperty(value = "流程实例的结果", required = true, notes = "参见 bpm_process_instance_result", example = "2")
    private Integer result;

    @ApiModelProperty(value = "提交时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "结束时间", required = true)
    private Date endTime;

    /**
     * 当前任务
     */
    private List<Task> tasks;

    @ApiModel("流程任务")
    @Data
    public static class Task {

        @ApiModelProperty(value = "流程任务的编号", required = true, example = "1024")
        private String id;

        @ApiModelProperty(value = "任务名称", required = true, example = "芋道")
        private String name;

    }

}
