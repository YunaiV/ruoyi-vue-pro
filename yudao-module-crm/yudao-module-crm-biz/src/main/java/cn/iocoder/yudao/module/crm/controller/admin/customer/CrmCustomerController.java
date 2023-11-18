package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
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
        CrmCustomerDO customer = customerService.getCustomer(id);
        CrmCustomerRespVO customerRespVO = CrmCustomerConvert.INSTANCE.convert(customer);
        if (ObjectUtil.isAllNotEmpty(customer, customer.getAreaId())) {
            customerRespVO.setAreaName(AreaUtils.format(customer.getAreaId()));
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(NumberUtil.parseLong(customerRespVO.getCreator()), customerRespVO.getOwnerUserId())));
        customerRespVO.setCreatorName(Optional.ofNullable(userMap.get(NumberUtil.parseLong(customerRespVO.getCreator()))).map(AdminUserRespDTO::getNickname).orElse(null));
        AdminUserRespDTO ownerUser = userMap.get(customer.getOwnerUserId());
        if (Objects.nonNull(ownerUser)) {
            customerRespVO.setOwnerUserName(ownerUser.getNickname());
            DeptRespDTO dept = deptApi.getDept(ownerUser.getDeptId());
            if (Objects.nonNull(dept)) {
                customerRespVO.setOwnerUserDept(dept.getName());
            }
        }
        return success(customerRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getCustomerPage(@Valid CrmCustomerPageReqVO pageVO) {
        PageResult<CrmCustomerDO> pageResult = customerService.getCustomerPage(pageVO);
        Set<Long> userSet = CollectionUtils.convertSetByFlatMap(pageResult.getList(), i -> Stream.of(NumberUtil.parseLong(i.getCreator()), i.getOwnerUserId()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userSet);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(userMap.values().stream().map(AdminUserRespDTO::getDeptId).collect(Collectors.toSet()));
        PageResult<CrmCustomerRespVO> pageVo = CrmCustomerConvert.INSTANCE.convertPage(pageResult, userMap, deptMap);
        return success(pageVo);
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
