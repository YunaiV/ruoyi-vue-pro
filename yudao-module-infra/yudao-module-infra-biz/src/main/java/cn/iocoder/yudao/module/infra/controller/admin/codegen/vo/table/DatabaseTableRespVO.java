package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 数据库的表定义 Response VO")
@Data
public class DatabaseTableRespVO {

    @ApiModelProperty(value = "表名称", required = true, example = "yuanma")
    private String name;

    @ApiModelProperty(value = "表描述", required = true, example = "芋道源码")
    private String comment;

}
