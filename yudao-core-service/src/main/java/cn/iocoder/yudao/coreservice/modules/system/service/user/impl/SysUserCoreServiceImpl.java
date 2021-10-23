package cn.iocoder.yudao.coreservice.modules.system.service.user.impl;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.user.SysUserCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 后台用户 Service Core 实现
 *
 * @author 芋道源码
 */
@Service
public class SysUserCoreServiceImpl implements SysUserCoreService {

    @Resource
    private SysUserCoreMapper userCoreMapper;

    @Override
    public SysUserDO getUser(Long id) {
        return userCoreMapper.selectById(id);
    }

}
