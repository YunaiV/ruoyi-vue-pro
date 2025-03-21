package cn.iocoder.yudao.module.erp.service.product.ironwoodfilingcabinet.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 铁木文件柜
 * 
 * @author Wqh
 */
@Data
public class IronWoodFilingCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 收纳管理
     */
    private String storageManagement;
    /**
     * 脚轮
     */
    private Boolean casters;
}
