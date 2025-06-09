package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 头程申请表明细 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class TmsFirstMileRequestItemRespVO extends BaseVO {


    @Schema(description = "明细编号")
    @ExcelProperty("明细编号")
    private Long id;

    @Schema(description = "产品id")
    private Long productId;

    @Schema(description = "产品名称")
    @ExcelProperty("产品名称")
    private String productName;

    //带出该申请部门的 该产品sku的 中国的 仓库库存汇总
    @Schema(description = "国内仓库库存")
    @ExcelProperty("国内仓库库存")
    private Integer domesticWarehouseStock;

    //purchase_transit_qty 自动计算该sku的采购在途数量
    @Schema(description = "采购在途数量")
    @ExcelProperty("采购在途数量")
    private Integer purchaseTransitQty;

    @Schema(description = "FBA条码")
    @ExcelProperty("FBA条码")
    private String fbaBarCode;

    @Schema(description = "申请数量")
    @ExcelProperty("申请数量")
    private Integer qty;

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

    @Schema(description = "订购状态")
    @ExcelProperty(value = "订购状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.ORDER_STATUS)
    private Integer orderStatus;

    @Schema(description = "关闭状态")
    @ExcelProperty(value = "关闭状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.OFF_STATUS)
    private Integer offStatus;

    @Schema(description = "已订购数")
    @ExcelProperty("已订购数")
    private Integer orderClosedQty;

    //产品重量
    @Schema(description = "产品基础重量（kg）")
    @ExcelProperty("产品基础重量（kg）")
    private BigDecimal productWeight;

    @Schema(description = "销售公司ID")
    private Long salesCompanyId;

    @Schema(description = "销售公司名称")
    @ExcelProperty("销售公司名称")
    private String salesCompanyName;

    @Schema(description = "版本号")
    private Integer revision;


    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;
}