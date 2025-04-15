package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo;

import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,create_time,from_company_id,updater,ownership_move_id,update_time,from_dept_id,product_id,qty,id,to_company_id,to_dept_id
 */
@Schema(description = "管理后台 - 所有者库存移动详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershipMoveItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20590")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "所有者移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3465")
    @ExcelProperty("所有者移动表ID")
    private Long ownershipMoveId;

    @Schema(description = "产品ID", example = "2464")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "调出财务公司ID", example = "28314")
    @ExcelProperty("调出财务公司ID")
    private Long fromCompanyId;

    @Schema(description = "调出部门ID", example = "7494")
    @ExcelProperty("调出部门ID")
    private Long fromDeptId;

    @Schema(description = "调入财务公司ID", example = "11668")
    @ExcelProperty("调入财务公司ID")
    private Long toCompanyId;

    @Schema(description = "调入部门ID", example = "12421")
    @ExcelProperty("调入部门ID")
    private Long toDeptId;

    @Schema(description = "调出部门", example = "")
    @ExcelProperty("调出部门")
    private DeptSimpleRespVO fromDept;

    @Schema(description = "调入部门", example = "")
    @ExcelProperty("调入部门")
    private DeptSimpleRespVO toDept;


    @Schema(description = "调出部门", example = "")
    @ExcelProperty("调出部门")
    private FmsCompanySimpleRespVO fromCompany;

    @Schema(description = "调入部门", example = "")
    @ExcelProperty("调入部门")
    private FmsCompanySimpleRespVO toCompany;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("移动数量")
    private Integer qty;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;
}
