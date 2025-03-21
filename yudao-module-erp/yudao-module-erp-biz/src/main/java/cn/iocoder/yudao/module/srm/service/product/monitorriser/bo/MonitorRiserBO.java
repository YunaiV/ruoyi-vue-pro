package cn.iocoder.yudao.module.srm.service.product.monitorriser.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 显示器增高架
 * 
 * @author Wqh
 */
@Data
public class MonitorRiserBO extends ErpProductBO {
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
    /**
     * 调节脚垫
     */
    private String adjustableFootPad;
    /**
     * 电子集成模块
     */
    private Boolean electronicIntegrationModules;
}
