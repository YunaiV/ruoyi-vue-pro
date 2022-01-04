package cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@ApiModel("流程模型的导入 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModeImportReqVO extends BpmModelBaseVO {

    @ApiModelProperty(value = "BPMN 文件", required = true)
    @NotNull(message = "BPMN 文件不能为空")
    private MultipartFile bpmnFile;

}
