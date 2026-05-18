package cn.iocoder.yudao.module.social.service.user;

import cn.iocoder.yudao.module.social.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.social.dal.mysql.user.AppUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserServiceImpl implements UserService {

    @Resource
    private AppUserMapper appUserMapper;

    @Override
    public UserDO getOrCreateByOpenid(String openid, String unionid, String nickname, String avatarUrl) {
        UserDO existing = appUserMapper.selectByOpenid(openid);
        if (existing != null) {
            return existing;
        }
        UserDO created = UserDO.builder()
                .openid(openid)
                .unionid(unionid)
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .gender(0)
                .isRealNameVerified(0)
                .currentScore(0)
                .totalGames(0)
                .wins(0)
                .totalOrganized(0)
                .creditScore(100)
                .femaleVisibility(0)
                .discoverEnabled(1)
                .status(1)
                .build();
        appUserMapper.insert(created);
        return created;
    }

    @Override
    public UserDO getUser(Long id) {
        return appUserMapper.selectById(id);
    }

    @Override
    public UserDO getUserByOpenid(String openid) {
        return appUserMapper.selectByOpenid(openid);
    }

}
