package cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("管理后台 - 部门精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptSimpleRespVO {

    @ApiModelProperty(value = "部门编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "部门名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "父部门 ID", required = true, example = "1024")
    private Long parentId;

}
