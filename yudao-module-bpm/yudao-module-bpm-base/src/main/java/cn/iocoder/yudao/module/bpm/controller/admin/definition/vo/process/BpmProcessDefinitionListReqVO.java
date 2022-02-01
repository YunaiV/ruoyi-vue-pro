package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("流程定义列表 Request VO")
public class BpmProcessDefinitionListReqVO extends PageParam {

    @ApiModelProperty(value = "中断状态", example = "1", notes = "参见 SuspensionState 枚举")
    private Integer suspensionState;

}
