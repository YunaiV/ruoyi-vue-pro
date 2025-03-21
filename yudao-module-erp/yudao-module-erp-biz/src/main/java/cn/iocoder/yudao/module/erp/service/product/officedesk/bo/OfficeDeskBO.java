package cn.iocoder.yudao.module.erp.service.product.officedesk.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 办公桌
 * 
 * @author Wqh
 */
@Data
public class OfficeDeskBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
