package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignFeaturesDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeepayDesignFeaturesMapper extends BaseMapperX<DeepayDesignFeaturesDO> {

    default DeepayDesignFeaturesDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayDesignFeaturesDO>()
                .eq(DeepayDesignFeaturesDO::getChainCode, chainCode)
                .orderByDesc(DeepayDesignFeaturesDO::getId)
                .last("limit 1"));
    }
}
