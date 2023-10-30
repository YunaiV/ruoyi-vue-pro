package cn.iocoder.yudao.module.crm.controller.admin.contract;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.convert.contract.ContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.service.contract.ContractService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM 合同")
@RestController
@RequestMapping("/crm/contract")
@Validated
public class ContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractCreateReqVO createReqVO) {
        return success(contractService.createContract(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractUpdateReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        ContractDO contract = contractService.getContract(id);
        return success(ContractConvert.INSTANCE.convert(contract));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageVO);
        return success(ContractConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contract:export')")
    @OperateLog(type = EXPORT)
    public void exportContractExcel(@Valid ContractExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<ContractDO> list = contractService.getContractList(exportReqVO);
        // 导出 Excel
        List<ContractExcelVO> datas = ContractConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "合同.xls", "数据", ContractExcelVO.class, datas);
    }

    @PutMapping("/transfer")
    @Operation(summary = "合同转移")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> transfer(@Valid @RequestBody CrmTransferContractReqVO reqVO) {
        contractService.transferContract(reqVO, getLoginUserId());
        return success(true);
    }

}
