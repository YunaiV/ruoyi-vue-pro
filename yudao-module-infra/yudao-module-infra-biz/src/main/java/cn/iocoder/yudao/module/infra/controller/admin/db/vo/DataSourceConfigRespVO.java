package cn.iocoder.yudao.module.infra.controller.admin.db.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 数据源配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataSourceConfigRespVO extends DataSourceConfigBaseVO {

    @Schema(description = "主键编号", required = true, example = "1024")
    private Integer id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
