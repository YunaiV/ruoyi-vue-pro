package cn.iocoder.yudao.module.oa.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.node.*;
import cn.iocoder.yudao.module.oa.service.file.OaFileNodeService;
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
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 云盘文件")
@RestController
@RequestMapping("/oa/file-node")
@Validated
public class OaFileNodeController {

    @Resource
    private OaFileNodeService fileNodeService;

    @GetMapping("/get-storage")
    @Operation(summary = "获得本人云盘空间")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<OaFileStorageRespVO> getFileStorage() {
        return success(fileNodeService.getFileStorage(getLoginUserId()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得云盘文件分页")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<PageResult<OaFileNodeRespVO>> getFileNodePage(@Valid OaFileNodePageReqVO pageReqVO) {
        return success(fileNodeService.getFileNodePage(pageReqVO, getLoginUserId()));
    }

    @GetMapping("/directory-list")
    @Operation(summary = "获得本人可用目录列表")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<List<OaFileNodeRespVO>> getFileDirectoryList() {
        return success(fileNodeService.getFileDirectoryList(getLoginUserId()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建云盘文件或目录")
    @PreAuthorize("@ss.hasPermission('oa:file:create')")
    public CommonResult<Long> createFileNode(@Valid @RequestBody OaFileNodeSaveReqVO reqVO) {
        return success(fileNodeService.createFileNode(reqVO, getLoginUserId()));
    }

    @PutMapping("/update-name")
    @Operation(summary = "重命名云盘文件")
    @PreAuthorize("@ss.hasPermission('oa:file:update')")
    public CommonResult<Boolean> updateFileNodeName(@Valid @RequestBody OaFileNodeRenameReqVO reqVO) {
        fileNodeService.updateFileNodeName(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-parent")
    @Operation(summary = "移动云盘文件")
    @PreAuthorize("@ss.hasPermission('oa:file:update')")
    public CommonResult<Boolean> updateFileNodeParent(@Valid @RequestBody OaFileNodeMoveReqVO reqVO) {
        fileNodeService.updateFileNodeParent(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制云盘文件或目录")
    @PreAuthorize("@ss.hasPermission('oa:file:create')")
    public CommonResult<Long> copyFileNode(@Valid @RequestBody OaFileNodeCopyReqVO reqVO) {
        return success(fileNodeService.copyFileNode(reqVO, getLoginUserId()));
    }

    @PutMapping("/recycle")
    @Operation(summary = "移入回收站")
    @Parameter(name = "id", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:delete')")
    public CommonResult<Boolean> recycleFileNode(@RequestParam("id") Long id) {
        fileNodeService.recycleFileNode(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/restore")
    @Operation(summary = "恢复回收站文件")
    @Parameter(name = "id", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:delete')")
    public CommonResult<Boolean> restoreFileNode(@RequestParam("id") Long id) {
        fileNodeService.restoreFileNode(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "彻底删除回收站文件")
    @Parameter(name = "id", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:delete')")
    public CommonResult<Boolean> deleteFileNode(@RequestParam("id") Long id) {
        fileNodeService.deleteFileNode(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件详情")
    @Parameter(name = "id", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<OaFileNodeRespVO> getFileNode(@RequestParam("id") Long id) {
        return success(fileNodeService.getFileNode(id, getLoginUserId()));
    }

}
