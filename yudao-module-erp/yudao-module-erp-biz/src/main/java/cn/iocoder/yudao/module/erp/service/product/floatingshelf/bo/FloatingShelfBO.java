package cn.iocoder.yudao.module.erp.service.product.floatingshelf.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 多媒体挂墙支架
 * 
 * @author Wqh
 */
@Data
public class FloatingShelfBO extends ErpProductBO {
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
     * 其他
     */
    private String otherFeatures;
}
