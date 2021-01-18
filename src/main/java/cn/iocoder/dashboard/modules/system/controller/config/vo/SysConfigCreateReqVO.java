package cn.iocoder.dashboard.modules.system.controller.config.vo;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@ApiModel("参数配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigCreateReqVO extends SysDeptBaseVO {

    @ApiModelProperty(value = "参数键名", required = true, example = "yunai.db.username")
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String key;

}
