package cn.iocoder.yudao.module.crm.controller.admin.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.PostRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

@Tag(name = "管理后台 - CRM 数据权限")
@RestController
@RequestMapping("/crm/permission")
@Validated
public class CrmPermissionController {

    @Resource
    private CrmPermissionService permissionService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private PostApi postApi;

    @PostMapping("/create")
    @Operation(summary = "创建数据权限")
    @PreAuthorize("@ss.hasPermission('crm:permission:create')")
    @CrmPermission(bizTypeValue = "#reqVO.bizType", bizId = "#reqVO.bizId", level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> addPermission(@Valid @RequestBody CrmPermissionCreateReqVO reqVO) {
        permissionService.createPermission(CrmPermissionConvert.INSTANCE.convert(reqVO));
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "编辑数据权限")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    @CrmPermission(bizTypeValue = "#updateReqVO.bizType", bizId = "#updateReqVO.bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> updatePermission(@Valid @RequestBody CrmPermissionUpdateReqVO updateReqVO) {
        permissionService.updatePermission(updateReqVO);
        return success(true);
    }

    // TODO @puhui999：这个要不要放到 Service 实现，让 Controller 还轻一点；
    @DeleteMapping("/delete")
    @Operation(summary = "删除数据权限")
    @Parameter(name = "ids", description = "数据权限编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    public CommonResult<Boolean> deletePermission(@RequestParam("ids") Collection<Long> ids) {
        List<CrmPermissionDO> permissions = permissionService.getPermissionList(ids);
        if (CollUtil.isEmpty(permissions)) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
        Set<Long> bizIds = convertSet(permissions, CrmPermissionDO::getBizId);
        if (bizIds.size() > 1) { // 情况一：数据权限的模块数据编号是一致的不可能存在两个 TODO @puhui999：这里可以额外说明下原因，就是批量删除权限的时候，只能属于同一个 bizId 下；
            throw exception(CRM_PERMISSION_DELETE_FAIL);
        }
        // TODO @puhui999：下面 2 个，可以忽略。简单点哈；
        if (permissions.size() != ids.size()) { // 情况二：期望数量和实际结果不一致
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
        // 情况三：不能包含负责人
        // TODO @puhui999：isOwner 可以直接放到判断里，不用单独取个变量名
        boolean isOwner = CollectionUtils.anyMatch(permissions, item -> ObjUtil.equal(item.getLevel(), CrmPermissionLevelEnum.OWNER.getLevel()));
        if (isOwner) {
            throw exception(CRM_PERMISSION_DELETE_FAIL_EXIST_OWNER);
        }
        // 校验操作人是否为负责人
        CrmPermissionDO permission = permissionService.getPermission(permissions.get(0).getBizId(), getLoginUserId());
        if (!CrmPermissionLevelEnum.isOwner(permission.getLevel())) {
            throw exception(CRM_PERMISSION_DELETE_DENIED);
        }
        // 删除数据权限
        permissionService.deletePermission(ids);
        return success(true);
    }

    // TODO @puhui999：这个要不要放到 Service 实现，让 Controller 还轻一点；
    // TODO @puhui999：delete-self 就可以啦。方法名叫 deleteSelfPermission
    @DeleteMapping("/deleteSelfPermission")
    @Operation(summary = "删除自己的数据权限")
    @Parameter(name = "id", description = "数据权限编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    public CommonResult<Boolean> deletePermission(@RequestParam("id") Long id) {
        // 校验数据存在且是自己
        CrmPermissionDO permission = permissionService.getPermission(id, getLoginUserId());
        if (permission == null) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
        // 校验是否是负责人
        if (CrmPermissionLevelEnum.isOwner(permission.getLevel())) {
            throw exception(CRM_PERMISSION_DELETE_SELF_PERMISSION_FAIL_EXIST_OWNER);
        }

        // 删除
        permissionService.deletePermission(Collections.singletonList(id));
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取数据权限列表")
    @Parameters({
            @Parameter(name = "bizType", description = "CRM 类型", required = true, example = "2"),
            @Parameter(name = "bizId", description = "CRM 类型数据编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:query')")
    public CommonResult<List<CrmPermissionRespVO>> getPermissionList(@RequestParam("bizType") Integer bizType,
                                                                     @RequestParam("bizId") Long bizId) {
        List<CrmPermissionDO> permission = permissionService.getPermissionListByBiz(bizType, bizId);
        if (CollUtil.isEmpty(permission)) {
            return success(Collections.emptyList());
        }

        // 拼接数据
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(convertSet(permission, CrmPermissionDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userList, AdminUserRespDTO::getDeptId));
        Set<Long> postIds = CollectionUtils.convertSetByFlatMap(userList, AdminUserRespDTO::getPostIds, Collection::stream);
        Map<Long, PostRespDTO> postMap = postApi.getPostMap(postIds);
        return success(CrmPermissionConvert.INSTANCE.convert(permission, userList, deptMap, postMap));
    }

}
