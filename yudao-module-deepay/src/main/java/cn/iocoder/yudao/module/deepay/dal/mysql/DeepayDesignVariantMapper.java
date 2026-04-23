package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignVariantDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayDesignVariantMapper extends BaseMapperX<DeepayDesignVariantDO> {

    default List<DeepayDesignVariantDO> selectByChainCode(String chainCode) {
        return selectList(new LambdaQueryWrapper<DeepayDesignVariantDO>()
                .eq(DeepayDesignVariantDO::getChainCode, chainCode)
                .orderByDesc(DeepayDesignVariantDO::getScore));
    }

    default void markSelected(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayDesignVariantDO>()
                .eq(DeepayDesignVariantDO::getId, id)
                .set(DeepayDesignVariantDO::getSelected, 1));
    }
}
