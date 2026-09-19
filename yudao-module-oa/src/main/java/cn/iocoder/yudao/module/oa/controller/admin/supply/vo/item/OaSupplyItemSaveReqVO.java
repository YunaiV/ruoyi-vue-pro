package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 办公用品新增/修改 Request VO")
@Data
public class OaSupplyItemSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "所属部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @Schema(description = "物品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A4 打印纸")
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 128, message = "物品名称长度不能超过 {max} 个字符")
    private String name;

    @Schema(description = "物品编码", example = "BGYP0001")
    @Size(max = 64, message = "物品编码长度不能超过 {max} 个字符")
    private String no;

    @Schema(description = "类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类别不能为空")
    @InEnum(value = OaSupplyCategoryEnum.class, message = "类别必须是 {value}")
    private Integer category;

    @Schema(description = "管理类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "管理类型不能为空")
    @InEnum(value = OaSupplyManageTypeEnum.class, message = "管理类型必须是 {value}")
    private Integer manageType;

    @Schema(description = "规格型号", example = "A4 80g")
    @Size(max = 128, message = "规格型号长度不能超过 {max} 个字符")
    private String model;

    @Schema(description = "计量单位", example = "包")
    @Size(max = 32, message = "计量单位长度不能超过 {max} 个字符")
    private String unit;

    @Schema(description = "参考单价", example = "100.00")
    @DecimalMin(value = "0", message = "参考单价不能小于 {value}")
    @Digits(integer = 16, fraction = 2, message = "参考单价整数位不能超过 {integer} 位，小数位不能超过 {fraction} 位")
    private BigDecimal referencePrice;

    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于 {value}")
    private Integer stockQuantity;

    @Schema(description = "最低库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "最低库存不能为空")
    @Min(value = 0, message = "最低库存不能小于 {value}")
    private Integer minStockQuantity;

    @Schema(description = "物品图片", example = "https://example.com/image.png")
    @Size(max = 2048, message = "物品图片长度不能超过 {max} 个字符")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

}
