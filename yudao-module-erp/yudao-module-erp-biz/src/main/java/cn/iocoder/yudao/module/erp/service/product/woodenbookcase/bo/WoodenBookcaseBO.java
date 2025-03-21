package cn.iocoder.yudao.module.erp.service.product.woodenbookcase.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 木制书架
 * 
 * @author Wqh
 */
@Data
public class WoodenBookcaseBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
