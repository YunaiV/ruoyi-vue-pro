package cn.iocoder.yudao.module.member.api.user;

import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.member.convert.user.UserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 会员用户的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MemberUserApiImpl implements MemberUserApi {

    @Resource
    private MemberUserService userService;

    @Override
    public UserRespDTO getUser(Long id) {
        MemberUserDO user = userService.getUser(id);
        return UserConvert.INSTANCE.convert2(user);
    }

    @Override
    public List<UserRespDTO> getUsers(Collection<Long> ids) {
        return UserConvert.INSTANCE.convertList2(userService.getUserList(ids));
    }

    @Override
    public List<UserRespDTO> getUserListByNickname(String nickname) {
        return UserConvert.INSTANCE.convertList2(userService.getUserListByNickname(nickname));
    }

}
