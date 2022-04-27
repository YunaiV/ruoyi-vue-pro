package cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 部门更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptUpdateReqVO extends DeptBaseVO {

    @ApiModelProperty(value = "部门编号", required = true, example = "1024")
    @NotNull(message = "部门编号不能为空")
    private Long id;

}
