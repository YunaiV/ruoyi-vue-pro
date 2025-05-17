package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 流程模型的保存 Request VO")
@Data
public class BpmModelSaveReqVO extends BpmModelMetaInfoVO {

    @Schema(description = "编号", example = "1024")
    private String id;

    @Schema(description = "流程标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "process_yudao")
    @NotEmpty(message = "流程标识不能为空")
    private String key;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @Schema(description = "流程分类", example = "1")
    private String category;

    @Schema(description = "BPMN XML")
    private String bpmnXml;

    @Schema(description = "仿钉钉流程设计模型对象")
    @Valid
    private BpmSimpleModelNodeVO simpleModel;

}
