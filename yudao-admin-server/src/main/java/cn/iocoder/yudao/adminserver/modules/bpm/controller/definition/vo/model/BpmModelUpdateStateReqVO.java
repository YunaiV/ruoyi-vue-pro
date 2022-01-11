package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("流程模型更新状态 Request VO")
@Data
public class BpmModelUpdateStateReqVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private String id;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "见 SuspensionState 枚举")
    @NotNull(message = "状态不能为空")
    private Integer state;

}
