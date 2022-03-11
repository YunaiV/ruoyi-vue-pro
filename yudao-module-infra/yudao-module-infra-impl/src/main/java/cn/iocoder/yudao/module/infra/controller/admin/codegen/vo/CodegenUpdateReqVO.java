package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.column.CodegenColumnBaseVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTableBaseVO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenSceneEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 代码生成表和字段的修改 Request VO")
@Data
public class CodegenUpdateReqVO {

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
    @Valid
    public static class Table extends CodegenTableBaseVO {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

        @AssertTrue(message = "上级菜单不能为空")
        public boolean isParentMenuIdValid() {
            // 生成场景为管理后台时，必须设置上级菜单，不然生成的菜单 SQL 是无父级菜单的
            return ObjectUtil.notEqual(getScene(), CodegenSceneEnum.ADMIN.getScene())
                    || getParentMenuId() != null;
        }

    }

    @ApiModel("更新表定义")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Column extends CodegenColumnBaseVO {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

    }

}
