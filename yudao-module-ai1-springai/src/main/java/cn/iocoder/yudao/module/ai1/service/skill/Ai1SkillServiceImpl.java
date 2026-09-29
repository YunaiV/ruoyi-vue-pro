package cn.iocoder.yudao.module.ai1.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 SKILL Service 实现类
 *
 * // TODO @AI：“名称唯一性由应用层校验 + 数据库唯一键 (tenant_id, name, deleted_at) 共同保证”类似这种公主是，可以去掉；
 * 名称唯一性由应用层校验 + 数据库唯一键 (tenant_id, name, deleted_at) 共同保证：
 * 前者给出友好提示，后者兜底并发创建
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1SkillServiceImpl implements Ai1SkillService {

    /**
     * 名称格式：同时作为物化目录名，仅支持字母、数字、中划线
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");

    @Resource
    private Ai1SkillMapper skillMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1SkillFileService skillFileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkill(Ai1SkillSaveReqVO createReqVO) {
        // 1. 校验名称
        String name = createReqVO.getName().trim();
        validateSkillNameUnique(null, name);

        // 2. 插入
        Ai1SkillDO skill = BeanUtils.toBean(createReqVO, Ai1SkillDO.class).setName(name);
        try {
            skillMapper.insert(skill);
        } catch (DuplicateKeyException e) {
            // TODO @AI：不用考虑这个，直接抛出异常就好了；
            throw exception(SKILL_NAME_DUPLICATE, name);
        }

        // 3. 播种固定文件：SKILL.md + scripts/ + reference/
        skillFileService.createSkillSeedFiles(skill);
        return skill.getId();
    }

    @Override
    public void updateSkill(Ai1SkillSaveReqVO updateReqVO) {
        // 1. 校验存在、名称
        validateSkillExists(updateReqVO.getId());
        String name = updateReqVO.getName().trim();
        validateSkillNameUnique(updateReqVO.getId(), name);

        // 2. 更新
        Ai1SkillDO updateObj = BeanUtils.toBean(updateReqVO, Ai1SkillDO.class).setName(name);
        try {
            skillMapper.updateById(updateObj);
        } catch (DuplicateKeyException e) {
            // TODO @AI：不用考虑这个，直接抛出异常就好了；
            throw exception(SKILL_NAME_DUPLICATE, name);
        }
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

        // 2. 写入删除时间后逻辑删除，释放名称唯一键；级联删除内容文件
        skillMapper.updateDeletedAtByIds(ids, LocalDateTime.now());
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
        // TODO @AI：直接 inline 掉，简化点。
        Ai1SkillDO updateObj = new Ai1SkillDO().setId(id);
        updateObj.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(updateObj);
    }

    /**
     * 校验名称格式与租户内唯一
     */
    private void validateSkillNameUnique(Long id, String name) {
        // TODO @AI：这个是不是参数校验，vo 里面搞掉噢；
        if (!ReUtil.isMatch(NAME_PATTERN, name)) {
            throw exception(SKILL_NAME_INVALID);
        }
        Ai1SkillDO skill = skillMapper.selectByName(name);
        if (skill != null && ObjUtil.notEqual(skill.getId(), id)) {
            throw exception(SKILL_NAME_DUPLICATE, name);
        }
    }

}
