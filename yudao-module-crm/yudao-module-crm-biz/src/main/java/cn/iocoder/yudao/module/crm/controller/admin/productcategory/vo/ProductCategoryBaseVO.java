package cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 产品分类 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProductCategoryBaseVO {

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "父级 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4680")
    @NotNull(message = "父级 id 不能为空")
    private Long parentId;

}
