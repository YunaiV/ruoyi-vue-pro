package cn.iocoder.yudao.module.erp.service.product.keyboardtray.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 键盘托
 * 
 * @author Wqh
 */
@Data
public class KeyboardTrayBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 电缆管理
     */
    private String cableManagement;
    /**
     * 收纳管理
     */
    private String storageManagement;
}
