package cn.iocoder.yudao.module.product.api.spu;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
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
 * 商品 SPU API 接口实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuMapper productSpuMapper;

    @Override
    public List<ProductSpuRespDTO> getSpuList(Collection<Long> spuIds) {
        // TODO 需不需要判断集合中是否有 null 值
        if (CollectionUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }
        List<ProductSpuDO> productSpuDOList = productSpuMapper.selectBatchIds(spuIds);
        return ProductSpuConvert.INSTANCE.convertList2(productSpuDOList);
    }

}
