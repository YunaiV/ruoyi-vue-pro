package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignVersionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayDesignVersionMapper extends BaseMapperX<DeepayDesignVersionDO> {

    default List<DeepayDesignVersionDO> selectByChainCode(String chainCode) {
        return selectList(new LambdaQueryWrapper<DeepayDesignVersionDO>()
                .eq(DeepayDesignVersionDO::getChainCode, chainCode)
                .orderByAsc(DeepayDesignVersionDO::getVersion));
    }

    default int countByChainCode(String chainCode) {
        Long count = selectCount(new LambdaQueryWrapper<DeepayDesignVersionDO>()
                .eq(DeepayDesignVersionDO::getChainCode, chainCode));
        return count == null ? 0 : count.intValue();
    }

}
