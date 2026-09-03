package cn.iocoder.yudao.module.pms.controller.admin.pm.project;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 项目收藏")
@RestController
@RequestMapping("/pms/pm/project-favorite")
@Validated
public class PmsProjectFavoriteController {

    @Resource
    private PmsProjectFavoriteService projectFavoriteService;

    @PostMapping("/create")
    @Operation(summary = "收藏项目")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<Boolean> createProjectFavorite(@RequestParam("projectId") Long projectId) {
        projectFavoriteService.createProjectFavorite(projectId, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消收藏项目")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<Boolean> deleteProjectFavorite(@RequestParam("projectId") Long projectId) {
        projectFavoriteService.deleteProjectFavorite(projectId, getLoginUserId());
        return success(true);
    }

}
