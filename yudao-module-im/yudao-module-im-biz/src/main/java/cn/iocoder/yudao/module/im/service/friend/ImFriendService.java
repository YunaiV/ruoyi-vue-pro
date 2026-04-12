package cn.iocoder.yudao.module.im.service.friend;

/**
 * IM 好友关系 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFriendService {

    /**
     * 校验两个用户是否是好友关系（未删除状态）
     * <p>
     * 如果不是好友，直接抛出业务异常
     *
     * @param userId       用户编号
     * @param friendUserId 好友用户编号
     */
    void validateIsFriend(Long userId, Long friendUserId);

}
