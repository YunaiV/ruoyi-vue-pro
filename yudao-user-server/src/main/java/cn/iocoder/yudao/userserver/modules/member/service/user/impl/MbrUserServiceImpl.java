package cn.iocoder.yudao.userserver.modules.member.service.user.impl;

import cn.iocoder.yudao.userserver.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;

/**
 * User Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class MbrUserServiceImpl implements MbrUserService {

    @Resource
    private MbrUserMapper userMapper;

    @Override
    public MbrUserDO getUserByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        userMapper.updateById(new MbrUserDO().setId(id).setLoginIp(loginIp).setLoginDate(new Date()));
    }

    @Override
    public MbrUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

}
