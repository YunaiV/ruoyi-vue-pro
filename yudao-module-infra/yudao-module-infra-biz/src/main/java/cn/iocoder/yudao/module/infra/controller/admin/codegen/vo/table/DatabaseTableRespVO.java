package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 数据库的表定义 Response VO")
@Data
public class DatabaseTableRespVO {

    @Schema(title = "表名称", required = true, example = "yuanma")
    private String name;

    @Schema(title = "表描述", required = true, example = "芋道源码")
    private String comment;

}
