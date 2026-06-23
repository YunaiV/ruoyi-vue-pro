package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 好友关系 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFriendMapper extends BaseMapperX<ImFriendDO> {

    default ImFriendDO selectByUserIdAndFriendUserId(Long userId, Long friendUserId) {
        return selectOne(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .eq(ImFriendDO::getFriendUserId, friendUserId));
    }

    default List<ImFriendDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .orderByDesc(ImFriendDO::getId));
    }

    /**
     * 增量拉取当前用户的好友关系（含已删除，按 update_time + id 正向游标）
     *
     * @param userId         当前用户编号
     * @param lastUpdateTime 上次拉取到的更新时间；首次拉取传 null
     * @param lastId         上次拉取到的记录编号；首次拉取传 null
     * @param limit          拉取数量
     * @return 好友关系列表
     */
    default List<ImFriendDO> selectPullListByUserId(Long userId, Long lastUpdateTime, Long lastId, Integer limit) {
        LambdaQueryWrapperX<ImFriendDO> query = new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId);
        if (lastUpdateTime != null && lastId != null) {
            LocalDateTime lastTime = LocalDateTimeUtil.of(lastUpdateTime);
            query.and(w -> w.gt(ImFriendDO::getUpdateTime, lastTime)
                    .or(n -> n.eq(ImFriendDO::getUpdateTime, lastTime).gt(ImFriendDO::getId, lastId)));
        }
        return selectList(query.orderByAsc(ImFriendDO::getUpdateTime).orderByAsc(ImFriendDO::getId)
                .last("LIMIT " + limit));
    }

    default List<ImFriendDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .eq(ImFriendDO::getStatus, status)
                .orderByDesc(ImFriendDO::getId));
    }

    default List<ImFriendDO> selectListByUserIdAndFriendUserIdsAndStatus(Long userId,
                                                                        Collection<Long> friendUserIds,
                                                                        Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .eq(ImFriendDO::getUserId, userId)
                .in(ImFriendDO::getFriendUserId, friendUserIds)
                .eq(ImFriendDO::getStatus, status));
    }

    default List<ImFriendDO> selectListByUserIdsAndFriendUserIdAndStatus(Collection<Long> userIds,
                                                                        Long friendUserId,
                                                                        Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFriendDO>()
                .in(ImFriendDO::getUserId, userIds)
                .eq(ImFriendDO::getFriendUserId, friendUserId)
                .eq(ImFriendDO::getStatus, status));
    }

    default PageResult<ImFriendDO> selectPage(ImFriendManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFriendDO>()
                .eqIfPresent(ImFriendDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ImFriendDO::getFriendUserId, reqVO.getFriendUserId())
                .eqIfPresent(ImFriendDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImFriendDO::getSilent, reqVO.getSilent())
                .betweenIfPresent(ImFriendDO::getAddTime, reqVO.getAddTime())
                .orderByDesc(ImFriendDO::getId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateReAddFields(Long id, Integer status, LocalDateTime addTime,
                                  LocalDateTime updateTime,
                                  Boolean silent, Boolean pinned, Boolean blocked,
                                  String displayName, Integer addSource) {
        return update(null, Wrappers.<ImFriendDO>lambdaUpdate()
                .eq(ImFriendDO::getId, id)
                .set(ImFriendDO::getStatus, status)
                .set(ImFriendDO::getAddTime, addTime)
                .set(ImFriendDO::getUpdateTime, updateTime)
                .set(ImFriendDO::getSilent, silent)
                .set(ImFriendDO::getPinned, pinned)
                .set(ImFriendDO::getBlocked, blocked)
                .set(ImFriendDO::getDisplayName, displayName)
                .set(ImFriendDO::getAddSource, addSource)
                .set(ImFriendDO::getDeleteTime, null));
    }

}
