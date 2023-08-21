package cn.iocoder.yudao.module.member.api.level;

import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.yudao.module.member.service.level.MemberLevelService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 会员等级 API 实现类
 *
 * @author owen
 */
@Service
@Validated
public class MemberLevelApiImpl implements MemberLevelApi {

    @Resource
    private MemberLevelService memberLevelService;

    public void plusExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId) {
        memberLevelService.plusExperience(userId, experience, bizType, bizId);
    }
}
