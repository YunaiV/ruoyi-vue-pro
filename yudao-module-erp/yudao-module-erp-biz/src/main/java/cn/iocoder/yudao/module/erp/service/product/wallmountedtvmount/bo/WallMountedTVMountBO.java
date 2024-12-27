package cn.iocoder.yudao.module.erp.service.product.wallmountedtvmount.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 挂墙电视支架
 * 
 * @author Wqh
 */
@Data
public class WallMountedTVMountBO extends ErpProductBO {
    /**
     * VESA孔距最小宽度
     */
    private Integer widthMin;
    /**
     * VESA孔距最大宽度
     */
    private Integer widthMax;
    /**
     * VESA孔距最大长度
     */
    private Integer lengthMax;
    /**
     * VESA孔距最小长度
     */
    private Integer lengthMin;
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
     * 其他
     */
    private String otherFeatures;
}
