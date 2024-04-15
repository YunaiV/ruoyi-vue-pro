package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.account.ErpAccountPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.account.ErpAccountRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.account.ErpAccountSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 结算账户")
@RestController
@RequestMapping("/erp/account")
@Validated
public class ErpAccountController {

    @Resource
    private ErpAccountService accountService;

    @PostMapping("/create")
    @Operation(summary = "创建结算账户")
    @PreAuthorize("@ss.hasPermission('erp:account:create')")
    public CommonResult<Long> createAccount(@Valid @RequestBody ErpAccountSaveReqVO createReqVO) {
        return success(accountService.createAccount(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新结算账户")
    @PreAuthorize("@ss.hasPermission('erp:account:update')")
    public CommonResult<Boolean> updateAccount(@Valid @RequestBody ErpAccountSaveReqVO updateReqVO) {
        accountService.updateAccount(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-default-status")
    @Operation(summary = "更新结算账户默认状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true),
            @Parameter(name = "status", description = "状态", required = true)
    })
    public CommonResult<Boolean> updateAccountDefaultStatus(@RequestParam("id") Long id,
                                                              @RequestParam("defaultStatus") Boolean defaultStatus) {
        accountService.updateAccountDefaultStatus(id, defaultStatus);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除结算账户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:account:delete')")
    public CommonResult<Boolean> deleteAccount(@RequestParam("id") Long id) {
        accountService.deleteAccount(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得结算账户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:account:query')")
    public CommonResult<ErpAccountRespVO> getAccount(@RequestParam("id") Long id) {
        ErpAccountDO account = accountService.getAccount(id);
        return success(BeanUtils.toBean(account, ErpAccountRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得结算账户精简列表", description = "只包含被开启的结算账户，主要用于前端的下拉选项")
    public CommonResult<List<ErpAccountRespVO>> getWarehouseSimpleList() {
        List<ErpAccountDO> list = accountService.getAccountListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, account -> new ErpAccountRespVO().setId(account.getId())
                .setName(account.getName()).setDefaultStatus(account.getDefaultStatus())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得结算账户分页")
    @PreAuthorize("@ss.hasPermission('erp:account:query')")
    public CommonResult<PageResult<ErpAccountRespVO>> getAccountPage(@Valid ErpAccountPageReqVO pageReqVO) {
        PageResult<ErpAccountDO> pageResult = accountService.getAccountPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpAccountRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出结算账户 Excel")
    @PreAuthorize("@ss.hasPermission('erp:account:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAccountExcel(@Valid ErpAccountPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpAccountDO> list = accountService.getAccountPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "结算账户.xls", "数据", ErpAccountRespVO.class,
                        BeanUtils.toBean(list, ErpAccountRespVO.class));
    }

}