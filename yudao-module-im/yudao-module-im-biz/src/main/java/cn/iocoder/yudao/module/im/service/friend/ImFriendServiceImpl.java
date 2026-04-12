package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_FRIEND;

/**
 * IM 好友关系 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFriendServiceImpl implements ImFriendService {

    @Resource
    private ImFriendMapper imFriendMapper;

    // TODO @AI：逻辑这块，在优化下；
    @Override
    public void validateIsFriend(Long userId, Long friendUserId) {
        // 好友关系表中，deleted = false 的记录表示仍是好友
        // BaseMapperX 默认会过滤 deleted = true 的记录
        ImFriendDO friend = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (friend == null) {
            throw exception(FRIEND_NOT_FRIEND);
        }
    }

}
