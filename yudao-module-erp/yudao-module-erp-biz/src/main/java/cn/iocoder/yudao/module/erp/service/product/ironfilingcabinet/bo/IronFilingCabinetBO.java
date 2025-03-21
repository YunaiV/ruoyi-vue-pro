package cn.iocoder.yudao.module.erp.service.product.ironfilingcabinet.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

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
