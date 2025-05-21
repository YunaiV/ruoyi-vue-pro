package cn.iocoder.yudao.module.cms.controller.admin.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.*;
import cn.iocoder.yudao.module.cms.service.comment.CmsCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - CMS Comment Management")
@RestController
@RequestMapping("/cms/admin/comments")
@Validated
public class CmsCommentController {

    @Resource
    private CmsCommentService commentService;

    // Note: CmsCommentConvert is primarily used within the service impl for DO <-> RespVO.
    // Controller methods will typically receive ReqVOs and return RespVOs directly from the service.

    @PostMapping("/create")
    @Operation(summary = "Create a new comment (Admin)")
    @PreAuthorize("@ss.hasPermission('cms:comment:create')")
    public CommonResult<Long> createComment(@Valid @RequestBody CmsCommentCreateReqVO createReqVO) {
        Long commentId = commentService.createComment(createReqVO);
        return success(commentId);
    }

    @PutMapping("/update-status")
    @Operation(summary = "Update comment status")
    @Parameter(name = "id", description = "Comment ID", required = true, example = "1024")
    @Parameter(name = "status", description = "New Status (0:pending, 1:approved, 2:spam)", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('cms:comment:update')")
    public CommonResult<Boolean> updateCommentStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        commentService.updateCommentStatus(id, status);
        return success(true);
    }
    
    // If admin needs to edit more fields, an update method like this would be used:
    // @PutMapping("/update")
    // @Operation(summary = "Update a comment (Admin)")
    // @PreAuthorize("@ss.hasPermission('cms:comment:update')")
    // public CommonResult<Boolean> updateComment(@Valid @RequestBody CmsCommentUpdateReqVO updateReqVO) {
    //     commentService.updateComment(updateReqVO); // Assuming such a method exists in service
    //     return success(true);
    // }


    @DeleteMapping("/delete")
    @Operation(summary = "Delete a comment by ID")
    @Parameter(name = "id", description = "Comment ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        commentService.deleteComment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get a comment by ID")
    @Parameter(name = "id", description = "Comment ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:comment:query')")
    public CommonResult<CmsCommentRespVO> getComment(@RequestParam("id") Long id) {
        CmsCommentRespVO comment = commentService.getComment(id);
        return success(comment);
    }

    @GetMapping("/page")
    @Operation(summary = "Get comment page (Admin)")
    @PreAuthorize("@ss.hasPermission('cms:comment:query')")
    public CommonResult<PageResult<CmsCommentRespVO>> getCommentPage(@Valid CmsCommentPageReqVO pageVO) {
        PageResult<CmsCommentRespVO> pageResult = commentService.getCommentAdminPage(pageVO);
        return success(pageResult);
    }
}
