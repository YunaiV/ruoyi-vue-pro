package cn.iocoder.yudao.coreservice.modules.member.service.user.impl;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.coreservice.modules.member.dal.mysql.user.MbrUserCoreMapper;
import cn.iocoder.yudao.coreservice.modules.member.service.user.MbrUserCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * User Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class MbrUserCoreServiceImpl implements MbrUserCoreService {

    @Resource
    private MbrUserCoreMapper userCoreMapper;

    @Override
    public MbrUserDO getUser(Long id) {
        return userCoreMapper.selectById(id);
    }

}
