package cn.iocoder.yudao.module.crm.controller.admin.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.service.CrmPermissionValidateService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
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
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_DENIED;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum.getNameByType;
import static cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum.isOwner;

@Tag(name = "管理后台 - CRM 数据权限（数据团队成员操作）")
@RestController
@RequestMapping("/crm/permission")
@Validated
public class CrmPermissionController {

    @Resource
    private CrmPermissionService crmPermissionService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private List<CrmPermissionValidateService> permissionValidateServices;

    private void validatePermission(Integer bizType, Long bizId) {
        // 1. TODO 校验是否为超级管理员
        // 2. 防御一手，如果是超级管理员不校验权限还是得校验一下数据是否存在
        permissionValidateServices.forEach(item -> {
            if (!item.validateBizIdExists(bizType, bizId)) {
                throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, getNameByType(bizType));
            }
        });
        // 3. 校验数据权限 （如果存在则表示 bizId 也存在）
        CrmPermissionDO permission = crmPermissionService.getPermissionByBizTypeAndBizIdAndUserId(
                bizType, bizId, getLoginUserId());
        if (isOwner(permission.getPermissionLevel())) { // 只有负责人才可以操作团队成员
            return;
        }
        throw exception(CRM_PERMISSION_DENIED, getNameByType(bizType));
    }

    @PutMapping("/add")
    @Operation(summary = "添加团队成员")
    @PreAuthorize("@ss.hasPermission('crm:permission:create')")
    public CommonResult<Boolean> addPermission(@Valid @RequestBody CrmPermissionCreateReqVO reqVO) {
        // 1. 前置校验
        validatePermission(reqVO.getBizType(), reqVO.getBizId());

        // 2. 加入成员
        crmPermissionService.createPermission(CrmPermissionConvert.INSTANCE.convert(reqVO));
        return success(true);
    }


    @PutMapping("/update")
    @Operation(summary = "编辑团队成员")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    public CommonResult<Boolean> updatePermission(@Valid @RequestBody CrmPermissionUpdateReqVO updateReqVO) {
        // 1. 前置校验
        validatePermission(updateReqVO.getBizType(), updateReqVO.getBizId());

        // 2. 编辑团队成员
        crmPermissionService.updatePermission(CrmPermissionConvert.INSTANCE.convert(updateReqVO));
        return success(true);
    }

    @GetMapping("/delete")
    @Operation(summary = "移除团队成员")
    @Parameter(name = "id", description = "团队成员编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    public CommonResult<Boolean> deletePermission(@RequestParam("bizType") Integer bizType,
                                                  @RequestParam("bizId") Long bizId,
                                                  @RequestParam("id") Long id) {
        // 1. 前置校验
        validatePermission(bizType, bizId);

        // 2. 移除团队成员
        crmPermissionService.deletePermission(id);
        return success(true);
    }

    @GetMapping("/quit")
    @Operation(summary = "退出团队")
    @Parameters({
            @Parameter(name = "bizType", description = "CRM 类型", required = true, example = "2"),
            @Parameter(name = "bizId", description = "CRM 类型数据编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    public CommonResult<Boolean> quitPermission(@RequestParam("bizType") Integer bizType,
                                                @RequestParam("bizId") Long bizId) {
        CrmPermissionDO permission = crmPermissionService.getPermissionByBizTypeAndBizIdAndUserId(
                bizType, bizId, getLoginUserId());
        if (permission == null) { // 没有就不是团队成员
            return success(false);
        }
        crmPermissionService.deletePermission(permission.getId());
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
        List<CrmPermissionDO> permission = crmPermissionService.getPermissionByBizTypeAndBizId(bizType, bizId);
        if (CollUtil.isEmpty(permission)) {
            return success(Collections.emptyList());
        }
        permission.removeIf(item -> ObjUtil.equal(item.getUserId(), CrmPermissionDO.POOL_USER_ID)); // 排除

        // 拼接数据
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(convertSet(permission, CrmPermissionDO::getUserId));
        return success(CrmPermissionConvert.INSTANCE.convert(permission, userList));
    }

}
