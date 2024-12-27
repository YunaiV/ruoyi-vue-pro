package cn.iocoder.yudao.module.erp.service.product.teatable.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 茶几
 * 
 * @author Wqh
 */
@Data
public class TeaTableBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
