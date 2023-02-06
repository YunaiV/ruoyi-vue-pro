package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程定义的分页的每一项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmProcessDefinitionPageItemRespVO extends BpmProcessDefinitionRespVO {

    @Schema(description = "表单名字", example = "请假表单")
    private String formName;

    @Schema(description = "部署时间", required = true)
    private LocalDateTime deploymentTime;

}
