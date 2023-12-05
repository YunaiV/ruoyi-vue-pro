package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO @zange-ok：需要加 CRM 前置噢
/**
 * 产品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmProductBaseVO {

    // TODO @zange：example 要写哈；主要是接口文档，可以基于 example 可以生产请求参数

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "好产品")
    @NotNull(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12306")
    @NotNull(message = "产品编码不能为空")
    private String no;

    @Schema(description = "单位", example = "2")
    private String unit;

    @Schema(description = "价格", example = "8911")
    private Long price;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "上架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "产品分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "产品分类ID不能为空")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你说的对")
    private String description;

    // TODO @zange-ok：这个字段只有 create 可以传递，update 不传递；所以放到 create 和 resp 里；

}
