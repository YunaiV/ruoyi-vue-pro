package cn.iocoder.yudao.module.erp.service.product.tvcabinet.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

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
