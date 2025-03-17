package cn.iocoder.yudao.module.wms.dal.dataobject.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 仓库 DO
 * @author 李方捷
 * @table-fields : country,code,contact_phone,city,contact_person,postcode,is_sync,mode,external_storage_id,province,address_line2,address_line1,company_name,name,id,status
 */
@TableName("wms_warehouse")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_warehouse_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsWarehouseDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
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
     * 公司名称
     */
    private String companyName;

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
    // private Boolean deleted;
    // 
    // public WmsWarehouseDO setDeleted(Boolean deleted) {
    // this.deleted = deleted;
    // return this;
    // }
}
