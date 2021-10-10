package cn.iocoder.yudao.userserver.modules.member.service.user;

import cn.iocoder.yudao.framework.common.validation.Mobile;
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
     * 基于手机号创建用户。
     * 如果用户已经存在，则直接进行返回
     *
     * @param mobile 手机号
     * @param registerIp 注册 IP
     * @return 用户对象
     */
    MbrUserDO createUserIfAbsent(@Mobile String mobile, String registerIp);

    /**
     * 更新用户的最后登陆信息
     *
     * @param id 用户编号
     * @param loginIp 登陆 IP
     */
    void updateUserLogin(Long id, String loginIp);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    MbrUserDO getUser(Long id);

}
