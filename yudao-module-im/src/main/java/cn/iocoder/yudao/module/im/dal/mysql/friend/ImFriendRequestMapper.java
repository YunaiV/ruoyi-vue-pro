package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendRequestManagerPageReqVO;
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

    /**
     * 拉取「我相关」的好友申请列表；游标分页：lastRequestId 为 null 拉首页，非 null 拉 id 严格小于它的下一页
     */
    default List<ImFriendRequestDO> selectMyList(Long userId, Long lastRequestId, int limit) {
        // 先放扩展过滤再放双向 OR；否则 .and() 返回 LambdaQueryWrapper 基类，丢失 ltIfPresent 等扩展方法
        LambdaQueryWrapperX<ImFriendRequestDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.ltIfPresent(ImFriendRequestDO::getId, lastRequestId)
                .and(w -> w.eq(ImFriendRequestDO::getFromUserId, userId)
                        .or().eq(ImFriendRequestDO::getToUserId, userId))
                .orderByDesc(ImFriendRequestDO::getId)
                .last("LIMIT " + limit);
        return selectList(wrapper);
    }

    default int updateByIdAndHandleResult(Long id, Integer handleResult, ImFriendRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getId, id).eq(ImFriendRequestDO::getHandleResult, handleResult));
    }

    default PageResult<ImFriendRequestDO> selectPage(ImFriendRequestManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFriendRequestDO>()
                .eqIfPresent(ImFriendRequestDO::getFromUserId, reqVO.getFromUserId())
                .eqIfPresent(ImFriendRequestDO::getToUserId, reqVO.getToUserId())
                .eqIfPresent(ImFriendRequestDO::getHandleResult, reqVO.getHandleResult())
                .eqIfPresent(ImFriendRequestDO::getAddSource, reqVO.getAddSource())
                .betweenIfPresent(ImFriendRequestDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImFriendRequestDO::getId));
    }

}
