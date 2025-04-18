package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : ownership_move_id,from_dept_id,create_time,product_id,qty,from_company_id,to_company_id,to_dept_id
 */
@Schema(description = "管理后台 - 所有者库存移动详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockOwnershipMoveItemPageReqVO extends PageParam {

    @Schema(description = "所有者移动表ID", example = "3465")
    private Long ownershipMoveId;

    @Schema(description = "产品ID", example = "2464")
    private Long productId;

    @Schema(description = "调出财务公司ID", example = "28314")
    private Long fromCompanyId;

    @Schema(description = "调出部门ID", example = "7494")
    private Long fromDeptId;

    @Schema(description = "调入财务公司ID", example = "11668")
    private Long toCompanyId;

    @Schema(description = "调入部门ID", example = "12421")
    private Long toDeptId;

    @Schema(description = "移动数量")
    private Integer qty;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
