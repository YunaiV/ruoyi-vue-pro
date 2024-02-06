package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.record;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.erp.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品库存明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpStockRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18909")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10625")
    private Long productId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32407")
    private Long warehouseId;

    @Schema(description = "出入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "11084")
    @ExcelProperty("出入库数量")
    private BigDecimal count;

    @Schema(description = "总库存量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4307")
    @ExcelProperty("总库存量")
    private BigDecimal totalCount;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty(value = "业务类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.STOCK_RECORD_BIZ_TYPE)
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27093")
    @ExcelProperty("业务编号")
    private Long bizId;

    @Schema(description = "业务项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23516")
    @ExcelProperty("业务项编号")
    private Long bizItemId;

    @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "Z110")
    @ExcelProperty("业务单号")
    private String bizNo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "25682")
    private String creator;

    // ========== 产品信息 ==========

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "苹果")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "水果")
    @ExcelProperty("产品分类")
    private String categoryName;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    @ExcelProperty("单位")
    private String unitName;

    // ========== 仓库信息 ==========

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    // ========== 用户信息 ==========

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("创建人")
    private String creatorName;

}