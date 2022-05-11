package net.lab1024.smartadmin.module.business.approve.camunda.hi.task.inst.domain.vo;

import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

/**
 *  [  ]
 *
 * @author 孟凯
 * @version 1.0
 * @company 1024创新实验室( www.1024lab.net )
 * @copyright (c) 1024创新实验室( www.1024lab.net )Inc. All rights reserved.
 * @date  2022-01-17 15:14:27
 * @since JDK1.8
 */
@Data
public class HiTaskinstVO {
    @ApiModelProperty("ID_")
    private String id;

    @ApiModelProperty("TASK_DEF_KEY_")
    private String taskDefKey;

    @ApiModelProperty("PROC_DEF_KEY_")
    private String procDefKey;

    @ApiModelProperty("PROC_DEF_ID_")
    private String procDefId;

    @ApiModelProperty("ROOT_PROC_INST_ID_")
    private String rootProcInstId;

    @ApiModelProperty("PROC_INST_ID_")
    private String procInstId;

    @ApiModelProperty("EXECUTION_ID_")
    private String executionId;

    @ApiModelProperty("CASE_DEF_KEY_")
    private String caseDefKey;

    @ApiModelProperty("CASE_DEF_ID_")
    private String caseDefId;

    @ApiModelProperty("CASE_INST_ID_")
    private String caseInstId;

    @ApiModelProperty("CASE_EXECUTION_ID_")
    private String caseExecutionId;

    @ApiModelProperty("ACT_INST_ID_")
    private String actInstId;

    @ApiModelProperty("NAME_")
    private String name;

    @ApiModelProperty("PARENT_TASK_ID_")
    private String parentTaskId;

    @ApiModelProperty("DESCRIPTION_")
    private String description;

    @ApiModelProperty("OWNER_")
    private String owner;

    @ApiModelProperty("ASSIGNEE_")
    private String assignee;

    @ApiModelProperty("START_TIME_")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty("END_TIME_")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty("DURATION_")
    private Long duration;

    @ApiModelProperty("DELETE_REASON_")
    private String deleteReason;

    @ApiModelProperty("PRIORITY_")
    private Integer priority;

    @ApiModelProperty("DUE_DATE_")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueDate;

    @ApiModelProperty("FOLLOW_UP_DATE_")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date followUpDate;

    @ApiModelProperty("TENANT_ID_")
    private String tenantId;

    @ApiModelProperty("REMOVAL_TIME_")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date removalTime;



}
