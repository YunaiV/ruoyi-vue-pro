package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendRequestHandleResultEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 好友申请记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFriendRequestMapper extends BaseMapperX<ImFriendRequestDO> {

    default ImFriendRequestDO selectByFromUserIdAndToUserId(Long fromUserId, Long toUserId) {
        return selectOne(new LambdaQueryWrapperX<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getFromUserId, fromUserId)
                .eq(ImFriendRequestDO::getToUserId, toUserId));
    }

    /**
     * 拉取「我相关」的好友申请列表
     */
    default List<ImFriendRequestDO> selectMyList(Long userId, LocalDateTime maxRequestUpdateTime,
                                                 Long maxId, int limit) {
        LambdaQueryWrapperX<ImFriendRequestDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.and(w -> w.eq(ImFriendRequestDO::getFromUserId, userId)
                        .or().eq(ImFriendRequestDO::getToUserId, userId));
        if (maxRequestUpdateTime != null && maxId != null) {
            wrapper.and(w -> w.lt(ImFriendRequestDO::getUpdateTime, maxRequestUpdateTime)
                    .or(n -> n.eq(ImFriendRequestDO::getUpdateTime, maxRequestUpdateTime)
                            .lt(ImFriendRequestDO::getId, maxId)));
        }
        wrapper.orderByDesc(ImFriendRequestDO::getUpdateTime)
                .orderByDesc(ImFriendRequestDO::getId)
                .last("LIMIT " + limit);
        return selectList(wrapper);
    }

    default int updateByIdAndHandleResult(Long id, Integer handleResult, ImFriendRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getId, id).eq(ImFriendRequestDO::getHandleResult, handleResult));
    }

    /**
     * 复用 (fromUserId, toUserId) 旧申请记录：覆盖申请理由 / 备注 / 来源，重置为未处理 + 清空旧处理痕迹
     * <p>
     * handleContent / handleTime 走 LambdaUpdateWrapper.set 显式置 null，updateById 默认会忽略 null 字段
     */
    default int updateByIdReset(Long id, String applyContent, String displayName, Integer addSource,
                                LocalDateTime updateTime) {
        return update(null, new LambdaUpdateWrapper<ImFriendRequestDO>()
                .eq(ImFriendRequestDO::getId, id)
                .set(ImFriendRequestDO::getApplyContent, applyContent)
                .set(ImFriendRequestDO::getDisplayName, displayName)
                .set(ImFriendRequestDO::getAddSource, addSource)
                .set(ImFriendRequestDO::getHandleResult, ImFriendRequestHandleResultEnum.UNHANDLED.getResult())
                .set(ImFriendRequestDO::getHandleContent, null)
                .set(ImFriendRequestDO::getHandleTime, null)
                .set(ImFriendRequestDO::getUpdateTime, updateTime));
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
