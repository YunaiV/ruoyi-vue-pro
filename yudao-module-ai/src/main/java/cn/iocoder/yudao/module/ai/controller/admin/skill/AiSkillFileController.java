package cn.iocoder.yudao.module.ai.controller.admin.skill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 技能文件")
@RestController
@RequestMapping("/ai/skill/file")
@Validated
public class AiSkillFileController {

    @Resource
    private AiSkillFileService skillFileService;
    @Resource
    private AiSkillService skillService;

    @GetMapping("/list")
    @Operation(summary = "获得技能文件列表", description = "平铺返回技能下的全部节点（包含文件内容）")
    @Parameter(name = "skillId", description = "技能编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai:skill:query')")
    public CommonResult<List<AiSkillFileRespVO>> getSkillFileList(@RequestParam("skillId") Long skillId) {
        skillService.validateSkillExists(skillId);
        List<AiSkillFileDO> list = skillFileService.getSkillFileListBySkillId(skillId);
        return success(BeanUtils.toBean(list, AiSkillFileRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得技能文件", description = "包含文件内容")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:skill:query')")
    public CommonResult<AiSkillFileRespVO> getSkillFile(@RequestParam("id") Long id) {
        AiSkillFileDO skillFile = skillFileService.getSkillFile(id);
        return success(BeanUtils.toBean(skillFile, AiSkillFileRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建技能目录或文件")
    @PreAuthorize("@ss.hasPermission('ai:skill:update')")
    public CommonResult<Long> createSkillFile(@Valid @RequestBody AiSkillFileCreateReqVO createReqVO) {
        return success(skillFileService.createSkillFile(createReqVO));
    }

    @PutMapping("/rename")
    @Operation(summary = "重命名技能目录或文件")
    @PreAuthorize("@ss.hasPermission('ai:skill:update')")
    public CommonResult<Boolean> renameSkillFile(@Valid @RequestBody AiSkillFileRenameReqVO renameReqVO) {
        skillFileService.renameSkillFile(renameReqVO);
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移动技能目录或文件")
    @PreAuthorize("@ss.hasPermission('ai:skill:update')")
    public CommonResult<Boolean> moveSkillFile(@Valid @RequestBody AiSkillFileMoveReqVO moveReqVO) {
        skillFileService.moveSkillFile(moveReqVO);
        return success(true);
    }

    @PutMapping("/update-content")
    @Operation(summary = "保存技能文件内容", description = "保存 SKILL.md 时校验头信息，并同步描述到技能")
    @PreAuthorize("@ss.hasPermission('ai:skill:update')")
    public CommonResult<Boolean> updateSkillFileContent(@Valid @RequestBody AiSkillFileContentReqVO contentReqVO) {
        skillFileService.updateSkillFileContent(contentReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除技能目录或文件", description = "目录递归删除后代")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:skill:delete')")
    public CommonResult<Boolean> deleteSkillFile(@RequestParam("id") Long id) {
        skillFileService.deleteSkillFile(id);
        return success(true);
    }

}
