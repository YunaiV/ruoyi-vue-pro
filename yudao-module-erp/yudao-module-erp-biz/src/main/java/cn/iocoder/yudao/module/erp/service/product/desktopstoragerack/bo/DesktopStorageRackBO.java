package cn.iocoder.yudao.module.erp.service.product.desktopstoragerack.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 桌面置物架
 * 
 * @author Wqh
 */
@Data
public class DesktopStorageRackBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 收纳管理
     */
    private String storageManagement;
    /**
     * 调节脚垫
     */
    private String adjustableFootPad;
}
