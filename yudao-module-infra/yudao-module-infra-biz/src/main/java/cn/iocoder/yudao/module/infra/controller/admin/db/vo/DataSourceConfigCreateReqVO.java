package cn.iocoder.yudao.module.infra.controller.admin.db.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 数据源配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataSourceConfigCreateReqVO extends DataSourceConfigBaseVO {

    @Schema(description = "密码", required = true, example = "123456")
    @NotNull(message = "密码不能为空")
    private String password;

}
