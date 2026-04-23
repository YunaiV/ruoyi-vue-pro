package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeepayProductionMapper extends BaseMapperX<DeepayProductionDO> {

    default DeepayProductionDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayProductionDO>()
                .eq(DeepayProductionDO::getChainCode, chainCode));
    }

}
