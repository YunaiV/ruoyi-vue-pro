package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import org.apache.ibatis.annotations.Mapper;

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

}
