package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * FMS 账套用户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsAccountUserMapper extends BaseMapperX<FmsAccountUserDO> {

    default FmsAccountUserDO selectByAccountSetIdAndUserId(Long accountSetId, Long userId) {
        return selectOne(FmsAccountUserDO::getAccountSetId, accountSetId,
                FmsAccountUserDO::getUserId, userId);
    }

    default List<FmsAccountUserDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<FmsAccountUserDO>()
                .eq(FmsAccountUserDO::getUserId, userId)
                .orderByDesc(FmsAccountUserDO::getDefaultStatus)
                .orderByDesc(FmsAccountUserDO::getId));
    }

    default List<FmsAccountUserDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsAccountUserDO>()
                .eq(FmsAccountUserDO::getAccountSetId, accountSetId)
                .orderByDesc(FmsAccountUserDO::getFounder)
                .orderByAsc(FmsAccountUserDO::getId));
    }

    default void deleteByAccountSetIdAndUserIds(Long accountSetId, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<FmsAccountUserDO>()
                .eq(FmsAccountUserDO::getAccountSetId, accountSetId)
                .in(FmsAccountUserDO::getUserId, userIds));
    }

    default Long selectCountByUserId(Long userId) {
        return selectCount(FmsAccountUserDO::getUserId, userId);
    }

    default void updateDefaultStatusByUserId(Long userId) {
        update(new FmsAccountUserDO().setDefaultStatus(false),
                new LambdaQueryWrapperX<FmsAccountUserDO>().eq(FmsAccountUserDO::getUserId, userId));
    }

    default void updateDefaultStatusByAccountSetIdAndUserId(Long accountSetId, Long userId) {
        update(new FmsAccountUserDO().setDefaultStatus(true), new LambdaQueryWrapperX<FmsAccountUserDO>()
                .eq(FmsAccountUserDO::getAccountSetId, accountSetId).eq(FmsAccountUserDO::getUserId, userId));
    }

}
