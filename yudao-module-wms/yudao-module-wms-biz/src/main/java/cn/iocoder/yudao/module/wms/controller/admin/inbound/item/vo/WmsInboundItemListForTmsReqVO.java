package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author jisencai
 * @table-fields : outbound_available_qty,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,inbound_dept_id,latest_flow_id,inbound_id,inbound_company_id,actual_qty,product_id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundItemListForTmsReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "")
    private Long warehouseId;

    @Schema(description = "公司ID,由用户指定", example = "")
    private Long companyId;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "入库单ID, 部门ID map", example = "")
    private List<WmsInboundItemDO> inboundDoList;
}
