package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerPoolConfigService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mapstruct.ap.internal.util.Collections;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_POOL_CONFIG_NOT_EXISTS_OR_DISABLED;

@Tag(name = "管理后台 - CRM 客户")
@RestController
@RequestMapping("/crm/customer")
@Validated
public class CrmCustomerController {

    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmCustomerPoolConfigService customerPoolConfigService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody CrmCustomerSaveReqVO createReqVO) {
        return success(customerService.createCustomer(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody CrmCustomerSaveReqVO updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "客户编号", required = true)
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
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                Collections.asSet(Long.valueOf(customer.getCreator()), customer.getOwnerUserId()));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convert(customer, userMap, deptMap));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getCustomerPage(@Valid CrmCustomerPageReqVO pageVO) {
        // 1. 查询客户分页
        PageResult<CrmCustomerDO> pageResult = customerService.getCustomerPage(pageVO, getLoginUserId());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2. 拼接数据
        Map<Long, Long> poolDayMap = Boolean.TRUE.equals(pageVO.getPool()) ? null :
                getPoolDayMap(pageResult.getList()); // 客户界面，需要查看距离进入公海的时间
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), user -> Stream.of(Long.parseLong(user.getCreator()), user.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convertPage(pageResult, userMap, deptMap, poolDayMap));
    }

    @GetMapping("/put-in-pool-remind-page")
    @Operation(summary = "获得待进入公海客户分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getPutInPoolRemindCustomerPage(@Valid CrmCustomerPageReqVO pageVO) {
        // 获取公海配置 TODO @dbh52：合并到 getPutInPoolRemindCustomerPage 会更合适哈；
        CrmCustomerPoolConfigDO poolConfigDO = customerPoolConfigService.getCustomerPoolConfig();
        if (ObjUtil.isNull(poolConfigDO)
                || Boolean.FALSE.equals(poolConfigDO.getEnabled())
                || Boolean.FALSE.equals(poolConfigDO.getNotifyEnabled())
        ) { // TODO @dbh52：这个括号，一般不换行，在 java 这里；
            throw exception(CUSTOMER_POOL_CONFIG_NOT_EXISTS_OR_DISABLED);
        }

        // 1. 查询客户分页
        PageResult<CrmCustomerDO> pageResult = customerService.getPutInPoolRemindCustomerPage(pageVO, poolConfigDO, getLoginUserId());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2. 拼接数据
        // TODO @芋艿：合并 getCustomerPage 和 getPutInPoolRemindCustomerPage 的后置处理；
        Map<Long, Long> poolDayMap = getPoolDayMap(pageResult.getList()); // 客户界面，需要查看距离进入公海的时间
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), user -> Stream.of(Long.parseLong(user.getCreator()), user.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(CrmCustomerConvert.INSTANCE.convertPage(pageResult, userMap, deptMap, poolDayMap));
    }

    /**
     * 获取距离进入公海的时间
     *
     * @param customerList 客户列表
     * @return Map<key 客户编号, value 距离进入公海的时间>
     */
    private Map<Long, Long> getPoolDayMap(List<CrmCustomerDO> customerList) {
        CrmCustomerPoolConfigDO poolConfig = customerPoolConfigService.getCustomerPoolConfig();
        if (poolConfig == null || !poolConfig.getEnabled()) {
            return MapUtil.empty();
        }
        return convertMap(customerList, CrmCustomerDO::getId, customer -> {
            // 1.1 未成交放入公海天数
            long dealExpireDay = 0;
            if (!customer.getDealStatus()) {
                dealExpireDay = poolConfig.getDealExpireDays() - LocalDateTimeUtils.between(customer.getCreateTime());
            }
            // 1.2 未跟进放入公海天数
            LocalDateTime lastTime = ObjUtil.defaultIfNull(customer.getContactLastTime(), customer.getCreateTime());
            long contactExpireDay = poolConfig.getContactExpireDays() - LocalDateTimeUtils.between(lastTime);
            if (contactExpireDay < 0) {
                contactExpireDay = 0;
            }
            // 2. 返回最小的天数
            return Math.min(dealExpireDay, contactExpireDay);
        });
    }

    @GetMapping(value = "/list-all-simple")
    @Operation(summary = "获取客户精简信息列表", description = "只包含有读权限的客户，主要用于前端的下拉选项")
    public CommonResult<List<CrmCustomerRespVO>> getSimpleDeptList() {
        CrmCustomerPageReqVO reqVO = new CrmCustomerPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        List<CrmCustomerDO> list = customerService.getCustomerPage(reqVO, getLoginUserId()).getList();
        return success(convertList(list, customer -> // 只返回 id、name 精简字段
                new CrmCustomerRespVO().setId(customer.getId()).setName(customer.getName())));
    }

    // TODO @puhui999：公海的导出，前端可以接下
    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('crm:customer:export')")
    @OperateLog(type = EXPORT)
    public void exportCustomerExcel(@Valid CrmCustomerPageReqVO pageVO,
                                    HttpServletResponse response) throws IOException {
        pageVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        List<CrmCustomerDO> list = customerService.getCustomerPage(pageVO, getLoginUserId()).getList();
        // 导出 Excel
        ExcelUtils.write(response, "客户.xls", "数据", CrmCustomerRespVO.class,
                BeanUtils.toBean(list, CrmCustomerRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入客户模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<CrmCustomerImportExcelVO> list = Arrays.asList(
                CrmCustomerImportExcelVO.builder().name("芋道").industryId(1).level(1).source(1).mobile("15601691300").telephone("")
                        .website("https://doc.iocoder.cn/").qq("").wechat("").email("yunai@iocoder.cn").description("").remark("")
                        .areaId(null).detailAddress("").build(),
                CrmCustomerImportExcelVO.builder().name("源码").industryId(1).level(1).source(1).mobile("15601691300").telephone("")
                        .website("https://doc.iocoder.cn/").qq("").wechat("").email("yunai@iocoder.cn").description("").remark("")
                        .areaId(null).detailAddress("").build()
        );
        // 输出
        ExcelUtils.write(response, "客户导入模板.xls", "客户列表", CrmCustomerImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入客户")
    @PreAuthorize("@ss.hasPermission('system:customer:import')")
    public CommonResult<CrmCustomerImportRespVO> importExcel(@Valid @RequestBody CrmCustomerImportReqVO importReqVO)
            throws Exception {
        List<CrmCustomerImportExcelVO> list = ExcelUtils.read(importReqVO.getFile(), CrmCustomerImportExcelVO.class);
        return success(customerService.importCustomerList(list, importReqVO));
    }

    @PutMapping("/transfer")
    @Operation(summary = "转移客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> transferCustomer(@Valid @RequestBody CrmCustomerTransferReqVO reqVO) {
        customerService.transferCustomer(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/lock")
    @Operation(summary = "锁定/解锁客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> lockCustomer(@Valid @RequestBody CrmCustomerLockReqVO lockReqVO) {
        customerService.lockCustomer(lockReqVO, getLoginUserId());
        return success(true);
    }

    // ==================== 公海相关操作 ====================

    @PutMapping("/put-pool")
    @Operation(summary = "数据放入公海")
    @Parameter(name = "id", description = "客户编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> putCustomerPool(@RequestParam("id") Long id) {
        customerService.putCustomerPool(id);
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "领取公海客户")
    @Parameter(name = "ids", description = "编号数组", required = true, example = "1,2,3")
    @PreAuthorize("@ss.hasPermission('crm:customer:receive')")
    public CommonResult<Boolean> receiveCustomer(@RequestParam(value = "ids") List<Long> ids) {
        customerService.receiveCustomer(ids, getLoginUserId(), Boolean.TRUE);
        return success(true);
    }

    @PutMapping("/distribute")
    @Operation(summary = "分配公海给对应负责人")
    @PreAuthorize("@ss.hasPermission('crm:customer:distribute')")
    public CommonResult<Boolean> distributeCustomer(@Valid @RequestBody CrmCustomerDistributeReqVO distributeReqVO) {
        customerService.receiveCustomer(distributeReqVO.getIds(), distributeReqVO.getOwnerUserId(), Boolean.FALSE);
        return success(true);
    }

}
