package cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("代码生成字段定义 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ToolCodegenColumnRespVO extends ToolCodegenColumnBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
