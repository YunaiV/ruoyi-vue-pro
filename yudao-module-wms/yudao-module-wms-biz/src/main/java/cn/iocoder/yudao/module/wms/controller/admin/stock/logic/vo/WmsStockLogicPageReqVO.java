package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author jisencai
 * @table-fields : company_id,outbound_pending_qty,create_time,product_id,shelving_pending_qty,available_qty,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 逻辑库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockLogicPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "14322")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "1919")
    private Long productId;

    @Schema(description = "产品代码", example = "1919")
    @ExcelProperty("产品代码")
    private Long productCode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "库存财务主体公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "可用库存", example = "")
    private Integer[] availableQty;

    @Schema(description = "待出库库存", example = "")
    private Integer[] outboundPendingQty;

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    private Integer[] shelvingPendingQty;
}
