package cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - WMS 移库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsMovementOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "移库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "YK202605110001")
    @ExcelProperty("移库单号")
    private String no;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据日期")
    private LocalDateTime orderTime;

    @Schema(description = "移库状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "移库状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ORDER_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long sourceWarehouseId;

    @Schema(description = "来源仓库名称", example = "北京仓")
    @ExcelProperty("来源仓库")
    private String sourceWarehouseName;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long targetWarehouseId;

    @Schema(description = "目标仓库名称", example = "上海仓")
    @ExcelProperty("目标仓库")
    private String targetWarehouseName;

    @Schema(description = "移库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @ExcelProperty("移库数量")
    private BigDecimal totalQuantity;

    @Schema(description = "总金额", example = "1000.00")
    @ExcelProperty("总金额")
    private BigDecimal totalPrice;

    @Schema(description = "移库明细")
    private List<WmsMovementOrderDetailRespVO> details;

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
