package cn.iocoder.dashboard.modules.system.controller.dept.vo.dept;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("部门列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptListReqVO extends SysDeptBaseVO {

    @ApiModelProperty(value = "部门名称", example = "芋道", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "展示状态", example = "1", notes = "参见 SysCommonStatusEnum 枚举类")
    private Integer status;

}
