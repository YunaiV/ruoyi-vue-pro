package cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail.WmsCheckOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - WMS 盘库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsCheckOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "盘库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PK202605110001")
    @ExcelProperty("盘库单号")
    private String no;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据日期")
    private LocalDateTime orderTime;

    @Schema(description = "盘库状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "盘库状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ORDER_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "北京仓")
    @ExcelProperty("仓库")
    private String warehouseName;

    @Schema(description = "盈亏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @ExcelProperty("盈亏数量")
    private BigDecimal totalQuantity;

    @Schema(description = "总金额（账面金额）", example = "1000.00")
    @ExcelProperty("总金额")
    private BigDecimal totalPrice;

    @Schema(description = "实际金额", example = "980.00")
    @ExcelProperty("实际金额")
    private BigDecimal actualPrice;

    @Schema(description = "盘库明细")
    private List<WmsCheckOrderDetailRespVO> details;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建者", example = "1")
    private String creator;
    @Schema(description = "创建者名称", example = "芋道")
    private String creatorName;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "1")
    private String updater;
    @Schema(description = "更新者名称", example = "芋道")
    private String updaterName;

}
