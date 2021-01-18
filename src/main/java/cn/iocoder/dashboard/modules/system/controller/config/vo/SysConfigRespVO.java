package cn.iocoder.dashboard.modules.system.controller.config.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@ApiModel("参数配置信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigRespVO extends SysConfigBaseVO {

    @ApiModelProperty(value = "参数配置序号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "参数键名", required = true, example = "yunai.db.username")
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String key;

    @ApiModelProperty(value = "创建时间", required = true, example = "时间戳格式")
    private Date createTime;

}
