package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @table-fields : inbound_id,source_item_id,left_quantity,inbound_status,actual_quantity,create_time,product_id,plan_quantity,shelved_quantity
 */
@Schema(description = "管理后台 - 入库单详情分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsPickupPendingPageReqVO extends PageParam {

    /**
     * 入库单编号
     **/
    @Schema(description = "入库单编号", example = "29327")
    private String inboundCode;

    /**
     * 入库状态
     **/
    @Schema(description = "入库状态", example = "0/1")
    private String inboundStatus;


    /**
     * 仓库ID
     **/
    @Schema(description = "仓库ID", example = "32")
    private Long warehouseId;


    /**
     * 产品ID
     **/
    @Schema(description = "产品ID", example = "29327")
    private Long productId;


}
