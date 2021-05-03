package cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo;

import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.column.ToolCodegenColumnBaseVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.table.ToolCodegenTableBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("代码生成表和字段的修改 Request VO")
@Data
public class ToolCodegenUpdateReqVO {

    @Valid // 校验内嵌的字段
    @NotNull(message = "表定义不能为空")
    private Table table;

    @Valid // 校验内嵌的字段
    @NotNull(message = "字段定义不能为空")
    private List<Column> columns;

    @ApiModel("更新表定义")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Table extends ToolCodegenTableBaseVO {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

    }

    @ApiModel("更新表定义")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Column extends ToolCodegenColumnBaseVO {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

    }

}
