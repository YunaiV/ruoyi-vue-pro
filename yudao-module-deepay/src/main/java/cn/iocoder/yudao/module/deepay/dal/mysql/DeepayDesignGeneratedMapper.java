package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignGeneratedDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayDesignGeneratedMapper extends BaseMapperX<DeepayDesignGeneratedDO> {

    default List<DeepayDesignGeneratedDO> selectByChainCode(String chainCode) {
        return selectList(new LambdaQueryWrapper<DeepayDesignGeneratedDO>()
                .eq(DeepayDesignGeneratedDO::getChainCode, chainCode)
                .orderByDesc(DeepayDesignGeneratedDO::getId));
    }

    default int countByChainCode(String chainCode) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<DeepayDesignGeneratedDO>()
                .eq(DeepayDesignGeneratedDO::getChainCode, chainCode)));
    }
}
