package cn.iocoder.yudao.module.member.api.level;

import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;

/**
 * 会员等级 API 接口
 *
 * @author owen
 */
public interface MemberLevelApi {

    /**
     * 获得会员等级
     *
     * @param id 会员等级编号
     * @return 会员等级
     */
    MemberLevelRespDTO getMemberLevel(Long id);

    /**
     * 增加会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型 {@link MemberExperienceBizTypeEnum}
     * @param bizId      业务编号
     */
    void addExperience(Long userId, Integer experience, Integer bizType, String bizId);

    /**
     * 扣减会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型 {@link MemberExperienceBizTypeEnum}
     * @param bizId      业务编号
     */
    void reduceExperience(Long userId, Integer experience, Integer bizType, String bizId);

}
