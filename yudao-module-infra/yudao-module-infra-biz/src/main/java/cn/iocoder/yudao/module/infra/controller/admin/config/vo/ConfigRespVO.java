package cn.iocoder.yudao.module.infra.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(title = "管理后台 - 参数配置信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigRespVO extends ConfigBaseVO {

    @Schema(title = "参数配置序号", required = true, example = "1024")
    private Long id;

    @Schema(title = "参数键名", required = true, example = "yunai.db.username")
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String key;

    @Schema(title = "参数类型", required = true, example = "1", description = "参见 SysConfigTypeEnum 枚举")
    private Integer type;

    @Schema(title = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

}
