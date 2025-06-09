package cn.iocoder.yudao.module.wms.api.warehouse.dto;

import lombok.Data;

/**
 * 仓库返回 DTO
 *
 * @author wdy
 */
@Data
public class WmsWarehouseDTO {
    private Long id;

    /**
     * 属性/模式 : 0-自营;1-三方;2-平台；
     */
    private Integer mode;

    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 外部存储ID
     */
    private Long externalStorageId;

    /**
     * 详细地址3
     */
    private String addressLine3;

    /**
     * 国家
     */
    private String country;

    /**
     * 省/州
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 详细地址1
     */
    private String addressLine1;

    /**
     * 详细地址2
     */
    private String addressLine2;

    /**
     * 邮编
     */
    private String postcode;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系的话
     */
    private String contactPhone;

    /**
     * 库存同步：0-关闭；1-开启；
     */
    private Integer isSync;

    /**
     * 状态：0-不可用；1-可用
     */
    private Integer status;

}
