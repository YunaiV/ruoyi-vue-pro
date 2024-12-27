package cn.iocoder.yudao.module.erp.service.product.sidecabinet.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 边柜
 * 
 * @author Wqh
 */
@Data
public class SideCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
