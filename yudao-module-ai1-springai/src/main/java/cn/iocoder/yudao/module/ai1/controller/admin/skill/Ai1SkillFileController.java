package cn.iocoder.yudao.module.ai1.controller.admin.skill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.*;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillFileService;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - AI1 SKILL 内容文件")
@RestController
@RequestMapping("/ai1/skill/file")
@Validated
public class Ai1SkillFileController {

    @Resource
    private Ai1SkillFileService skillFileService;
    @Resource
    private Ai1SkillService skillService;

    @GetMapping("/tree")
    @Operation(summary = "获得 SKILL 文件树", description = "不返回文件内容，编辑时通过获取接口加载")
    @Parameter(name = "skillId", description = "SKILL 编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:skill:query')")
    public CommonResult<List<Ai1SkillFileRespVO>> getSkillFileTree(@RequestParam("skillId") Long skillId) {
        skillService.validateSkillExists(skillId);
        List<Ai1SkillFileDO> list = skillFileService.getSkillFileListBySkillId(skillId);
        return success(buildSkillFileTree(list));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 SKILL 文件", description = "包含文件内容")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:skill:query')")
    public CommonResult<Ai1SkillFileRespVO> getSkillFile(@RequestParam("id") Long id) {
        Ai1SkillFileDO skillFile = skillFileService.getSkillFile(id);
        return success(BeanUtils.toBean(skillFile, Ai1SkillFileRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建 SKILL 目录或文件")
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Long> createSkillFile(@Valid @RequestBody Ai1SkillFileCreateReqVO createReqVO) {
        return success(skillFileService.createSkillFile(createReqVO));
    }

    @PutMapping("/rename")
    @Operation(summary = "重命名 SKILL 目录或文件")
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Boolean> renameSkillFile(@Valid @RequestBody Ai1SkillFileRenameReqVO renameReqVO) {
        skillFileService.renameSkillFile(renameReqVO);
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移动 SKILL 目录或文件")
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Boolean> moveSkillFile(@Valid @RequestBody Ai1SkillFileMoveReqVO moveReqVO) {
        skillFileService.moveSkillFile(moveReqVO);
        return success(true);
    }

    @PutMapping("/update-content")
    @Operation(summary = "保存 SKILL 文件内容")
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Boolean> updateSkillFileContent(@Valid @RequestBody Ai1SkillFileContentReqVO contentReqVO) {
        skillFileService.updateSkillFileContent(contentReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 SKILL 目录或文件", description = "目录递归删除后代")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:skill:update')")
    public CommonResult<Boolean> deleteSkillFile(@RequestParam("id") Long id) {
        skillFileService.deleteSkillFile(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    // TODO @AI：父子的拼接，是不是可以交给前端噢？
    /**
     * 扁平节点列表组装为树：返回根级节点，子节点挂在 children；父节点缺失的孤儿节点按根级展示
     */
    private List<Ai1SkillFileRespVO> buildSkillFileTree(List<Ai1SkillFileDO> list) {
        List<Ai1SkillFileRespVO> nodes = BeanUtils.toBean(list, Ai1SkillFileRespVO.class, node -> node.setContent(null));
        Map<Long, Ai1SkillFileRespVO> nodeMap = convertMap(nodes, Ai1SkillFileRespVO::getId);
        List<Ai1SkillFileRespVO> roots = new ArrayList<>();
        for (Ai1SkillFileRespVO node : nodes) {
            Ai1SkillFileRespVO parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(node);
        }
        return roots;
    }

}
