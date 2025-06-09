package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : bin_move_id,create_time,product_id,qty,remark,from_bin_id,to_bin_id
 */
@Schema(description = "管理后台 - 库位移动详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockBinMoveItemPageReqVO extends PageParam {

    @Schema(description = "库位移动表ID", example = "13416")
    private Long binMoveId;

    @Schema(description = "产品ID", example = "4832")
    private Long productId;

    @Schema(description = "调出库位ID", example = "1149")
    private Long fromBinId;

    @Schema(description = "调入库位ID", example = "28214")
    private Long toBinId;

    @Schema(description = "移动数量")
    private Integer qty;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "备注", example = "")
    private String remark;
}
