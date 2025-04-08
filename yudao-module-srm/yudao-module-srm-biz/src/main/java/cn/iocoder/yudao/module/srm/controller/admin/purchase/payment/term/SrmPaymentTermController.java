package cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermSimpleRespVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term.SrmPaymentTermDO;
import cn.iocoder.yudao.module.srm.service.purchase.payment.term.SrmPaymentTermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 付款条款")
@RestController
@RequestMapping("/srm/payment-term")
@Validated
public class SrmPaymentTermController {

    @Resource
    private SrmPaymentTermService paymentTermService;

    @PostMapping("/create")
    @Operation(summary = "创建付款条款")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('srm:payment-term:create')")
    public CommonResult<Long> createPaymentTerm(@Valid @RequestBody SrmPaymentTermSaveReqVO createReqVO) {
        return success(paymentTermService.createPaymentTerm(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新付款条款")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:update')")
    public CommonResult<Boolean> updatePaymentTerm(@Valid @RequestBody SrmPaymentTermSaveReqVO updateReqVO) {
        paymentTermService.updatePaymentTerm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除付款条款")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('srm:payment-term:delete')")
    public CommonResult<Boolean> deletePaymentTerm(@RequestParam("id") Long id) {
        paymentTermService.deletePaymentTerm(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得付款条款")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:query')")
    public CommonResult<SrmPaymentTermRespVO> getPaymentTerm(@RequestParam("id") Long id) {
        SrmPaymentTermDO paymentTerm = paymentTermService.getPaymentTerm(id);
        return success(BeanUtils.toBean(paymentTerm, SrmPaymentTermRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得付款条款分页")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:query')")
    public CommonResult<PageResult<SrmPaymentTermRespVO>> getPaymentTermPage(@Valid SrmPaymentTermPageReqVO pageReqVO) {
        PageResult<SrmPaymentTermDO> pageResult = paymentTermService.getPaymentTermPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SrmPaymentTermRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出付款条款 Excel")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPaymentTermExcel(@Valid SrmPaymentTermPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SrmPaymentTermDO> list = paymentTermService.getPaymentTermPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "付款条款.xls", "数据", SrmPaymentTermRespVO.class,
            BeanUtils.toBean(list, SrmPaymentTermRespVO.class));
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入付款条款 Excel")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:import')")
    public CommonResult<List<SrmPaymentTermSaveReqVO>> importPaymentTermExcel(
        @RequestParam("file") MultipartFile file) throws Exception {
        List<SrmPaymentTermSaveReqVO> list = ExcelUtils.read(file, SrmPaymentTermSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        paymentTermService.createPaymentTermList(list);
        return success(list);
    }

    //精简列表
    @GetMapping("/simple-list")
    @Operation(summary = "获得付款条款精简列表")
    @PreAuthorize("@ss.hasPermission('srm:payment-term:query')")
    public CommonResult<List<SrmPaymentTermSimpleRespVO>> getSimplePaymentTermList() {
        List<SrmPaymentTermDO> list = paymentTermService.getPaymentTermList();
        return success(BeanUtils.toBean(list, SrmPaymentTermSimpleRespVO.class));
    }
}