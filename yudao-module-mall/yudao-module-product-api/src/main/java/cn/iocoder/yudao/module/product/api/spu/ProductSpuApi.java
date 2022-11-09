package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ProductSpuApi {


    /**
     * 根据spuId列表 查询spu信息
     *
     * @param spuIds spu ID列表
     * @return spu信息列表
     */
    List<SpuInfoRespDTO> getSpusByIds(Collection<Long> spuIds);
}
