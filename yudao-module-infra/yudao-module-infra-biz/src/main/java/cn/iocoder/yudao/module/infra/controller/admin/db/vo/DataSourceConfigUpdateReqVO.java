package cn.iocoder.yudao.module.infra.controller.admin.db.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 数据源配置更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataSourceConfigUpdateReqVO extends DataSourceConfigBaseVO {

    @Schema(description = "主键编号", required = true, example = "1024")
    @NotNull(message = "主键编号不能为空")
    private Long id;

    @Schema(description = "密码", required = true, example = "123456")
    @NotNull(message = "密码不能为空")
    private String password;

}
