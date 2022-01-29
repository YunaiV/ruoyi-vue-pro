package cn.iocoder.yudao.module.system.controller.tenant;

import cn.iocoder.yudao.module.system.controller.tenant.vo.*;
import cn.iocoder.yudao.module.system.convert.tenant.SysTenantConvert;
import cn.iocoder.yudao.module.system.service.tenant.SysTenantService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "租户")
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController {

    @Resource
    private SysTenantService tenantService;

    @GetMapping("/get-id-by-name")
    @ApiOperation(value = "使用租户名，获得租户编号", notes = "登录界面，根据用户的租户名，获得租户编号")
    @ApiImplicitParam(name = "name", value = "租户名", required = true, example = "芋道源码", dataTypeClass = Long.class)
    public CommonResult<Long> getTenantIdByName(@RequestParam("name") String name) {
        SysTenantDO tenantDO = tenantService.getTenantByName(name);
        return success(tenantDO != null ? tenantDO.getId() : null);
    }

    @PostMapping("/create")
    @ApiOperation("创建租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:create')")
    public CommonResult<Long> createTenant(@Valid @RequestBody SysTenantCreateReqVO createReqVO) {
        return success(tenantService.createTenant(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:update')")
    public CommonResult<Boolean> updateTenant(@Valid @RequestBody SysTenantUpdateReqVO updateReqVO) {
        tenantService.updateTenant(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除租户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:tenant:delete')")
    public CommonResult<Boolean> deleteTenant(@RequestParam("id") Long id) {
        tenantService.deleteTenant(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得租户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:tenant:query')")
    public CommonResult<SysTenantRespVO> getTenant(@RequestParam("id") Long id) {
        SysTenantDO tenant = tenantService.getTenant(id);
        return success(SysTenantConvert.INSTANCE.convert(tenant));
    }

    @GetMapping("/list")
    @ApiOperation("获得租户列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('system:tenant:query')")
    public CommonResult<List<SysTenantRespVO>> getTenantList(@RequestParam("ids") Collection<Long> ids) {
        List<SysTenantDO> list = tenantService.getTenantList(ids);
        return success(SysTenantConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得租户分页")
    @PreAuthorize("@ss.hasPermission('system:tenant:query')")
    public CommonResult<PageResult<SysTenantRespVO>> getTenantPage(@Valid SysTenantPageReqVO pageVO) {
        PageResult<SysTenantDO> pageResult = tenantService.getTenantPage(pageVO);
        return success(SysTenantConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出租户 Excel")
    @PreAuthorize("@ss.hasPermission('system:tenant:export')")
    @OperateLog(type = EXPORT)
    public void exportTenantExcel(@Valid SysTenantExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SysTenantDO> list = tenantService.getTenantList(exportReqVO);
        // 导出 Excel
        List<SysTenantExcelVO> datas = SysTenantConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "租户.xls", "数据", SysTenantExcelVO.class, datas);
    }


}
