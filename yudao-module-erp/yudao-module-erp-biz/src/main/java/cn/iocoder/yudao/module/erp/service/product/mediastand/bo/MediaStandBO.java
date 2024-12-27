package cn.iocoder.yudao.module.erp.service.product.mediastand.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 多媒体支架
 * 
 * @author Wqh
 */
@Data
public class MediaStandBO extends ErpProductBO {
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
     * 移动功能
     */
    private String mobileFunction;
    /**
     * 其他
     */
    private String otherFeatures;
}
