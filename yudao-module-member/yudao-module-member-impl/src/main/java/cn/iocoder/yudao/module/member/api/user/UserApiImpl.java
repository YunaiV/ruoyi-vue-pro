package cn.iocoder.yudao.module.member.api.user;

import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.member.convert.user.UserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.member.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 会员用户的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class UserApiImpl implements UserApi {

    @Resource
    private UserService userService;

    @Override
    public UserRespDTO getUser(Long id) {
        UserDO user = userService.getUser(id);
        return UserConvert.INSTANCE.convert2(user);
    }

}
