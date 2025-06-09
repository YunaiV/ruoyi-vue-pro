package cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo;

import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsCompanyRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsDeptRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsProductRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 调拨单明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsTransferItemRespVO {

    @Schema(description = "乐观锁")
    @ExcelProperty("乐观锁")
    private Integer revision;

    @Schema(description = "创建人ID")
    @ExcelProperty("创建人ID")
    private String creator;

    @Schema(description = "创建人姓名")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    @ExcelProperty("更新人ID")
    private String updater;

    @Schema(description = "更新人姓名")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "产品id")
    @ExcelProperty("产品id")
    private Long productId;

    @Schema(description = "产品")
    private TmsProductRespVO product;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer qty;

    @Schema(description = "可售库存")
    @ExcelProperty("可售库存")
    private Integer sellableQty;

    @Schema(description = "箱数")
    @ExcelProperty("箱数")
    private Integer boxQty;

    @Schema(description = "包装重量（kg）")
    @ExcelProperty("包装重量（kg）")
    private BigDecimal packageWeight;

    @Schema(description = "包装体积（m³）")
    @ExcelProperty("包装体积（m³）")
    private BigDecimal packageVolume;

    @Schema(description = "库存公司ID")
    @ExcelProperty("库存公司ID")
    private Long stockCompanyId;

    @Schema(description = "库存公司")
    private TmsCompanyRespVO stockCompany;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "实际发货数")
    @ExcelProperty("实际发货数")
    private Integer outboundClosedQty;

    @Schema(description = "已入库数")
    @ExcelProperty("已入库数")
    private Integer inboundClosedQty;

    @Schema(description = "库存归属部门ID")
    private Long deptId;

    @Schema(description = "库存归属部门")
    private TmsDeptRespVO dept;
}