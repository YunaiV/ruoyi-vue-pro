package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeepayProductMapper extends BaseMapperX<DeepayProductDO> {

    default DeepayProductDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getChainCode, chainCode));
    }

    default void updateStatusByChainCode(String chainCode, String status) {
        update(new LambdaUpdateWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getChainCode, chainCode)
                .set(DeepayProductDO::getStatus, status));
    }

}
