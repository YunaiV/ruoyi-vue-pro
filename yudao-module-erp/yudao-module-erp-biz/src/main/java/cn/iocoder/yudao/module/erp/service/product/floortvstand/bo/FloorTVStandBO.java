package cn.iocoder.yudao.module.erp.service.product.floortvstand.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 落地电视支架
 * 
 * @author Wqh
 */
@Data
public class FloorTVStandBO extends ErpProductBO {
    /**
     * VESA孔距最小宽度
     */
    private Integer vesaWidthMin;
    /**
     * VESA孔距最大宽度
     */
    private Integer vesaWidthMax;
    /**
     * VESA孔距最大长度
     */
    private Integer vesaLengthMax;
    /**
     * VESA孔距最小长度
     */
    private Integer vesaLengthMin;
    /**
     * 电视尺寸最小值
     */
    private Integer tvSizeMin;
    /**
     * 电视尺寸最大值
     */
    private Integer tvSizeMax;
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 中心高度最小值（最低高度）
     */
    private Integer centerHeightMin;
    /**
     * 中心高度最大值（最高高度）
     */
    private Integer centerHeightMax;
    /**
     * 电视旋转
     */
    private String tvRotation;
    /**
     * 电视俯仰
     */
    private String tvTilt;
    /**
     * 高度调节
     */
    private String heightAdjustment;
    /**
     * 横竖屏旋转
     */
    private String horizontalScreenRotation;
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
