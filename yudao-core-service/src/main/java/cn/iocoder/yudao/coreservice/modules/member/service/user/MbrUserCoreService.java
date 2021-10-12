package cn.iocoder.yudao.coreservice.modules.member.service.user;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;

/**
 * 前台用户 Core Service 接口
 *
 * @author 芋道源码
 */
public interface MbrUserCoreService {
    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    MbrUserDO getUser(Long id);

}
