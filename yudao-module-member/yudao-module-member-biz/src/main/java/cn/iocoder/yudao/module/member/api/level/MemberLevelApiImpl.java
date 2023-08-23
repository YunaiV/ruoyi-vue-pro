package cn.iocoder.yudao.module.member.api.level;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.yudao.module.member.service.level.MemberLevelService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.EXPERIENCE_BIZ_NOT_SUPPORT;

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

    @Override
    public void addExperience(Long userId, Integer experience, Integer bizType, String bizId) {
        // TODO @疯狂：可以在 MemberExperienceBizTypeEnum 增加一个方法，获得哈。
        MemberExperienceBizTypeEnum bizTypeEnum = EnumUtil.getBy(MemberExperienceBizTypeEnum.class,
                e -> Objects.equals(bizType, e.getType()));
        if (bizTypeEnum == null) {
            throw exception(EXPERIENCE_BIZ_NOT_SUPPORT);
        }
        memberLevelService.addExperience(userId, experience, bizTypeEnum, bizId);
    }

}
