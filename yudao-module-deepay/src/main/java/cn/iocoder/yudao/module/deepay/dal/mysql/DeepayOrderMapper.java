package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeepayOrderMapper extends BaseMapperX<DeepayOrderDO> {

    default DeepayOrderDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .orderByDesc(DeepayOrderDO::getId)
                .last("LIMIT 1"));
    }

    default void updateStatusByChainCode(String chainCode, String status) {
        update(new LambdaUpdateWrapper<DeepayOrderDO>()
                .eq(DeepayOrderDO::getChainCode, chainCode)
                .set(DeepayOrderDO::getStatus, status));
    }

}
