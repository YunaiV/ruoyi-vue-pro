package cn.iocoder.yudao.module.ai.controller.admin.skill;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillFileService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - AI 技能")
@RestController
@RequestMapping("/ai/skill")
@Validated
public class AiSkillController {

    @Resource
    private AiSkillService skillService;
    @Resource
    private AiSkillFileService skillFileService;
    @Resource
    private AiChatRoleService chatRoleService;

    @PostMapping("/create")
    @Operation(summary = "创建技能", description = "自动生成固定文件 SKILL.md、scripts/、reference/")
    @PreAuthorize("@ss.hasPermission('ai:skill:create')")
    public CommonResult<Long> createSkill(@Valid @RequestBody AiSkillSaveReqVO createReqVO) {
        return success(skillService.createSkill(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新技能", description = "名称不可修改；描述以 SKILL.md 为准")
    @PreAuthorize("@ss.hasPermission('ai:skill:update')")
    public CommonResult<Boolean> updateSkill(@Valid @RequestBody AiSkillSaveReqVO updateReqVO) {
        skillService.updateSkill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除技能", description = "同时删除全部文件；引用该技能的角色在对话中自动跳过")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:skill:delete')")
    public CommonResult<Boolean> deleteSkill(@RequestParam("id") Long id) {
        skillService.deleteSkill(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得技能")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:skill:query')")
    public CommonResult<AiSkillRespVO> getSkill(@RequestParam("id") Long id) {
        AiSkillDO skill = skillService.getSkill(id);
        if (skill == null) {
            return success(null);
        }
        return success(buildSkillList(singletonList(skill)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得技能分页")
    @PreAuthorize("@ss.hasPermission('ai:skill:query')")
    public CommonResult<PageResult<AiSkillRespVO>> getSkillPage(@Valid AiSkillPageReqVO pageReqVO) {
        PageResult<AiSkillDO> pageResult = skillService.getSkillPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        return success(new PageResult<>(buildSkillList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得技能精简列表", description = "只包含启用的技能，用于角色绑定技能的下拉选择；不返回文件内容")
    public CommonResult<List<AiSkillRespVO>> getSkillSimpleList() {
        List<AiSkillDO> list = skillService.getSkillListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, skill -> new AiSkillRespVO()
                .setId(skill.getId()).setName(skill.getName()).setDescription(skill.getDescription())));
    }

    /**
     * 拼接关联数据：文件数量、引用的角色
     */
    private List<AiSkillRespVO> buildSkillList(List<AiSkillDO> list) {
        List<Long> skillIds = convertList(list, AiSkillDO::getId);
        Map<Long, Integer> fileCountMap = skillFileService.getSkillFileCountMap(skillIds);
        List<AiChatRoleDO> chatRoles = chatRoleService.getChatRoleListBySkillIds(skillIds);
        return BeanUtils.toBean(list, AiSkillRespVO.class, skill -> skill
                .setFileCount(fileCountMap.getOrDefault(skill.getId(), 0))
                .setRoleNames(convertList(chatRoles, AiChatRoleDO::getName, chatRole -> CollUtil.contains(chatRole.getSkillIds(), skill.getId()))));
    }

}
