package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author jisencai
 * @table-fields : actual_qty,create_time,bin_id,expected_qty,stockCheck_id,product_id,remark,status
 */
@Schema(description = "管理后台 - 库位盘点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockCheckBinPageReqVO extends PageParam {

    @Schema(description = "盘点结果单ID", example = "5995")
    private Long stockCheckId;

    @Schema(description = "产品ID", example = "30522")
    private Long productId;

    @Schema(description = "预期库存，仓位可用库存")
    private Integer expectedQty;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "仓位ID", example = "")
    private Long binId;

    @Schema(description = "实际库存，实盘数量", example = "")
    private Integer actualQty;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "盘点状态", example = "")
    private Integer status;
}
