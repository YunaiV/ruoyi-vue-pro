package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 好友申请记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFriendRequestMapper extends BaseMapperX<ImFriendRequestDO> {

    default ImFriendRequestDO selectLatestByFromUserIdAndToUserId(Long fromUserId, Long toUserId) {
        return selectOne(new LambdaQueryWrapperX<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getFromUserId, fromUserId)
                .eq(ImFriendRequestDO::getToUserId, toUserId)
                .orderByDesc(ImFriendRequestDO::getId)
                .last("LIMIT 1"));
    }

    default List<ImFriendRequestDO> selectMyList(Long userId, int limit) {
        // 先放扩展过滤再放双向 OR；否则 .and() 返回 LambdaQueryWrapper 基类，丢失 eqIfPresent 等扩展方法
        LambdaQueryWrapperX<ImFriendRequestDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.and(w -> w.eq(ImFriendRequestDO::getFromUserId, userId)
                        .or().eq(ImFriendRequestDO::getToUserId, userId))
                .orderByDesc(ImFriendRequestDO::getId)
                .last("LIMIT " + limit);
        return selectList(wrapper);
    }

    default int updateByIdAndHandleResult(Long id, Integer handleResult, ImFriendRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getId, id).eq(ImFriendRequestDO::getHandleResult, handleResult));
    }

}
