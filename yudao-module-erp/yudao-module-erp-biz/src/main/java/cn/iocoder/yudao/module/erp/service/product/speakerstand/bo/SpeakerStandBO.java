package cn.iocoder.yudao.module.erp.service.product.speakerstand.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 音响支架
 * 
 * @author Wqh
 */
@Data
public class SpeakerStandBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 适配尺寸
     */
    private String adaptiveSize;
    /**
     * 兼容方式
     */
    private String compatibilityMode;
    /**
     * 高度调节
     */
    private String heightAdjustment;
    /**
     * 电缆管理
     */
    private String cableManagement;
    /**
     * 调节脚垫
     */
    private String adjustableFootPad;
    /**
     * 其他
     */
    private String otherFeatures;
}
