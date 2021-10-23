package cn.iocoder.yudao.userserver.modules.member.service.user.impl;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public MbrUserDO getUserByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    @Override
    public MbrUserDO createUserIfAbsent(String mobile, String registerIp) {
        // 用户已经存在
        MbrUserDO user = userMapper.selectByMobile(mobile);
        if (user != null) {
            return user;
        }
        // 用户不存在，则进行创建
        return this.createUser(mobile, registerIp);
    }

    private MbrUserDO createUser(String mobile, String registerIp) {
        // 生成密码
        String password = IdUtil.fastSimpleUUID();
        // 插入用户
        MbrUserDO user = new MbrUserDO();
        user.setMobile(mobile);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setRegisterIp(registerIp);
        userMapper.insert(user);
        return user;
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
