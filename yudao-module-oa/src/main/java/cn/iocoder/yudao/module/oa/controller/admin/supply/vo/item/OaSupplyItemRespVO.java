package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item;

import cn.iocoder.yudao.module.oa.enums.supply.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 办公用品 Response VO")
@Data
public class OaSupplyItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "所属部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deptId;

    @Schema(description = "物品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A4 打印纸")
    private String name;

    @Schema(description = "物品编码", example = "BGYP0001")
    private String no;

    @Schema(description = "类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer category;

    @Schema(description = "管理类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer manageType;

    @Schema(description = "规格型号", example = "A4 80g")
    private String model;

    @Schema(description = "计量单位", example = "包")
    private String unit;

    @Schema(description = "参考单价", example = "100.00")
    private BigDecimal referencePrice;

    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer stockQuantity;

    @Schema(description = "最低库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer minStockQuantity;

    @Schema(description = "物品图片", example = "https://example.com/image.png")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "所属部门", example = "行政部")
    private String deptName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
