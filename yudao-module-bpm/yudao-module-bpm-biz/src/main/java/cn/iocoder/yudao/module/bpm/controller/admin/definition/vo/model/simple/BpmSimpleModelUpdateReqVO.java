package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

// TODO @jason：需要考虑，如果某个节点的配置不正确，需要有提示；具体怎么实现，可以讨论下；
@Schema(description = "管理后台 - 仿钉钉流程设计模型的新增/修改 Request VO")
@Data
public class BpmSimpleModelUpdateReqVO {

    @Schema(description = "流程模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "流程模型编号不能为空")
    private String id; // 对应 Flowable act_re_model 表 ID_ 字段

    @Schema(description = "仿钉钉流程设计模型对象", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "仿钉钉流程设计模型对象不能为空")
    @Valid
    private BpmSimpleModelNodeVO simpleModel;

}
