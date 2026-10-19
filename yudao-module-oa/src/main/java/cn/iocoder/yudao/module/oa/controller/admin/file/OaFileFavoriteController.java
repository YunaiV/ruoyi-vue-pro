package cn.iocoder.yudao.module.oa.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.oa.service.file.OaFileFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 云盘收藏")
@RestController
@RequestMapping("/oa/file-favorite")
@Validated
public class OaFileFavoriteController {

    @Resource
    private OaFileFavoriteService fileFavoriteService;

    @PostMapping("/create")
    @Operation(summary = "收藏文件")
    @Parameter(name = "nodeId", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<Boolean> createFileFavorite(@RequestParam("nodeId") Long nodeId) {
        fileFavoriteService.createFileFavorite(nodeId, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消收藏文件")
    @Parameter(name = "nodeId", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:query')")
    public CommonResult<Boolean> deleteFileFavorite(@RequestParam("nodeId") Long nodeId) {
        fileFavoriteService.deleteFileFavorite(nodeId, getLoginUserId());
        return success(true);
    }

}
