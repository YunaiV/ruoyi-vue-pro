package cn.iocoder.yudao.module.member.service.level;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
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
    private MemberLevelLogService memberLevelLogService;
    @Resource
    private MemberExperienceLogService memberExperienceLogService;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserLevel(MemberUserDO user, Long levelId, String reason) {
        // 未调整的情况1
        if (user.getLevelId() == null && levelId == null) {
            return;
        }
        // 未调整的情况2
        if (ObjUtil.equal(user.getLevelId(), levelId)) {
            return;
        }

        // 需要后台用户填写为什么调整会员的等级
        if (StrUtil.isBlank(reason)) {
            throw exception(LEVEL_REASON_NOT_EXISTS);
        }

        int experience;
        int totalExperience = 0;
        // 记录等级变动
        if (levelId == null) {
            experience = -user.getExperience();

            // 取消了会员的等级
            memberLevelLogService.createCancelLog(user.getId(), reason);
            memberUserMapper.cancelUserLevel(user.getId());
        } else {
            MemberLevelDO level = validateLevelExists(levelId);
            // 变动经验值 = 等级的升级经验 - 会员当前的经验；正数为增加经验，负数为扣减经验
            experience = level.getExperience() - user.getExperience();
            // 会员当前的经验 = 等级的升级经验
            totalExperience = level.getExperience();

            memberLevelLogService.createAdjustLog(user, level, experience, reason);

            // 更新会员表上的等级编号、经验值
            updateUserLevelIdAndExperience(user.getId(), levelId, totalExperience);
        }


        // 记录会员经验变动
        memberExperienceLogService.createAdjustLog(user.getId(), experience, totalExperience);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void plusExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId) {
        if (experience == 0) {
            return;
        }

        MemberUserDO user = memberUserMapper.selectById(userId);
        if (user.getExperience() == null) {
            user.setExperience(0);
        }

        // 防止扣出负数
        int userExperience = NumberUtil.max(user.getExperience() + experience, 0);

        // 创建经验记录
        memberExperienceLogService.createBizLog(userId, experience, userExperience, bizType, bizId);

        // 计算会员等级
        Long levelId = calcLevel(user, userExperience);

        // 更新会员表上的等级编号、经验值
        updateUserLevelIdAndExperience(user.getId(), levelId, userExperience);
    }

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
    private Long calcLevel(MemberUserDO user, int userExperience) {
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

        // 保存等级变更记录
        memberLevelLogService.createAutoUpgradeLog(user, matchLevel);
        return matchLevel.getId();
    }
}
