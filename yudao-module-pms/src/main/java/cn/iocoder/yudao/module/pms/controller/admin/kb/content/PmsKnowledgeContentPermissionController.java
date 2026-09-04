package cn.iocoder.yudao.module.pms.controller.admin.kb.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.permission.PmsKnowledgeContentPermissionRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.permission.PmsKnowledgeContentPermissionUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionMemberDO;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识内容协作权限")
@RestController
@RequestMapping("/pms/kb/content-permission")
@Validated
public class PmsKnowledgeContentPermissionController {

    @Resource
    private PmsKnowledgeContentPermissionService permissionService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/get")
    @Operation(summary = "获得知识内容协作权限")
    @Parameter(name = "id", description = "协作权限编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeContentPermissionRespVO> getContentPermission(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        PmsKnowledgeContentPermissionDO permission = permissionService.getContentPermission(id, userId);
        List<PmsKnowledgeContentPermissionMemberDO> members = permissionService.getContentPermissionMemberList(id, userId);
        Integer currentUserLevel = permissionService.getCurrentUserContentPermissionLevel(
                id, permission.getLibraryId(), userId);
        return success(buildPermissionRespVO(permission, members, currentUserLevel));
    }

    @PutMapping("/update")
    @Operation(summary = "更新知识内容协作权限")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateContentPermission(@Valid @RequestBody PmsKnowledgeContentPermissionUpdateReqVO updateReqVO) {
        permissionService.updateContentPermission(updateReqVO, getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private PmsKnowledgeContentPermissionRespVO buildPermissionRespVO(PmsKnowledgeContentPermissionDO permission,
                                                                      List<PmsKnowledgeContentPermissionMemberDO> members,
                                                                      Integer currentUserLevel) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(members, PmsKnowledgeContentPermissionMemberDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(members, PmsKnowledgeContentPermissionMemberDO::getDeptId));
        // 拼接 VO 返回
        PmsKnowledgeContentPermissionRespVO permissionVO = BeanUtils.toBean(permission, PmsKnowledgeContentPermissionRespVO.class)
                .setCreatorUserId(NumberUtils.parseLong(permission.getCreator()))
                .setCurrentUserLevel(currentUserLevel)
                .setMembers(BeanUtils.toBean(members, PmsKnowledgeContentPermissionRespVO.Member.class));
        permissionVO.getMembers().forEach(member -> {
            findAndThen(userMap, member.getUserId(), user -> member.setUserName(user.getNickname()));
            findAndThen(deptMap, member.getDeptId(), dept -> member.setDeptName(dept.getName()));
        });
        return permissionVO;
    }

}
