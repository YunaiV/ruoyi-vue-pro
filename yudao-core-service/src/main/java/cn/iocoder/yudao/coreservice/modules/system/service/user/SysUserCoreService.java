package cn.iocoder.yudao.coreservice.modules.system.service.user;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;

/**
 * 后台用户 Service Core 接口
 *
 * @author 芋道源码
 */
public interface SysUserCoreService {

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    SysUserDO getUser(Long id);

}
