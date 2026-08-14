package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsInitialBalanceDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * FMS 科目期初余额 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsInitialBalanceMapper extends BaseMapperX<FmsInitialBalanceDO> {

    default List<FmsInitialBalanceDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsInitialBalanceDO>()
                .eq(FmsInitialBalanceDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsInitialBalanceDO::getSubjectId)
                .orderByAsc(FmsInitialBalanceDO::getId));
    }

    default FmsInitialBalanceDO selectByAccountSetIdAndSubjectId(Long accountSetId, Long subjectId) {
        return selectOne(FmsInitialBalanceDO::getAccountSetId, accountSetId,
                FmsInitialBalanceDO::getSubjectId, subjectId);
    }

    default List<FmsInitialBalanceDO> selectListByAccountSetIdAndSubjectIds(
            Long accountSetId, Collection<Long> subjectIds) {
        return selectList(new LambdaQueryWrapperX<FmsInitialBalanceDO>()
                .eq(FmsInitialBalanceDO::getAccountSetId, accountSetId)
                .in(FmsInitialBalanceDO::getSubjectId, subjectIds));
    }

    default Long selectCountByAccountSetIdAndSubjectIds(
            Long accountSetId, Collection<Long> subjectIds) {
        return selectCount(new LambdaQueryWrapperX<FmsInitialBalanceDO>()
                .eq(FmsInitialBalanceDO::getAccountSetId, accountSetId)
                .in(FmsInitialBalanceDO::getSubjectId, subjectIds));
    }

    default void updateSubject(Long accountSetId, Long subjectId, FmsInitialBalanceDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<FmsInitialBalanceDO>()
                .eq(FmsInitialBalanceDO::getAccountSetId, accountSetId)
                .eq(FmsInitialBalanceDO::getSubjectId, subjectId));
    }

}
