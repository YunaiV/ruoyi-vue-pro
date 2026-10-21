package cn.iocoder.yudao.module.ai1.controller.admin.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI1 SKILL")
@RestController
@RequestMapping("/ai1/skill")
@Validated
public class Ai1SkillController {

    @Resource
    private Ai1SkillService skillService;

    @PostMapping("/create")
    @Operation(summary = "创建 SKILL", description = "自动播种固定文件 SKILL.md、scripts/、reference/")
    @PreAuthorize("@ss.hasPermission('ai1:skill:create')")
    public CommonResult<Long> createSkill(@Valid @RequestBody Ai1SkillSaveReqVO createReqVO) {
        return success(skillService.createSkill(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 SKILL")
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Boolean> updateSkill(@Valid @RequestBody Ai1SkillSaveReqVO updateReqVO) {
        skillService.updateSkill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 SKILL", description = "级联删除内容文件")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:skill:delete')")
    public CommonResult<Boolean> deleteSkill(@RequestParam("id") Long id) {
        skillService.deleteSkill(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除 SKILL", description = "级联删除内容文件")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:skill:delete')")
    public CommonResult<Boolean> deleteSkillList(@RequestParam("ids") List<Long> ids) {
        skillService.deleteSkillListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 SKILL")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:skill:query')")
    public CommonResult<Ai1SkillRespVO> getSkill(@RequestParam("id") Long id) {
        Ai1SkillDO skill = skillService.getSkill(id);
        return success(BeanUtils.toBean(skill, Ai1SkillRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 SKILL 分页")
    @PreAuthorize("@ss.hasPermission('ai1:skill:query')")
    public CommonResult<PageResult<Ai1SkillRespVO>> getSkillPage(@Valid Ai1SkillPageReqVO pageReqVO) {
        PageResult<Ai1SkillDO> pageResult = skillService.getSkillPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, Ai1SkillRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得 SKILL 精简列表", description = "只包含开启状态，用于 Agent 绑定 SKILL 的下拉选择")
    public CommonResult<List<Ai1SkillRespVO>> getSkillSimpleList() {
        List<Ai1SkillDO> list = skillService.getSkillListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, skill -> new Ai1SkillRespVO()
                .setId(skill.getId()).setName(skill.getName()).setDescription(skill.getDescription())));
    }

}
