package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsProductRespVO;
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

    @Schema(description = "产品信息")
    private TmsProductRespVO product;

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

    //    快照产品信息
    /**
     * 包装长（mm）(单个产品)
     */
    private BigDecimal packageLength;
    /**
     * 包装宽（mm）(单个产品)
     */
    private BigDecimal packageWidth;
    /**
     * 包装高（mm）(单个产品)
     */
    private BigDecimal packageHeight;
    /**
     * 毛重（kg）(单个产品)
     */
    private BigDecimal packageWeight;
    /**
     * 基础重量(kg)(单个产品)
     */
    private BigDecimal weight;
    /**
     * 体积（mm³）(单个产品)
     */
    private BigDecimal volume;

    @Schema(description = "包装长（mm）")
    private BigDecimal totalPackageLength;

    @Schema(description = "包装宽（mm）")
    private BigDecimal totalPackageWidth;

    @Schema(description = "包装高（mm）")
    private BigDecimal totalPackageHeight;

    @Schema(description = "毛重（kg）")
    private BigDecimal totalPackageWeight;

    @Schema(description = "总净重（kg）")
    private BigDecimal totalWeight;

    @Schema(description = "总体积（mm³）")
    private BigDecimal totalVolume;

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