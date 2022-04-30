package cn.iocoder.yudao.module.infra.controller.admin.db.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 数据源配置更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataSourceConfigUpdateReqVO extends DataSourceConfigBaseVO {

    @ApiModelProperty(value = "主键编号", required = true, example = "1024")
    @NotNull(message = "主键编号不能为空")
    private Long id;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotNull(message = "密码不能为空")
    private String password;

}
