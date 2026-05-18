package cn.iocoder.yudao.module.social.service.user;

import cn.iocoder.yudao.module.social.dal.dataobject.user.UserDO;

public interface UserService {

    /**
     * 根据微信 openid 获取或创建 C 端用户。
     * 首次登录小程序时调用，幂等。
     *
     * @param openid     微信 openid
     * @param unionid    微信 unionid（可选）
     * @param nickname   昵称（可选，前端在用户授权后回填）
     * @param avatarUrl  头像（同上）
     * @return 用户 DO（含 id）
     */
    UserDO getOrCreateByOpenid(String openid, String unionid, String nickname, String avatarUrl);

    UserDO getUser(Long id);

    UserDO getUserByOpenid(String openid);

}
