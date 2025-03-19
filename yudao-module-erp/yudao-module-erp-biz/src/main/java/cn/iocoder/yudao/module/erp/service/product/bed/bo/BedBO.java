package cn.iocoder.yudao.module.erp.service.product.bed.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 床
 * 
 * @author Wqh
 */
@Data
public class BedBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
