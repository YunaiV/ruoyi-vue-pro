package cn.iocoder.yudao.module.crm.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 产品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProductBaseVO {

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品编码不能为空")
    private String no;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "价格", example = "8911")
    private Long price;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "产品分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1738")
    @NotNull(message = "产品分类ID不能为空")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你说的对")
    private String description;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31926")
    @NotNull(message = "负责人的用户编号不能为空")
    private Long ownerUserId;

}
