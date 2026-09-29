package cn.iocoder.yudao.module.ai1.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SKILL_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SKILL_NOT_EXISTS;

/**
 * AI1 SKILL Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1SkillServiceImpl implements Ai1SkillService {

    @Resource
    private Ai1SkillMapper skillMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1SkillFileService skillFileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkill(Ai1SkillSaveReqVO createReqVO) {
        // 1. 校验名称唯一
        validateSkillNameUnique(null, createReqVO.getName());

        // 2. 插入
        Ai1SkillDO skill = BeanUtils.toBean(createReqVO, Ai1SkillDO.class);
        skillMapper.insert(skill);

        // 3. 创建默认文件：SKILL.md + scripts/ + reference/
        skillFileService.createDefaultSkillFileList(skill);
        return skill.getId();
    }

    @Override
    public void updateSkill(Ai1SkillSaveReqVO updateReqVO) {
        // 1. 校验存在、名称唯一
        validateSkillExists(updateReqVO.getId());
        validateSkillNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 2. 更新
        Ai1SkillDO updateObj = BeanUtils.toBean(updateReqVO, Ai1SkillDO.class);
        skillMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long id) {
        deleteSkillListByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkillListByIds(List<Long> ids) {
        // 1. 校验存在
        ids.forEach(this::validateSkillExists);

        // 2. 删除 SKILL，并级联删除内容文件
        skillMapper.deleteByIds(ids);
        skillFileService.deleteSkillFileListBySkillIds(ids);
    }

    @Override
    public Ai1SkillDO getSkill(Long id) {
        return skillMapper.selectById(id);
    }

    @Override
    public Ai1SkillDO validateSkillExists(Long id) {
        Ai1SkillDO skill = skillMapper.selectById(id);
        if (skill == null) {
            throw exception(SKILL_NOT_EXISTS);
        }
        return skill;
    }

    @Override
    public PageResult<Ai1SkillDO> getSkillPage(Ai1SkillPageReqVO pageReqVO) {
        return skillMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1SkillDO> getSkillListByStatus(Integer status) {
        return skillMapper.selectListByStatus(status);
    }

    @Override
    public List<Ai1SkillDO> getSkillList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return skillMapper.selectByIds(ids);
    }

    @Override
    public void touchSkill(Long id) {
        skillMapper.updateById(new Ai1SkillDO().setId(id));
    }

    /**
     * 校验名称租户内唯一
     */
    private void validateSkillNameUnique(Long id, String name) {
        Ai1SkillDO skill = skillMapper.selectByName(name);
        if (skill != null && ObjUtil.notEqual(skill.getId(), id)) {
            throw exception(SKILL_NAME_DUPLICATE, name);
        }
    }

}
