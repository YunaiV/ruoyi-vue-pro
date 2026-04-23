package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductionPlanDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeepayProductionPlanMapper extends BaseMapperX<DeepayProductionPlanDO> {

    default DeepayProductionPlanDO selectLatestByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayProductionPlanDO>()
                .eq(DeepayProductionPlanDO::getChainCode, chainCode)
                .orderByDesc(DeepayProductionPlanDO::getCreatedAt)
                .last("LIMIT 1"));
    }

    default List<DeepayProductionPlanDO> selectPendingPlans() {
        return selectList(new LambdaQueryWrapper<DeepayProductionPlanDO>()
                .eq(DeepayProductionPlanDO::getStatus, "PENDING"));
    }

    default void markInProduction(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayProductionPlanDO>()
                .eq(DeepayProductionPlanDO::getId, id)
                .set(DeepayProductionPlanDO::getStatus, "IN_PRODUCTION")
                .set(DeepayProductionPlanDO::getUpdatedAt, LocalDateTime.now()));
    }

    default void markCompleted(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayProductionPlanDO>()
                .eq(DeepayProductionPlanDO::getId, id)
                .set(DeepayProductionPlanDO::getStatus, "COMPLETED")
                .set(DeepayProductionPlanDO::getUpdatedAt, LocalDateTime.now()));
    }

}
