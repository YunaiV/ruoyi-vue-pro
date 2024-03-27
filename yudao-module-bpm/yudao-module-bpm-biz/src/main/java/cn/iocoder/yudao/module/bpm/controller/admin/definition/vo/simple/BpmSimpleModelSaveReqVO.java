package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 仿钉钉流程设计模型的新增/修改 Request VO")
@Data
public class BpmSimpleModelSaveReqVO {

    @Schema(description = "流程模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "流程模型编号不能为空")
    private String modelId; // 对应 Flowable act_re_model 表 ID_ 字段

    @Schema(description = "仿钉钉流程设计模型对象", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "仿钉钉流程设计模型对象不能为空")
    @Valid
    private BpmSimpleModelNodeVO simpleModelBody;
}
