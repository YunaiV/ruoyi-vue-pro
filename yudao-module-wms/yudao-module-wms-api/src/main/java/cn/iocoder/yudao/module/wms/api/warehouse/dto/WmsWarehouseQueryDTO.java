package cn.iocoder.yudao.module.wms.api.warehouse.dto;

import lombok.Data;

import java.util.List;

@Data
public class WmsWarehouseQueryDTO {
    /**
     * 仓库详情
     */
    List<WmsWarehouseSimpleDTO> warehouses;
}
