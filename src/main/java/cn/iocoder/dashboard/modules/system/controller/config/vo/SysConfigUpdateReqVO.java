package cn.iocoder.dashboard.modules.system.controller.config.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("参数配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigUpdateReqVO extends SysConfigBaseVO {

    @ApiModelProperty(value = "参数配置序号", required = true, example = "1024")
    @NotNull(message = "参数配置编号不能为空")
    private Long id;

}
