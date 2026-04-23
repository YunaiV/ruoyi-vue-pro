package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 群成员 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapperX<ImGroupMemberDO> {

    default List<ImGroupMemberDO> selectListByGroupId(Long groupId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>().eq(ImGroupMemberDO::getGroupId, groupId));
    }

    default ImGroupMemberDO selectByGroupIdAndUserId(Long groupId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getUserId, userId));
    }

    default List<ImGroupMemberDO> selectListByGroupIdAndUserIds(Long groupId, Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .in(ImGroupMemberDO::getUserId, userIds));
    }

    default List<ImGroupMemberDO> selectListByGroupIdAndStatus(Long groupId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getStatus, status));
    }

    default List<ImGroupMemberDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getUserId, userId)
                .eq(ImGroupMemberDO::getStatus, status));
    }

    /**
     * 查询用户已退群的成员记录
     * <p>
     * 当 {@code minQuitTime} 非空时额外按 {@code quitTime ≥ minQuitTime} 过滤。
     *
     * @param userId      用户编号
     * @param minQuitTime 最早退群时间（含），可空
     * @return 已退群成员记录列表
     */
    default List<ImGroupMemberDO> selectQuitListByUserId(Long userId, LocalDateTime minQuitTime) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getUserId, userId)
                .eq(ImGroupMemberDO::getStatus, CommonStatusEnum.DISABLE.getStatus())
                .geIfPresent(ImGroupMemberDO::getQuitTime, minQuitTime));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByGroupIdAndStatus(Long groupId, Integer oldStatus, ImGroupMemberDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getStatus, oldStatus));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByGroupIdAndUserIdsAndStatus(Long groupId, Collection<Long> userIds,
                                                   Integer oldStatus, ImGroupMemberDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .in(ImGroupMemberDO::getUserId, userIds)
                .eq(ImGroupMemberDO::getStatus, oldStatus));
    }

}