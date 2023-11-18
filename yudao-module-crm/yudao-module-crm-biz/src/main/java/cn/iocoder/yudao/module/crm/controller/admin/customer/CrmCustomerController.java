package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM 客户")
@RestController
@RequestMapping("/crm/customer")
@Validated
public class CrmCustomerController {

    @Resource
    private CrmCustomerService customerService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private CrmPermissionService permissionService;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody CrmCustomerCreateReqVO createReqVO) {
        return success(customerService.createCustomer(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody CrmCustomerUpdateReqVO updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:customer:delete')")
    public CommonResult<Boolean> deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<CrmCustomerRespVO> getCustomer(@RequestParam("id") Long id) {
        // 1. 获取客户
        CrmCustomerDO customer = customerService.getCustomer(id);
        if (customer == null) {
            return success(null);
        }

        // 2. 拼接数据
        // 2.1 获取负责人
        List<CrmPermissionDO> ownerList = permissionService.getPermissionByBizTypeAndBizIdsAndLevel(
                CrmBizTypeEnum.CRM_CUSTOMER.getType(), Collections.singletonList(customer.getId()),
                CrmPermissionLevelEnum.OWNER.getLevel());
        Map<Long, CrmPermissionDO> ownerMap = convertMap(ownerList, CrmPermissionDO::getBizId);
        // 2.2 获取负责人详情
        Set<Long> userIds = convertSet(ownerList, CrmPermissionDO::getUserId);
        userIds.add(Long.parseLong(customer.getCreator())); // 加入创建者
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(userIds);
        Map<Long, AdminUserRespDTO> userMap = convertMap(userList, AdminUserRespDTO::getId);
        // 2.3 获取部门详情
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userList, AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convert(customer, ownerMap, userMap, deptMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getCustomerPage(@Valid CrmCustomerPageReqVO pageVO) {
        PageResult<CrmCustomerDO> pageResult = customerService.getCustomerPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), user -> Stream.of(NumberUtil.parseLong(user.getCreator()), user.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convertPage(pageResult, userMap, deptMap));
        return convertPage(customerService.getCustomerPage(pageVO, getLoginUserId()));
    }

    @GetMapping("/pool-page")
    @Operation(summary = "获得公海客户分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getPoolCustomerPage(@Valid CrmCustomerPageReqVO pageVO) {
        return convertPage(customerService.getCustomerPage(pageVO, CrmPermissionDO.POOL_USER_ID));
    }

    private CommonResult<PageResult<CrmCustomerRespVO>> convertPage(PageResult<CrmCustomerDO> pageResult) {
        // 2. 拼接数据
        Set<Long> ids = convertSet(pageResult.getList(), CrmCustomerDO::getId);
        // 2.1 获取负责人
        List<CrmPermissionDO> ownerList = permissionService.getPermissionByBizTypeAndBizIdsAndLevel(
                CrmBizTypeEnum.CRM_CUSTOMER.getType(), ids, CrmPermissionLevelEnum.OWNER.getLevel());
        Map<Long, CrmPermissionDO> ownerMap = convertMap(ownerList, CrmPermissionDO::getBizId);
        // 2.2 获取负责人详情
        Set<Long> userIds = convertSet(ownerList, CrmPermissionDO::getUserId);
        userIds.addAll(convertSet(pageResult.getList(), item -> Long.parseLong(item.getCreator()))); // 加入创建者
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(userIds);
        Map<Long, AdminUserRespDTO> userMap = convertMap(userList, AdminUserRespDTO::getId);
        // 2.3 获取部门详情
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userList, AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convertPage(pageResult, ownerMap, userMap, deptMap));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('crm:customer:export')")
    @OperateLog(type = EXPORT)
    public void exportCustomerExcel(@Valid CrmCustomerExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<CrmCustomerDO> list = customerService.getCustomerList(exportReqVO);
        // 导出 Excel
        List<CrmCustomerExcelVO> datas = CrmCustomerConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "客户.xls", "数据", CrmCustomerExcelVO.class, datas);
    }

    @PutMapping("/transfer")
    @Operation(summary = "客户转移")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> transfer(@Valid @RequestBody CrmCustomerTransferReqVO reqVO) {
        customerService.transferCustomer(reqVO, getLoginUserId());
        return success(true);
    }

    // TODO @Joey：单独建一个属于自己业务的 ReqVO；因为前端如果模拟请求，是不是可以更新其它字段了；
    @PutMapping("/lock")
    @Operation(summary = "锁定/解锁客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> lockCustomer(@Valid @RequestBody CrmCustomerUpdateReqVO updateReqVO) {
        customerService.lockCustomer(updateReqVO);
        return success(true);
    }

}
