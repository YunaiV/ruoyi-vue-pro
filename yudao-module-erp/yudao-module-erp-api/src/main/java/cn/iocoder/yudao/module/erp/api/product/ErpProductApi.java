package cn.iocoder.yudao.module.erp.api.product;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;

import java.util.List;

public interface ErpProductApi {
    /**
     * 获得所有产品DTO，根据ids，如果ids为null返回所有
     */
    List<ErpProductDTO> listProductDTOs(List<Long> ids);
}
