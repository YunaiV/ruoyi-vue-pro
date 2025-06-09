package cn.iocoder.yudao.module.wms.api.warehouse.dto.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 仓库列表查询 DTO
 *
 * @author wdy
 */
@Data
@Builder
public class WmsWarehouseListReqDTO {

    /**
     * 仓库编号
     */
    private Long id;

    /**
     * 仓库编码
     */
    private String code;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 外部存储编号
     */
    private Long externalStorageId;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 邮编
     */
    private String postcode;

    /**
     * 详细地址3
     */
    private String addressLine3;

} 