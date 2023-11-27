package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerLimitConfigConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerLimitConfigService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - CRM 客户限制配置")
@RestController
@RequestMapping("/crm/customer-limit-config")
@Validated
public class CrmCustomerLimitConfigController {

    @Resource
    private CrmCustomerLimitConfigService customerLimitConfigService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建客户限制配置")
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:create')")
    public CommonResult<Long> createCustomerLimitConfig(@Valid @RequestBody CrmCustomerLimitConfigCreateReqVO createReqVO) {
        return success(customerLimitConfigService.createCustomerLimitConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户限制配置")
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:update')")
    public CommonResult<Boolean> updateCustomerLimitConfig(@Valid @RequestBody CrmCustomerLimitConfigUpdateReqVO updateReqVO) {
        customerLimitConfigService.updateCustomerLimitConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户限制配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:delete')")
    public CommonResult<Boolean> deleteCustomerLimitConfig(@RequestParam("id") Long id) {
        customerLimitConfigService.deleteCustomerLimitConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户限制配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:query')")
    public CommonResult<CrmCustomerLimitConfigRespVO> getCustomerLimitConfig(@RequestParam("id") Long id) {
        CrmCustomerLimitConfigDO customerLimitConfig = customerLimitConfigService.getCustomerLimitConfig(id);
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(customerLimitConfig.getUserIds());
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(customerLimitConfig.getDeptIds());
        return success(CrmCustomerLimitConfigConvert.INSTANCE.convert(customerLimitConfig, userMap, deptMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户限制配置分页")
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:query')")
    public CommonResult<PageResult<CrmCustomerLimitConfigRespVO>> getCustomerLimitConfigPage(@Valid CrmCustomerLimitConfigPageReqVO pageVO) {
        PageResult<CrmCustomerLimitConfigDO> pageResult = customerLimitConfigService.getCustomerLimitConfigPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), CrmCustomerLimitConfigDO::getUserIds, Collection::stream));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(pageResult.getList(), CrmCustomerLimitConfigDO::getDeptIds, Collection::stream));
        return success(CrmCustomerLimitConfigConvert.INSTANCE.convertPage(pageResult, userMap, deptMap));
    }

}
