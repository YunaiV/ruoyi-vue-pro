package cn.iocoder.yudao.userserver.modules.member.service.user;

import cn.iocoder.yudao.userserver.modules.member.dal.dataobject.user.MbrUserDO;

/**
 * 前台用户 Service 接口
 *
 * @author 芋道源码
 */
public interface MbrUserService {

    /**
     * 通过手机查询用户
     *
     * @param mobile 手机
     * @return 用户对象
     */
    MbrUserDO getUserByMobile(String mobile);

    /**
     * 更新用户的最后登陆信息
     *
     * @param id 用户编号
     * @param loginIp 登陆 IP
     */
    void updateUserLogin(Long id, String loginIp);

}
