package cn.iocoder.yudao.module.crm.controller.admin.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_NOT_EXISTS;

@Tag(name = "管理后台 - CRM 数据权限（数据团队成员操作）")
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

    @PutMapping("/add")
    @Operation(summary = "添加团队成员")
    @PreAuthorize("@ss.hasPermission('crm:permission:create')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#reqVO.bizType", bizId = "#reqVO.bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> addPermission(@Valid @RequestBody CrmPermissionCreateReqVO reqVO) {
        permissionService.createPermission(CrmPermissionConvert.INSTANCE.convert(reqVO));
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "领取公海数据")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    public CommonResult<Boolean> receive(@RequestParam("bizType") Integer bizType, @RequestParam("bizId") Long bizId) {
        permissionService.receiveBiz(bizType, bizId, getLoginUserId());
        return success(true);
    }

    @PutMapping("/put-pool")
    @Operation(summary = "数据放入公海")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#bizType", bizId = "#bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> putPool(@RequestParam(value = "bizType") Integer bizType, @RequestParam("bizId") Long bizId) {
        permissionService.putPool(bizType, bizId, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "编辑团队成员权限")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#updateReqVO.bizType", bizId = "#updateReqVO.bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> updatePermission(@Valid @RequestBody CrmPermissionUpdateReqVO updateReqVO) {
        permissionService.updatePermission(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "移除团队成员")
    @Parameters({
            @Parameter(name = "bizType", description = "CRM 类型", required = true, example = "2"),
            @Parameter(name = "bizId", description = "CRM 类型数据编号", required = true, example = "1024"),
            @Parameter(name = "ids", description = "团队成员编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#bizType", bizId = "#bizId"
            , level = CrmPermissionLevelEnum.OWNER) // 为了校验权限请求必须带上 bizType 和  bizId
    public CommonResult<Boolean> deletePermission(@RequestParam("bizType") Integer bizType,
                                                  @RequestParam("bizId") Long bizId,
                                                  @RequestParam("ids") Collection<Long> ids) {
        permissionService.deletePermission(ids);
        return success(true);
    }

    @DeleteMapping("/quit-team")
    @Operation(summary = "退出团队")
    @Parameters({
            @Parameter(name = "id", description = "团队成员编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    public CommonResult<Boolean> deletePermission(@RequestParam("id") Long id) {
        // 校验数据存在且是自己
        CrmPermissionDO permission = permissionService.getPermissionByIdAndUserId(id, getLoginUserId());
        if (permission == null) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }

        // 删除
        permissionService.deletePermission(Collections.singletonList(id));
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取团队成员")
    @Parameters({
            @Parameter(name = "bizType", description = "CRM 类型", required = true, example = "2"),
            @Parameter(name = "bizId", description = "CRM 类型数据编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:query')")
    public CommonResult<List<CrmPermissionRespVO>> getPermissionList(@RequestParam("bizType") Integer bizType,
                                                                     @RequestParam("bizId") Long bizId) {
        List<CrmPermissionDO> permission = permissionService.getPermissionByBizTypeAndBizId(bizType, bizId);
        if (CollUtil.isEmpty(permission)) {
            return success(Collections.emptyList());
        }
        // TODO @puhui999：池子的逻辑；
        // 判断是否是公海数据
        Predicate<CrmPermissionDO> filter = item -> ObjUtil.equal(item.getUserId(), CrmPermissionDO.POOL_USER_ID);
        if (anyMatch(permission, filter)) {
            permission.removeIf(filter); // 排除
        }

        // 拼接数据
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(convertSet(permission, CrmPermissionDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userList, AdminUserRespDTO::getDeptId));
        Set<Long> postIds = userList.stream().flatMap(item -> item.getPostIds().stream()).collect(Collectors.toSet());
        Map<Long, PostRespDTO> postMap = postApi.getPostMap(postIds);
        return success(CrmPermissionConvert.INSTANCE.convert(permission, userList, deptMap, postMap));
    }

}
