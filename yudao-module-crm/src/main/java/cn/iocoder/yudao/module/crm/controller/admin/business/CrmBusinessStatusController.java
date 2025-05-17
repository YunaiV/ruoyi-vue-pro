package cn.iocoder.yudao.module.crm.controller.admin.business;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM 商机状态")
@RestController
@RequestMapping("/crm/business-status")
@Validated
public class CrmBusinessStatusController {

    @Resource
    private CrmBusinessStatusService businessStatusTypeService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business-status:create')")
    public CommonResult<Long> createBusinessStatus(@Valid @RequestBody CrmBusinessStatusSaveReqVO createReqVO) {
        return success(businessStatusTypeService.createBusinessStatus(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business-status:update')")
    public CommonResult<Boolean> updateBusinessStatus(@Valid @RequestBody CrmBusinessStatusSaveReqVO updateReqVO) {
        businessStatusTypeService.updateBusinessStatus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机状态")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business-status:delete')")
    public CommonResult<Boolean> deleteBusinessStatusType(@RequestParam("id") Long id) {
        businessStatusTypeService.deleteBusinessStatusType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机状态")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<CrmBusinessStatusRespVO> getBusinessStatusType(@RequestParam("id") Long id) {
        CrmBusinessStatusTypeDO statusType = businessStatusTypeService.getBusinessStatusType(id);
        if (statusType == null) {
            return success(null);
        }
        List<CrmBusinessStatusDO> statuses = businessStatusTypeService.getBusinessStatusListByTypeId(id);
        return success(BeanUtils.toBean(statusType, CrmBusinessStatusRespVO.class,
                statusTypeVO -> statusTypeVO.setStatuses(BeanUtils.toBean(statuses, CrmBusinessStatusRespVO.Status.class))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机状态分页")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<PageResult<CrmBusinessStatusRespVO>> getBusinessStatusPage(@Valid PageParam pageReqVO) {
        // 1. 查询数据
        PageResult<CrmBusinessStatusTypeDO> pageResult = businessStatusTypeService.getBusinessStatusTypePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2. 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), statusType -> Long.parseLong(statusType.getCreator())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(pageResult.getList(), CrmBusinessStatusTypeDO::getDeptIds, Collection::stream));
        return success(BeanUtils.toBean(pageResult, CrmBusinessStatusRespVO.class, statusTypeVO -> {
            statusTypeVO.setCreator(userMap.get(NumberUtils.parseLong(statusTypeVO.getCreator())).getNickname());
            statusTypeVO.setDeptNames(convertList(statusTypeVO.getDeptIds(),
                    deptId -> deptMap.containsKey(deptId) ? deptMap.get(deptId).getName() : null));
        }));
    }

    @GetMapping("/type-simple-list")
    @Operation(summary = "获得商机状态组列表")
    public CommonResult<List<CrmBusinessStatusRespVO>> getBusinessStatusTypeSimpleList() {
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypeList();
        // 过滤掉部门不匹配的
        Long deptId = adminUserApi.getUser(getLoginUserId()).getDeptId();
        list.removeIf(statusType -> CollUtil.isNotEmpty(statusType.getDeptIds()) && !statusType.getDeptIds().contains(deptId));
        return success(BeanUtils.toBean(list, CrmBusinessStatusRespVO.class));
    }

    @GetMapping("/status-simple-list")
    @Operation(summary = "获得商机状态列表")
    @Parameter(name = "typeId", description = "商机状态组", required = true, example = "1024")
    public CommonResult<List<CrmBusinessStatusRespVO.Status>> getBusinessStatusSimpleList(@RequestParam("typeId") Long typeId) {
        List<CrmBusinessStatusDO> list = businessStatusTypeService.getBusinessStatusListByTypeId(typeId);
        return success(BeanUtils.toBean(list, CrmBusinessStatusRespVO.Status.class));
    }

}
