package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("管理后台 - 数据字典的表定义 Response VO")
@Data
public class SchemaTableRespVO {

    @ApiModelProperty(value = "数据库", required = true, example = "yudao")
    private String tableSchema;

    @ApiModelProperty(value = "表名称", required = true, example = "yuanma")
    private String tableName;

    @ApiModelProperty(value = "表描述", required = true, example = "芋道源码")
    private String tableComment;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
