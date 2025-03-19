package cn.iocoder.yudao.module.erp.service.product.ironfilingcabinet.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 铁质文件柜
 * 
 * @author Wqh
 */
@Data
public class IronFilingCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
    /**
     * 脚轮
     */
    private Boolean casters;
}
