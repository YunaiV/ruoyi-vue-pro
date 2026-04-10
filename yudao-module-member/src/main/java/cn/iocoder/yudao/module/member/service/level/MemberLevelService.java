package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelListReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateLevelReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 会员等级 Service 接口
 *
 * @author owen
 */
public interface MemberLevelService {

    /**
     * 创建会员等级
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLevel(@Valid MemberLevelCreateReqVO createReqVO);

    /**
     * 更新会员等级
     *
     * @param updateReqVO 更新信息
     */
    void updateLevel(@Valid MemberLevelUpdateReqVO updateReqVO);

    /**
     * 删除会员等级
     *
     * @param id 编号
     */
    void deleteLevel(Long id);

    /**
     * 获得会员等级
     *
     * @param id 编号
     * @return 会员等级
     */
    MemberLevelDO getLevel(Long id);

    /**
     * 获得会员等级列表
     *
     * @param ids 编号
     * @return 会员等级列表
     */
    List<MemberLevelDO> getLevelList(Collection<Long> ids);

    /**
     * 获得会员等级列表
     *
     * @param listReqVO 查询参数
     * @return 会员等级列表
     */
    List<MemberLevelDO> getLevelList(MemberLevelListReqVO listReqVO);

    /**
     * 获得指定状态的会员等级列表
     *
     * @param status 状态
     * @return 会员等级列表
     */
    List<MemberLevelDO> getLevelListByStatus(Integer status);

    /**
     * 获得开启状态的会员等级列表
     *
     * @return 会员等级列表
     */
    default List<MemberLevelDO> getEnableLevelList() {
        return getLevelListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    /**
     * 修改会员的等级
     *
     * @param updateReqVO 修改参数
     */
    void updateUserLevel(MemberUserUpdateLevelReqVO updateReqVO);

    /**
     * 增加会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型
     * @param bizId      业务编号
     */
    void addExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId);

}
