package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuMapper productSpuMapper;

    @Override
    public List<SpuInfoRespDTO> getSpuList(Collection<Long> spuIds) {
        if (CollectionUtils.isAnyEmpty(spuIds)) {
            return Collections.emptyList();
        }
        List<ProductSpuDO> productSpuDOList = productSpuMapper.selectBatchIds(spuIds);
        return ProductSpuConvert.INSTANCE.convertList2(productSpuDOList);
    }
}
