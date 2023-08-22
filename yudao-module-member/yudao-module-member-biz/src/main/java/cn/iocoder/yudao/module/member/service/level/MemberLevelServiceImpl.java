package cn.iocoder.yudao.module.member.service.level;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateLevelReqVO;
import cn.iocoder.yudao.module.member.convert.level.MemberLevelConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelMapper;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 会员等级 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class MemberLevelServiceImpl implements MemberLevelService {

    @Resource
    private MemberLevelMapper levelMapper;
    @Resource
    private MemberLevelRecordService memberLevelRecordService;
    @Resource
    private MemberExperienceRecordService memberExperienceRecordService;
    @Resource
    private MemberUserMapper memberUserMapper;

    @Override
    public Long createLevel(MemberLevelCreateReqVO createReqVO) {
        // 校验配置是否有效
        validateConfigValid(null, createReqVO.getName(), createReqVO.getLevel(), createReqVO.getExperience());

        // 插入
        MemberLevelDO level = MemberLevelConvert.INSTANCE.convert(createReqVO);
        levelMapper.insert(level);
        // 返回
        return level.getId();
    }

    @Override
    public void updateLevel(MemberLevelUpdateReqVO updateReqVO) {
        // 校验存在
        validateLevelExists(updateReqVO.getId());
        // 校验配置是否有效
        validateConfigValid(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getLevel(), updateReqVO.getExperience());

        // 更新
        MemberLevelDO updateObj = MemberLevelConvert.INSTANCE.convert(updateReqVO);
        levelMapper.updateById(updateObj);
    }

    @Override
    public void deleteLevel(Long id) {
        // 校验存在
        validateLevelExists(id);
        // 校验分组下是否有用户
        validateLevelHasUser(id);
        // 删除
        levelMapper.deleteById(id);
    }

    @VisibleForTesting
    MemberLevelDO validateLevelExists(Long id) {
        MemberLevelDO levelDO = levelMapper.selectById(id);
        if (levelDO == null) {
            throw exception(LEVEL_NOT_EXISTS);
        }
        return levelDO;
    }

    @VisibleForTesting
    void validateNameUnique(List<MemberLevelDO> list, Long id, String name) {
        for (MemberLevelDO levelDO : list) {
            if (ObjUtil.notEqual(levelDO.getName(), name)) {
                continue;
            }

            if (id == null || !id.equals(levelDO.getId())) {
                throw exception(LEVEL_NAME_EXISTS, levelDO.getName());
            }
        }
    }

    @VisibleForTesting
    void validateLevelUnique(List<MemberLevelDO> list, Long id, Integer level) {
        for (MemberLevelDO levelDO : list) {
            if (ObjUtil.notEqual(levelDO.getLevel(), level)) {
                continue;
            }

            if (id == null || !id.equals(levelDO.getId())) {
                throw exception(LEVEL_VALUE_EXISTS, levelDO.getLevel(), levelDO.getName());
            }
        }
    }

    @VisibleForTesting
    void validateExperienceOutRange(List<MemberLevelDO> list, Long id, Integer level, Integer experience) {
        for (MemberLevelDO levelDO : list) {
            if (levelDO.getId().equals(id)) {
                continue;
            }

            if (levelDO.getLevel() < level) {
                // 经验大于前一个等级
                if (experience <= levelDO.getExperience()) {
                    throw exception(LEVEL_EXPERIENCE_MIN, levelDO.getName(), levelDO.getExperience());
                }
            } else if (levelDO.getLevel() > level) {
                //小于下一个级别
                if (experience >= levelDO.getExperience()) {
                    throw exception(LEVEL_EXPERIENCE_MAX, levelDO.getName(), levelDO.getExperience());
                }
            }
        }
    }

    @VisibleForTesting
    void validateConfigValid(Long id, String name, Integer level, Integer experience) {
        List<MemberLevelDO> list = levelMapper.selectList();
        // 校验名称唯一
        validateNameUnique(list, id, name);
        // 校验等级唯一
        validateLevelUnique(list, id, level);
        // 校验升级所需经验是否有效: 大于前一个等级，小于下一个级别
        validateExperienceOutRange(list, id, level, experience);
    }

    @VisibleForTesting
    void validateLevelHasUser(Long id) {
        Long count = memberUserMapper.selectCountByLevelId(id);
        if (count > 0) {
            throw exception(GROUP_HAS_USER);
        }
    }

    @Override
    public MemberLevelDO getLevel(Long id) {
        return levelMapper.selectById(id);
    }

    @Override
    public List<MemberLevelDO> getLevelList(Collection<Long> ids) {
        return levelMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberLevelDO> getLevelPage(MemberLevelPageReqVO pageReqVO) {
        return levelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberLevelDO> getLevelListByStatus(Integer status) {
        return levelMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserLevel(MemberUserUpdateLevelReqVO updateReqVO) {
        MemberUserDO user = memberUserMapper.selectById(updateReqVO.getId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        // 等级未发生变化
        if (ObjUtil.equal(user.getLevelId(), updateReqVO.getLevelId())) {
            return;
        }

        int experience;
        int totalExperience = 0;
        // 记录等级变动
        if (updateReqVO.getLevelId() == null) {
            // 取消用户等级时，为扣减经验
            experience = -user.getExperience();

            // 取消了会员的等级
            memberLevelRecordService.createCancelLog(user.getId(), updateReqVO.getReason());
            memberUserMapper.updateUserLevelToNull(user.getId());
        } else {
            MemberLevelDO level = validateLevelExists(updateReqVO.getLevelId());
            // 变动经验值 = 等级的升级经验 - 会员当前的经验；正数为增加经验，负数为扣减经验
            experience = level.getExperience() - user.getExperience();
            // 会员当前的经验 = 等级的升级经验
            totalExperience = level.getExperience();

            memberLevelRecordService.createAdjustLog(user, level, experience, updateReqVO.getReason());

            // 更新会员表上的等级编号、经验值
            updateUserLevelIdAndExperience(user.getId(), updateReqVO.getLevelId(), totalExperience);
        }

        // 记录会员经验变动
        memberExperienceRecordService.createAdjustLog(user.getId(), experience, totalExperience);
    }

    // TODO @疯狂：方法名，建议改成 increase 或者 add 经验，和项目更统一一些
    // TODO @疯狂：bizType 改成具体数值，主要是枚举在 api 不好传递，rpc 情况下
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plusExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId) {
        if (experience == 0) {
            return;
        }

        MemberUserDO user = memberUserMapper.selectById(userId);

        // 防止扣出负数
        int userExperience = NumberUtil.max(user.getExperience() + experience, 0);
        // 创建经验记录
        memberExperienceRecordService.createBizLog(userId, experience, userExperience, bizType, bizId);

        // 计算会员等级
        MemberLevelDO newLevel = calculateNewLevel(user, userExperience);
        Long newLevelId = null;
        if (newLevel != null) {
            newLevelId = newLevel.getId();
            // 保存等级变更记录
            memberLevelRecordService.createAutoUpgradeLog(user, newLevel);
        }

        // 更新会员表上的等级编号、经验值
        updateUserLevelIdAndExperience(user.getId(), newLevelId, userExperience);
    }

    // TODO @疯狂：让 memberUserService 那开个方法；每个模块，不直接操作对方的 mapper；
    private void updateUserLevelIdAndExperience(Long userId, Long levelId, Integer experience) {
        memberUserMapper.updateById(new MemberUserDO()
                .setId(userId)
                .setLevelId(levelId).setExperience(experience)
        );
    }

    /**
     * 计算会员等级
     *
     * @param user           会员
     * @param userExperience 会员当前的经验值
     * @return 会员等级编号，null表示无变化
     */
    private MemberLevelDO calculateNewLevel(MemberUserDO user, int userExperience) {
        List<MemberLevelDO> list = getEnableLevelList();
        if (CollUtil.isEmpty(list)) {
            log.warn("计算会员等级失败：会员等级配置不存在");
            return null;
        }

        MemberLevelDO matchLevel = list.stream()
                .filter(level -> userExperience >= level.getExperience())
                .max(Comparator.nullsFirst(Comparator.comparing(MemberLevelDO::getLevel)))
                .orElse(null);
        if (matchLevel == null) {
            log.warn("计算会员等级失败：未找到会员{}经验{}对应的等级配置", user.getId(), userExperience);
            return null;
        }

        // 等级没有变化
        if (ObjectUtil.equal(matchLevel.getId(), user.getLevelId())) {
            return null;
        }

        return matchLevel;
    }
}
