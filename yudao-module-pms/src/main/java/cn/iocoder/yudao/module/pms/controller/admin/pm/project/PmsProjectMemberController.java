package cn.iocoder.yudao.module.pms.controller.admin.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberSaveReqVO;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 项目成员")
@RestController
@RequestMapping("/pms/pm/project-member")
@Validated
public class PmsProjectMemberController {

    @Resource
    private PmsProjectMemberService projectMemberService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list")
    @Operation(summary = "获得项目成员列表")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-member:query')")
    public CommonResult<List<PmsProjectMemberRespVO>> getProjectMemberList(
            @RequestParam("projectId") Long projectId) {
        List<PmsProjectMemberRespVO> members = projectMemberService.getProjectMemberList(projectId, getLoginUserId());
        return success(fillProjectMemberRespVOList(members));
    }

    @PutMapping("/update-list")
    @Operation(summary = "保存项目成员及其权限级别")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-member:update')")
    public CommonResult<Boolean> updateProjectMemberList(@Valid @RequestBody PmsProjectMemberSaveReqVO saveReqVO) {
        projectMemberService.updateProjectMemberList(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "移除项目成员")
    @Parameters({
            @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024"),
            @Parameter(name = "userId", description = "后台用户编号", required = true, example = "1")
    })
    @PreAuthorize("@ss.hasPermission('pms:pm:project-member:update')")
    public CommonResult<Boolean> deleteProjectMember(@RequestParam("projectId") Long projectId,
                                                     @RequestParam("userId") Long userId) {
        projectMemberService.deleteProjectMember(projectId, userId, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/exit")
    @Operation(summary = "主动退出项目")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-member:query')")
    public CommonResult<Boolean> exitProject(@RequestParam("projectId") Long projectId) {
        projectMemberService.exitProject(projectId, getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<PmsProjectMemberRespVO> fillProjectMemberRespVOList(List<PmsProjectMemberRespVO> members) {
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        // 1. 批量查询用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(members, PmsProjectMemberRespVO::getUserId));
        // 2. 拼接用户信息
        for (PmsProjectMemberRespVO memberVO : members) {
            MapUtils.findAndThen(userMap, memberVO.getUserId(),
                    user -> memberVO.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
        }
        return members;
    }

}
