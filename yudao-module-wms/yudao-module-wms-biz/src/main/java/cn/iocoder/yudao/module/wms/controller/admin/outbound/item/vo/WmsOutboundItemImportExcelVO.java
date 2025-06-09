package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,latest_flow_id,updater,inbound_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情 Import VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class WmsOutboundItemImportExcelVO {

    @ExcelProperty("SKU")
    public String productCode;

    @ExcelProperty("仓位")
    public String binCode;
    /**
     * 仓位ID
     **/
    @ExcelIgnore
    public Long binId;

    /**
     * 产品ID
     **/
    @ExcelIgnore
    public Long productId;



    @ExcelProperty("计划出库量")
    private Integer planQty;

    @ExcelProperty("备注")
    private String remark;



}
