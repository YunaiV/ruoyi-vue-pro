package cn.iocoder.yudao.module.srm.service.product.tvcabinet.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 电视柜
 * 
 * @author Wqh
 */
@Data
public class TVCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
