package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    default List<ImGroupMemberDO> selectListByGroupIdAndStatusAndRoles(Long groupId, Integer status,
                                                                        Collection<Integer> roles) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getStatus, status)
                .in(ImGroupMemberDO::getRole, roles));
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

    @SuppressWarnings("UnusedReturnValue")
    default int updateListByGroupIdAndUserIds(Long groupId, Collection<Long> userIds, ImGroupMemberDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .in(ImGroupMemberDO::getUserId, userIds));
    }

    default Long selectCountByGroupIdAndRoleAndStatus(Long groupId, Integer role, Integer status) {
        return selectCount(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getRole, role)
                .eq(ImGroupMemberDO::getStatus, status));
    }

    /**
     * 批量按 group_id 统计指定状态的成员数
     */
    default Map<Long, Long> selectCountMapByGroupIdsAndStatus(Collection<Long> groupIds, Integer status) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = selectMaps(Wrappers.<ImGroupMemberDO>query()
                .select("group_id AS groupId", "COUNT(*) AS cnt")
                .in("group_id", groupIds)
                .eq("status", status)
                .groupBy("group_id"));
        // 转换成 Map<Long, Long>
        Map<Long, Long> result = new HashMap<>(rows.size());
        rows.forEach(row -> result.put(
                ((Number) row.get("groupId")).longValue(),
                ((Number) row.get("cnt")).longValue()));
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateMuteEndTimeNull(Long id) {
        return update(null, Wrappers.<ImGroupMemberDO>lambdaUpdate()
                .eq(ImGroupMemberDO::getId, id)
                .set(ImGroupMemberDO::getMuteEndTime, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateRejoinFields(Long id, Integer status, LocalDateTime joinTime,
                                   Integer role, Integer addSource, Long inviterUserId) {
        return update(null, Wrappers.<ImGroupMemberDO>lambdaUpdate()
                .eq(ImGroupMemberDO::getId, id)
                .set(ImGroupMemberDO::getStatus, status)
                .set(ImGroupMemberDO::getJoinTime, joinTime)
                .set(ImGroupMemberDO::getRole, role)
                .set(ImGroupMemberDO::getAddSource, addSource)
                .set(ImGroupMemberDO::getInviterUserId, inviterUserId)
                .set(ImGroupMemberDO::getQuitTime, null)
                .set(ImGroupMemberDO::getMuteEndTime, null));
    }

}
