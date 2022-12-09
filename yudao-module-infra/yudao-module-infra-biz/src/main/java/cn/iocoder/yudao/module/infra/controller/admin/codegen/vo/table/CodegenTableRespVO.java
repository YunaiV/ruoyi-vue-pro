package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 代码生成表定义 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CodegenTableRespVO extends CodegenTableBaseVO {

    @Schema(title = "编号", required = true, example = "1")
    private Long id;

    @Schema(title = "主键编号", required = true, example = "1024")
    private Integer dataSourceConfigId;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(title = "更新时间", required = true)
    private LocalDateTime updateTime;

}
