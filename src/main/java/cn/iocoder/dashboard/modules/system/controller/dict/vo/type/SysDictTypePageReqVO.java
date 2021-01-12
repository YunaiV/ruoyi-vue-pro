package cn.iocoder.dashboard.modules.system.controller.dict.vo.type;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("字典类型分页列表 Request VO")
@Data
public class SysDictTypePageReqVO {

    @ApiModelProperty(value = "字典类型名称", example = "芋道", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "展示状态", example = "1", notes = "参见 SysCommonStatusEnum 枚举类")
    private Integer status;

}
