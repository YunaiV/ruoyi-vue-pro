package cn.iocoder.yudao.module.system.controller.dept.vo.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "岗位导出 Request VO", description = "参数和 SysPostExcelVO 是一致的")
@Data
public class SysPostExportReqVO {

    @ApiModelProperty(value = "岗位编码", example = "yudao", notes = "模糊匹配")
    private String code;

    @ApiModelProperty(value = "岗位名称", example = "芋道", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "展示状态", example = "1", notes = "参见 SysCommonStatusEnum 枚举类")
    private Integer status;

}
