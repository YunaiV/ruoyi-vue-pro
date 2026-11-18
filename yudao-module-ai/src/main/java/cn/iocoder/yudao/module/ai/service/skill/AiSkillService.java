package cn.iocoder.yudao.module.ai.service.skill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import jakarta.validation.Valid;
import org.springframework.ai.tool.ToolCallback;

import java.util.Collection;
import java.util.List;

/**
 * AI 技能 Service 接口
 *
 * @author 芋道源码
 */
public interface AiSkillService {

    /**
     * 创建技能，并生成固定文件 SKILL.md、scripts/、reference/
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSkill(@Valid AiSkillSaveReqVO createReqVO);

    /**
     * 更新技能：名称创建后不可修改，描述以 SKILL.md 为准，仅更新版本、状态、备注
     *
     * @param updateReqVO 更新信息
     */
    void updateSkill(@Valid AiSkillSaveReqVO updateReqVO);

    /**
     * 更新技能描述：保存 SKILL.md 时，由头信息的 description 同步
     *
     * @param id          编号
     * @param description 描述
     */
    void updateSkillDescription(Long id, String description);

    /**
     * 删除技能，并删除其全部文件；角色上的绑定保留，对话时自动跳过
     *
     * @param id 编号
     */
    void deleteSkill(Long id);

    /**
     * 获得技能
     *
     * @param id 编号
     * @return 技能
     */
    AiSkillDO getSkill(Long id);

    /**
     * 校验技能是否存在
     *
     * @param id 编号
     * @return 技能
     */
    AiSkillDO validateSkillExists(Long id);

    /**
     * 校验技能可以被角色绑定：存在（当前租户）且启用
     *
     * @param ids 编号集合
     */
    void validateSkillListEnable(Collection<Long> ids);

    /**
     * 获得技能分页
     *
     * @param pageReqVO 分页查询
     * @return 技能分页
     */
    PageResult<AiSkillDO> getSkillPage(AiSkillPageReqVO pageReqVO);

    /**
     * 获得指定状态的技能列表
     *
     * @param status 状态
     * @return 技能列表
     */
    List<AiSkillDO> getSkillListByStatus(Integer status);

    /**
     * 获得技能列表
     *
     * @param ids 编号集合
     * @return 技能列表
     */
    List<AiSkillDO> getSkillList(Collection<Long> ids);

    // ========== 对话 ==========

    /**
     * 获得角色的技能工具：read_skill、read_skill_file
     *
     * @param chatRole 角色
     * @return 技能工具列表；角色没有绑定启用的技能时，返回空列表
     */
    List<ToolCallback> getSkillToolCallbackList(AiChatRoleDO chatRole);

    /**
     * 读取角色绑定技能的文件，供技能工具调用
     *
     * 每次读取都重新校验：技能仍绑定在角色上、仍启用、文件存在；路径只允许技能内的相对路径；
     * 结果超过 16000 字时截断并注明
     *
     * @param roleId    角色编号
     * @param skillName 技能名称
     * @param path      文件相对路径；为空时读取 SKILL.md，并附上技能内的文件列表
     * @return 返回给模型的文本；读取失败、拒绝时也返回说明文本
     */
    String readSkillFile(Long roleId, String skillName, String path);

}
