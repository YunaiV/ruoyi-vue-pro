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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

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
    private DeptApi deptApi;
    @Resource
    private PostApi postApi;

    @PutMapping("/add")
    @Operation(summary = "添加团队成员")
    @PreAuthorize("@ss.hasPermission('crm:permission:create')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#reqVO.bizType", bizId = "#reqVO.bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> addPermission(@Valid @RequestBody CrmPermissionCreateReqVO reqVO) {
        // 2. 加入成员
        crmPermissionService.createPermission(CrmPermissionConvert.INSTANCE.convert(reqVO));
        return success(true);
    }


    @PutMapping("/update")
    @Operation(summary = "编辑团队成员")
    @PreAuthorize("@ss.hasPermission('crm:permission:update')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#updateReqVO.bizType", bizId = "#updateReqVO.bizId"
            , level = CrmPermissionLevelEnum.WRITE)
    public CommonResult<Boolean> updatePermission(@Valid @RequestBody CrmPermissionUpdateReqVO updateReqVO) {
        crmPermissionService.updatePermission(CrmPermissionConvert.INSTANCE.convert(updateReqVO));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "移除团队成员")
    @Parameters({
            @Parameter(name = "bizType", description = "CRM 类型", required = true, example = "2"),
            @Parameter(name = "bizId", description = "CRM 类型数据编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "团队成员编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('crm:permission:delete')")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_PERMISSION, bizTypeValue = "#bizType", bizId = "#bizId"
            , level = CrmPermissionLevelEnum.OWNER)
    public CommonResult<Boolean> deletePermission(@RequestParam("bizType") Integer bizType,
                                                  @RequestParam("bizId") Long bizId,
                                                  @RequestParam("id") Long id) {
        crmPermissionService.deletePermission(id);
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
        // TODO @puhui999：池子的逻辑；
        permission.removeIf(item -> ObjUtil.equal(item.getUserId(), CrmPermissionDO.POOL_USER_ID)); // 排除

        // 拼接数据
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(convertSet(permission, CrmPermissionDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userList, AdminUserRespDTO::getDeptId));
        Set<Long> postIds = userList.stream().flatMap(item -> item.getPostIds().stream()).collect(Collectors.toSet());
        Map<Long, PostRespDTO> postMap = postApi.getPostMap(postIds);
        return success(CrmPermissionConvert.INSTANCE.convert(permission, userList, deptMap, postMap));
    }

}
