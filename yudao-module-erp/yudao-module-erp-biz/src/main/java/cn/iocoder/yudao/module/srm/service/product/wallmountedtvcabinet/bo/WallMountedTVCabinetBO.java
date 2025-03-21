package cn.iocoder.yudao.module.srm.service.product.wallmountedtvcabinet.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

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
