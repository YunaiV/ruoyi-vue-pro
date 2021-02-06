package cn.iocoder.dashboard.modules.system.controller.test.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("字典类型更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysTestDemoUpdateReqVO extends SysTestDemoBaseVO {

    @ApiModelProperty(value = "字典主键", required = true, example = "1")
    @NotNull(message = "字典主键不能为空")
    private Long id;

}
