package cn.iocoder.yudao.module.pms.controller.admin.kb.library;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeLibraryMoveGroupReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupDO;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库分组")
@RestController
@RequestMapping("/pms/kb/group")
@Validated
public class PmsKnowledgeGroupController {

    @Resource
    private PmsKnowledgeGroupService knowledgeGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建知识库分组")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody PmsKnowledgeGroupSaveReqVO saveReqVO) {
        return success(knowledgeGroupService.createGroup(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改知识库分组")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody PmsKnowledgeGroupSaveReqVO saveReqVO) {
        knowledgeGroupService.updateGroup(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-sort")
    @Operation(summary = "修改知识库分组排序")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateGroupSort(
            @Valid @RequestBody PmsKnowledgeGroupSortReqVO sortReqVO) {
        knowledgeGroupService.updateGroupSort(sortReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库分组")
    @Parameter(name = "id", description = "知识库分组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        knowledgeGroupService.deleteGroup(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得知识库分组列表")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<List<PmsKnowledgeGroupRespVO>> getGroupList() {
        Long userId = getLoginUserId();
        List<PmsKnowledgeGroupDO> groups = knowledgeGroupService.getGroupList(userId);
        Map<Long, Integer> groupCountMap = knowledgeGroupService.getGroupLibraryCountMap(userId,
                convertSet(groups, PmsKnowledgeGroupDO::getId));
        return success(convertList(groups, group -> BeanUtils.toBean(group, PmsKnowledgeGroupRespVO.class)
                .setLibraryCount(groupCountMap.getOrDefault(group.getId(), 0))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得知识库分组")
    @Parameter(name = "id", description = "知识库分组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeGroupRespVO> getGroup(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(knowledgeGroupService.getGroup(id, getLoginUserId()),
                PmsKnowledgeGroupRespVO.class));
    }

    @PutMapping("/move")
    @Operation(summary = "移动知识库到个人分组")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> moveLibraryToGroup(
            @Valid @RequestBody PmsKnowledgeLibraryMoveGroupReqVO moveReqVO) {
        knowledgeGroupService.moveLibraryToGroup(moveReqVO, getLoginUserId());
        return success(true);
    }
}
