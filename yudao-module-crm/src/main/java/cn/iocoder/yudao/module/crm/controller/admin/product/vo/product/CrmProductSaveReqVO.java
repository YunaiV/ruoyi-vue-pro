package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmProductStatusParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmProductUnitParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 产品创建/修改 Request VO")
@Data
public class CrmProductSaveReqVO {

    @Schema(description = "产品编号", example = "20529")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "好产品")
    @NotNull(message = "产品名称不能为空")
    @DiffLogField(name = "产品名称")
    private String name;

    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12306")
    @NotNull(message = "产品编码不能为空")
    @DiffLogField(name = "产品编码")
    private String no;

    @Schema(description = "单位", example = "2")
    @DiffLogField(name = "单位", function = CrmProductUnitParseFunction.NAME)
    private Integer unit;

    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "8911")
    @NotNull(message = "价格不能为空")
    @DiffLogField(name = "价格")
    private BigDecimal price;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "上架")
    @NotNull(message = "状态不能为空")
    @DiffLogField(name = "状态", function = CrmProductStatusParseFunction.NAME)
    private Integer status;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "产品分类编号不能为空")
    @DiffLogField(name = "产品分类编号")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你说的对")
    @DiffLogField(name = "产品描述")
    private String description;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31926")
    @NotNull(message = "负责人的用户编号不能为空")
    private Long ownerUserId;

}
