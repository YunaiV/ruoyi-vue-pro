package cn.iocoder.yudao.module.wms.api.warehouse.dto;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * @author jisencai
 * @table-fields : code,inbound_status,company_id,inbound_time,arrival_actual_time,remark,audit_status,trace_no,type,upstream_type,init_age,upstream_id,shipping_method,id,upstream_code,dept_id,arrival_plan_time,shelving_status,warehouse_id
 */
@Data
public class WmsWareHouseUpdateReqDTO {

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空", groups = {ValidationGroup.update.class})
    private Long productId;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    /**
     * 在制数量
     */
    private Integer makePendingQty;

}
