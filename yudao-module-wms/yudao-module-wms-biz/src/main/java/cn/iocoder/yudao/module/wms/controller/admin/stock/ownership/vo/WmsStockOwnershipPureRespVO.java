package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,creator,company_id,create_time,outbound_pending_qty,available_qty,updater,update_time,product_id,shelving_pending_qty,id,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 所有者库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershipPureRespVO {

    @Schema(description = "库存财务主体公司ID", example = "")
    @ExcelProperty("库存财务主体公司ID")
    private Long companyId;

    @Schema(description = "财务公司", example = "")
    @ExcelProperty("财务公司")
    private FmsCompanySimpleRespVO company;

    @Schema(description = "库存归属部门ID", example = "")
    @ExcelProperty("库存归属部门ID")
    private Long deptId;

    @Schema(description = "部门", example = "")
    @ExcelProperty("部门")
    private DeptSimpleRespVO dept;

    @Schema(description = "库龄", example = "")
    @ExcelProperty("库龄")
    private Integer age;

}
