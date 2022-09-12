package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SPU API 实现类
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
public class ProductSpuApiImpl implements ProductSpuApi {

    @Override
    public List<SpuInfoRespDTO> getSpuList(Collection<Long> spuIds) {
        return null;
    }

}
