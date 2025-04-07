package cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 所有者库存移动详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershiopMoveItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19084")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "所有者移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16712")
    @ExcelProperty("所有者移动表ID")
    private Long ownershipMoveId;

    @Schema(description = "产品ID", example = "11262")
    @ExcelProperty("产品ID")
    private Integer productId;

    @Schema(description = "调出财务公司ID", example = "6416")
    @ExcelProperty("调出财务公司ID")
    private Long fromCompanyId;

    @Schema(description = "调出部门ID", example = "11905")
    @ExcelProperty("调出部门ID")
    private Long fromDeptId;

    @Schema(description = "调入财务公司ID", example = "29642")
    @ExcelProperty("调入财务公司ID")
    private Long toCompanyId;

    @Schema(description = "调入部门ID", example = "17818")
    @ExcelProperty("调入部门ID")
    private Long toDeptId;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("移动数量")
    private Integer qty;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}