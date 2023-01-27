package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 流程模型的分页的每一项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModelPageItemRespVO extends BpmModelBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "表单名字", example = "请假表单")
    private String formName;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 最新部署的流程定义
     */
    private ProcessDefinition processDefinition;

    @ApiModel("流程定义")
    @Data
    public static class ProcessDefinition {

        @ApiModelProperty(value = "编号", required = true, example = "1024")
        private String id;

        @ApiModelProperty(value = "版本", required = true, example = "1")
        private Integer version;

        @ApiModelProperty(value = "部署时间", required = true)
        private LocalDateTime deploymentTime;

        @ApiModelProperty(value = "中断状态", required = true, example = "1", notes = "参见 SuspensionState 枚举")
        private Integer suspensionState;

    }

}
