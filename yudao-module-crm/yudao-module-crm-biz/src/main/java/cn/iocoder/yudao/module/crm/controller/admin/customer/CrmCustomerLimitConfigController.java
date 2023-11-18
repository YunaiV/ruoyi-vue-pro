package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.customerlimitconfig.CrmCustomerLimitConfigConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customerlimitconfig.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.crm.service.customerlimitconfig.CrmCustomerLimitConfigService;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客户限制配置")
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
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(new HashSet<>(customerLimitConfig.getUserIds()));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(new HashSet<>(customerLimitConfig.getDeptIds()));
        return success(CrmCustomerLimitConfigConvert.INSTANCE.convert(customerLimitConfig, userMap, deptMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户限制配置分页")
    @PreAuthorize("@ss.hasPermission('crm:customer-limit-config:query')")
    public CommonResult<PageResult<CrmCustomerLimitConfigRespVO>> getCustomerLimitConfigPage(@Valid CrmCustomerLimitConfigPageReqVO pageVO) {
        PageResult<CrmCustomerLimitConfigDO> pageResult = customerLimitConfigService.getCustomerLimitConfigPage(pageVO);
        Set<Long> userIds = CollectionUtils.convertSetByFlatMap(pageResult.getList(), CrmCustomerLimitConfigDO::getUserIds, Collection::stream);
        Set<Long> deptIds = CollectionUtils.convertSetByFlatMap(pageResult.getList(), CrmCustomerLimitConfigDO::getDeptIds, Collection::stream);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptIds);
        return success(CrmCustomerLimitConfigConvert.INSTANCE.convertPage(pageResult, userMap, deptMap));
    }

}
