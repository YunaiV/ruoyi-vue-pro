package cn.iocoder.yudao.module.erp.service.product.chestdrawers.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 斗柜
 * 
 * @author Wqh
 */
@Data
public class ChestDrawersBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
