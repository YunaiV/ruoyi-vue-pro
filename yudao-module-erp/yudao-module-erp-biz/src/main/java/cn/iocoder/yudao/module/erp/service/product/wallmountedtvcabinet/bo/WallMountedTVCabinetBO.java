package cn.iocoder.yudao.module.erp.service.product.wallmountedtvcabinet.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 挂墙电视柜
 * 
 * @author Wqh
 */
@Data
public class WallMountedTVCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
