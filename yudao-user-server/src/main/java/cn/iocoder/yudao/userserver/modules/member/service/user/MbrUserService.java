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

}
