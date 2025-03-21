package cn.iocoder.yudao.module.srm.service.product.bedsidecabinet.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 床头柜
 * 
 * @author Wqh
 */
@Data
public class BedsideCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
