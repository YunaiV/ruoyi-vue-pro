package cn.iocoder.yudao.module.cms.controller.admin.tag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.*;
import cn.iocoder.yudao.module.cms.convert.tag.CmsTagConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;
import cn.iocoder.yudao.module.cms.service.tag.CmsTagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - CMS Tag Management")
@RestController
@RequestMapping("/cms/admin/tags")
@Validated
public class CmsTagController {

    @Resource
    private CmsTagService tagService;

    @Resource
    private CmsTagConvert tagConvert;

    @PostMapping("/create")
    @Operation(summary = "Create a new tag")
    @PreAuthorize("@ss.hasPermission('cms:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody CmsTagCreateReqVO createReqVO) {
        Long tagId = tagService.createTag(createReqVO);
        return success(tagId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update an existing tag")
    @PreAuthorize("@ss.hasPermission('cms:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody CmsTagUpdateReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a tag by ID")
    @Parameter(name = "id", description = "Tag ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get a tag by ID")
    @Parameter(name = "id", description = "Tag ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:tag:query')")
    public CommonResult<CmsTagRespVO> getTag(@RequestParam("id") Long id) {
        CmsTagDO tag = tagService.getTag(id);
        return success(tagConvert.convert(tag));
    }

    @GetMapping("/page")
    @Operation(summary = "Get tag page")
    @PreAuthorize("@ss.hasPermission('cms:tag:query')")
    public CommonResult<PageResult<CmsTagRespVO>> getTagPage(@Valid CmsTagPageReqVO pageVO) {
        PageResult<CmsTagDO> pageResult = tagService.getTagPage(pageVO);
        return success(tagConvert.convertPage(pageResult));
    }

    @GetMapping("/list-simple")
    @Operation(summary = "Get simple tag list (for dropdowns, etc.)")
    @PreAuthorize("@ss.hasPermission('cms:tag:query')")
    public CommonResult<List<CmsTagSimpleRespVO>> getSimpleTagList(CmsTagPageReqVO reqVO) {
        List<CmsTagDO> list = tagService.getTagList(reqVO);
        return success(tagConvert.convertListSimple(list));
    }
}
