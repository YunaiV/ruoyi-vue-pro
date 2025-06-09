package cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo;

import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileStockRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 头程单明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsFirstMileItemRespVO extends BaseVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "申请单编号")
    @ExcelProperty("申请单编号")
    private String requestCode;

    @Schema(description = "申请项ID")
    @ExcelProperty("申请项ID")
    private Long requestItemId;

    @Schema(description = "产品ID")
    private Long productId;
    /**
     * FBA条码
     */
    @Schema(description = "FBA条码")
    private String fbaBarCode;

    //产品名称
    @Schema(description = "产品名称")
    @ExcelProperty("产品名称")
    private String productName;
    //产品SKU
    @Schema(description = "产品SKU")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "件数")
    @ExcelProperty("件数")
    private Integer qty;

    @Schema(description = "箱数")
    @ExcelProperty("箱数")
    private Integer boxQty;

    @Schema(description = "库存公司")
    private Long companyId;

    @Schema(description = "库存信息(分页不做渲染)")
    private List<TmsFirstMileStockRespVO> stock;
    //库存公司名称
    @Schema(description = "库存公司名称")
    @ExcelProperty("库存公司名称")
    private String companyName;

    @Schema(description = "库存归属部门ID")
    private Long deptId;

    //库存归属部门名称
    @Schema(description = "库存归属部门名称")
    @ExcelProperty("库存归属部门名称")
    private String deptName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "实际发货数")
    @ExcelProperty("实际发货数")
    private Integer outboundClosedQty;

    @Schema(description = "计划发货数")
    @ExcelProperty("计划发货数")
    private Integer outboundPlanQty;

    @Schema(description = "已入库数量")
    @ExcelProperty("已入库数量")
    private Integer inboundClosedQty;

    @Schema(description = "发出仓ID")
    private Long fromWarehouseId;

    @Schema(description = "发出仓名称")
    @ExcelProperty("发出仓名称")
    private String fromWarehouseName;

    @Schema(description = "包装长（cm）")
    @ExcelProperty("包装长（cm）")
    private BigDecimal packageLength;

    @Schema(description = "包装宽（cm）")
    @ExcelProperty("包装宽（cm）")
    private BigDecimal packageWidth;

    @Schema(description = "包装高（cm）")
    @ExcelProperty("包装高（cm）")
    private BigDecimal packageHeight;

    @Schema(description = "毛重（kg）")
    @ExcelProperty("毛重（kg）")
    private BigDecimal packageWeight;

    @Schema(description = "体积（m³）")
    @ExcelProperty("体积（m³）")
    private BigDecimal volume;

    @Schema(description = "销售公司ID")
    private Long salesCompanyId;

    @Schema(description = "销售公司名称")
    @ExcelProperty("销售公司名称")
    private String salesCompanyName;

    @Schema(description = "版本号")
    private Integer revision;
}