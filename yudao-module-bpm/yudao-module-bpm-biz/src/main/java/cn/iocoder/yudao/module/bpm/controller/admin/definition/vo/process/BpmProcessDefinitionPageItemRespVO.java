package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 流程定义的分页的每一项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmProcessDefinitionPageItemRespVO extends BpmProcessDefinitionRespVO {

    @ApiModelProperty(value = "表单名字", example = "请假表单")
    private String formName;

    @ApiModelProperty(value = "部署时间", required = true)
    private LocalDateTime deploymentTime;

}
