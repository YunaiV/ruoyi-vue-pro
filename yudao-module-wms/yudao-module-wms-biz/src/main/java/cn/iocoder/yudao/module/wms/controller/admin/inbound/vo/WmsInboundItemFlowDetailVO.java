package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author jisencai
 * @table-fields : tenant_id,no,creator,inbound_status,company_id,create_time,inbound_time,arrival_actual_time,audit_status,creator_comment,source_bill_id,trace_no,type,updater,update_time,init_age,shipping_method,source_bill_no,source_bill_type,id,dept_id,arrival_plan_time,warehouse_id
 */
@Schema(description = "管理后台 - 入库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemFlowDetailVO {

    private Long id;

    private Long inboundItemId;

    private Long inboundId;

    private Long productId;

    private Integer outboundAvailableQty;

    private Long warehouseId;

    private Long binId;

    private Date createTime;

}
