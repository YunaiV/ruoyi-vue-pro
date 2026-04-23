package cn.iocoder.yudao.module.deepay.service.product;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * {@link ProductService} 实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public DeepayStyleChainDO getByChainCode(String chainCode) {
        return deepayStyleChainMapper.selectOne(
                new LambdaQueryWrapper<DeepayStyleChainDO>()
                        .eq(DeepayStyleChainDO::getChainCode, chainCode));
    }

}
