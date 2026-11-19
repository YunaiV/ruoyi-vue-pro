package cn.iocoder.yudao.module.ai.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.dal.mysql.skill.AiSkillMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.tool.skill.AiSkillToolCallback;
import javax.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 技能 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiSkillServiceImpl implements AiSkillService {

    /**
     * 单次返回给模型的最大字数，超出截断
     */
    static final int READ_CONTENT_MAX_LENGTH = 16000;

    @Resource
    private AiSkillMapper skillMapper;

    @Resource
    private AiSkillFileService skillFileService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiChatRoleService chatRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkill(AiSkillSaveReqVO createReqVO) {
        // 1. 校验名称唯一
        validateSkillNameUnique(createReqVO.getName());

        // 2. 插入：描述保持单行，与 SKILL.md 头信息一致
        AiSkillDO skill = BeanUtils.toBean(createReqVO, AiSkillDO.class, o -> o.setId(null)
                .setDescription(AiSkillFileServiceImpl.toFrontmatterValue(createReqVO.getDescription())));
        skillMapper.insert(skill);

        // 3. 创建固定文件：SKILL.md + scripts/ + reference/
        skillFileService.createDefaultSkillFileList(skill);
        return skill.getId();
    }

    @Override
    public void updateSkill(AiSkillSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateSkillExists(updateReqVO.getId());

        // 2. 更新：名称创建后不可修改；描述以 SKILL.md 为准，在保存 SKILL.md 时同步
        skillMapper.updateById(new AiSkillDO().setId(updateReqVO.getId()).setVersion(updateReqVO.getVersion())
                .setStatus(updateReqVO.getStatus()).setRemark(updateReqVO.getRemark()));
    }

    @Override
    public void updateSkillDescription(Long id, String description) {
        skillMapper.updateById(new AiSkillDO().setId(id).setDescription(description));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long id) {
        // 1. 校验存在
        validateSkillExists(id);

        // 2. 删除技能及其文件；角色上的绑定保留，对话时自动跳过
        skillMapper.deleteById(id);
        skillFileService.deleteSkillFileListBySkillId(id);
    }

    @Override
    public AiSkillDO getSkill(Long id) {
        return skillMapper.selectById(id);
    }

    @Override
    public AiSkillDO validateSkillExists(Long id) {
        AiSkillDO skill = skillMapper.selectById(id);
        if (skill == null) {
            throw exception(SKILL_NOT_EXISTS);
        }
        return skill;
    }

    @Override
    public void validateSkillListEnable(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 查询时按当前租户过滤，其它租户的技能视为不存在
        List<AiSkillDO> skills = skillMapper.selectByIds(ids);
        for (Long id : ids) {
            AiSkillDO skill = CollUtil.findOne(skills, item -> ObjUtil.equal(item.getId(), id));
            if (skill == null) {
                throw exception(SKILL_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(skill.getStatus())) {
                throw exception(SKILL_DISABLE, skill.getName());
            }
        }
    }

    @Override
    public PageResult<AiSkillDO> getSkillPage(AiSkillPageReqVO pageReqVO) {
        return skillMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiSkillDO> getSkillListByStatus(Integer status) {
        return skillMapper.selectListByStatus(status);
    }

    @Override
    public List<AiSkillDO> getSkillList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return skillMapper.selectByIds(ids);
    }

    private void validateSkillNameUnique(String name) {
        if (skillMapper.selectByName(name) != null) {
            throw exception(SKILL_NAME_DUPLICATE, name);
        }
    }

    // ========== 对话 ==========

    @Override
    public List<ToolCallback> getSkillToolCallbackList(AiChatRoleDO chatRole) {
        if (chatRole == null || CollUtil.isEmpty(chatRole.getSkillIds())) {
            return Collections.emptyList();
        }
        // 已删除、停用的技能自动跳过
        List<AiSkillDO> skills = filterList(getSkillList(chatRole.getSkillIds()),
                skill -> CommonStatusEnum.isEnable(skill.getStatus()));
        if (CollUtil.isEmpty(skills)) {
            return Collections.emptyList();
        }
        return AiSkillToolCallback.buildList(chatRole.getId(), TenantContextHolder.getTenantId(), skills, this);
    }

    @Override
    public String readSkillFile(Long roleId, String skillName, String path) {
        // 1.1 校验参数
        if (StrUtil.isBlank(skillName)) {
            return "参数 name 不能为空，请传入“可用技能”清单中的技能名称";
        }
        // 1.2 校验技能：角色仍绑定该技能，技能存在（当前租户）且启用
        AiChatRoleDO chatRole = chatRoleService.getChatRole(roleId);
        AiSkillDO skill = skillMapper.selectByName(skillName);
        if (chatRole == null || skill == null || CommonStatusEnum.isDisable(skill.getStatus())
                || !CollUtil.contains(chatRole.getSkillIds(), skill.getId())) {
            return StrUtil.format("技能({})已停用或不可用", skillName);
        }

        // 2. 读取：未指定路径时读取 SKILL.md；文件列表放在正文前面，正文过长截断时仍能看到，便于模型按需继续读取
        String result;
        if (StrUtil.isEmpty(path)) {
            result = skillFileService.readSkillFile(skill, AiSkillFileDO.NAME_SKILL);
            List<String> paths = filterList(skillFileService.getSkillFilePathList(skill.getId()),
                    item -> ObjUtil.notEqual(item, AiSkillFileDO.NAME_SKILL));
            if (CollUtil.isNotEmpty(paths)) {
                result = "技能内的其它文件（需要时使用 read_skill_file 读取）：\n- " + String.join("\n- ", paths)
                        + "\n\n---\n" + result;
            }
        } else {
            result = skillFileService.readSkillFile(skill, path);
        }

        // 3. 超长截断，并明确提示
        if (result.length() > READ_CONTENT_MAX_LENGTH) {
            result = result.substring(0, READ_CONTENT_MAX_LENGTH) + StrUtil.format(
                    "\n\n[内容过长已截断：共 {} 字，本次仅返回前 {} 字]", result.length(), READ_CONTENT_MAX_LENGTH);
        }
        return result;
    }

}
