package cn.iocoder.yudao.module.wms.api.warehouse.dto;

import lombok.Data;

import java.util.List;

@Data
public class WmsWarehouseSimpleDTO {
    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 产品id
     */
    private List<Long> productIds;
}
