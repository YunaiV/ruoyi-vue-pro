package cn.iocoder.yudao.module.infra.controller.admin.db.vo;

import lombok.*;

import java.time.LocalDateTime;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 数据源配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataSourceConfigRespVO extends DataSourceConfigBaseVO {

    @ApiModelProperty(value = "主键编号", required = true, example = "1024")
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
