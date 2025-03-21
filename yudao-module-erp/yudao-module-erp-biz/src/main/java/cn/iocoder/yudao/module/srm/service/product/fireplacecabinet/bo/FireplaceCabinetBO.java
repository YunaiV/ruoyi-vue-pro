package cn.iocoder.yudao.module.srm.service.product.fireplacecabinet.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

/**
 * 壁炉柜
 * 
 * @author Wqh
 */
@Data
public class FireplaceCabinetBO extends ErpProductBO {
    /**
     * 承重
     */
    private Integer loadCapacity;
}
