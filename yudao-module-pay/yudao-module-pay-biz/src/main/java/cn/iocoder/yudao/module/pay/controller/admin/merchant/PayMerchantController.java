package cn.iocoder.yudao.module.pay.controller.admin.merchant;

import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant.*;
import cn.iocoder.yudao.module.pay.convert.merchant.PayMerchantConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayMerchantService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "支付商户信息")
@RestController
@RequestMapping("/pay/merchant")
@Validated
public class PayMerchantController {

    @Resource
    private PayMerchantService merchantService;

    @PostMapping("/create")
    @Operation(summary = "创建支付商户信息")
    @PreAuthorize("@ss.hasPermission('pay:merchant:create')")
    public CommonResult<Long> createMerchant(@Valid @RequestBody PayMerchantCreateReqVO createReqVO) {
        return success(merchantService.createMerchant(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新支付商户信息")
    @PreAuthorize("@ss.hasPermission('pay:merchant:update')")
    public CommonResult<Boolean> updateMerchant(@Valid @RequestBody PayMerchantUpdateReqVO updateReqVO) {
        merchantService.updateMerchant(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改支付商户状态")
    @PreAuthorize("@ss.hasPermission('pay:merchant:update')")
    public CommonResult<Boolean> updateMerchantStatus(@Valid @RequestBody PayMerchantUpdateStatusReqVO reqVO) {
        merchantService.updateMerchantStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除支付商户信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:merchant:delete')")
    public CommonResult<Boolean> deleteMerchant(@RequestParam("id") Long id) {
        merchantService.deleteMerchant(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得支付商户信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<PayMerchantRespVO> getMerchant(@RequestParam("id") Long id) {
        PayMerchantDO merchant = merchantService.getMerchant(id);
        return success(PayMerchantConvert.INSTANCE.convert(merchant));
    }

    @GetMapping("/list-by-name")
    @Operation(summary = "根据商户名称获得支付商户信息列表")
    @Parameter(name = "name", description = "商户名称", example = "芋道")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<PayMerchantRespVO>> getMerchantListByName(@RequestParam(required = false) String name) {
        List<PayMerchantDO> merchantListDO = merchantService.getMerchantListByName(name);
        return success(PayMerchantConvert.INSTANCE.convertList(merchantListDO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得支付商户信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<PayMerchantRespVO>> getMerchantList(@RequestParam("ids") Collection<Long> ids) {
        List<PayMerchantDO> list = merchantService.getMerchantList(ids);
        return success(PayMerchantConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付商户信息分页")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<PageResult<PayMerchantRespVO>> getMerchantPage(@Valid PayMerchantPageReqVO pageVO) {
        PageResult<PayMerchantDO> pageResult = merchantService.getMerchantPage(pageVO);
        return success(PayMerchantConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出支付商户信息 Excel")
    @PreAuthorize("@ss.hasPermission('pay:merchant:export')")
    @OperateLog(type = EXPORT)
    public void exportMerchantExcel(@Valid PayMerchantExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PayMerchantDO> list = merchantService.getMerchantList(exportReqVO);
        // 导出 Excel
        List<PayMerchantExcelVO> datas = PayMerchantConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付商户信息.xls", "数据", PayMerchantExcelVO.class, datas);
    }

}
