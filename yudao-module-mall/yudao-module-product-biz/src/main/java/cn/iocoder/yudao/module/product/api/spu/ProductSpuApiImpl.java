package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * todo 注释
 */
@Service
public class ProductSpuApiImpl implements ProductSpuApi {

    @Override
    public List<SpuInfoRespDTO> getSpusByIds(Collection<Long> spuIds) {
        return null;
    }

}
