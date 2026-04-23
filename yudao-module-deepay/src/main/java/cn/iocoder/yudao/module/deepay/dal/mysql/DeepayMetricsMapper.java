package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeepayMetricsMapper extends BaseMapperX<DeepayMetricsDO> {

    default void markSoldByChainCode(String chainCode) {
        update(new LambdaUpdateWrapper<DeepayMetricsDO>()
                .eq(DeepayMetricsDO::getChainCode, chainCode)
                .set(DeepayMetricsDO::getSold, 1));
    }

}
