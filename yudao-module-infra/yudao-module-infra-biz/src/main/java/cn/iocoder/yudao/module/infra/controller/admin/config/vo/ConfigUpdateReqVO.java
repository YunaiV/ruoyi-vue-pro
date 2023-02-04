package cn.iocoder.yudao.module.infra.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 参数配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConfigUpdateReqVO extends ConfigBaseVO {

    @Schema(description = "参数配置序号", required = true, example = "1024")
    @NotNull(message = "参数配置编号不能为空")
    private Long id;

}
