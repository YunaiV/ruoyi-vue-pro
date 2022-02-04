package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@ApiModel(value = "管理后台 - 流程模型的导入 Request VO", description = "相比流程模型的新建来说，只是多了一个 bpmnFile 文件")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModeImportReqVO extends BpmModelCreateReqVO {

    @ApiModelProperty(value = "BPMN 文件", required = true)
    @NotNull(message = "BPMN 文件不能为空")
    private MultipartFile bpmnFile;

}
