package cn.iocoder.yudao.module.infra.controller.admin.config.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel("管理后台 - 参数配置信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigRespVO extends ConfigBaseVO {

    @ApiModelProperty(value = "参数配置序号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "参数键名", required = true, example = "yunai.db.username")
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String key;

    @ApiModelProperty(value = "参数类型", required = true, example = "1", notes = "参见 SysConfigTypeEnum 枚举")
    private Integer type;

    @ApiModelProperty(value = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

}
