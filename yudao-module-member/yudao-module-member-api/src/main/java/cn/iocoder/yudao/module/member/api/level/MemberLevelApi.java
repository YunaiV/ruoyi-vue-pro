package cn.iocoder.yudao.module.member.api.level;

import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;

/**
 * 会员等级 API接口
 *
 * @author owen
 */
public interface MemberLevelApi {

    /**
     * 增加会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型 {@link MemberExperienceBizTypeEnum}
     * @param bizId      业务编号
     */
    void plusExperience(Long userId, Integer experience, Integer bizType, String bizId);
}
