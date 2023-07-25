package cn.iocoder.yudao.module.pay.controller.admin.refund;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.*;
import cn.iocoder.yudao.module.pay.convert.refund.PayRefundConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 退款订单")
@RestController
@RequestMapping("/pay/refund")
@Validated
public class PayRefundController {

    @Resource
    private PayRefundService refundService;
    @Resource
    private PayAppService appService;

    @GetMapping("/get")
    @Operation(summary = "获得退款订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PayRefundDetailsRespVO> getRefund(@RequestParam("id") Long id) {
        PayRefundDO refund = refundService.getRefund(id);
        if (refund == null) {
            return success(new PayRefundDetailsRespVO());
        }

        // 拼接数据
        PayAppDO app = appService.getApp(refund.getAppId());
        return success(PayRefundConvert.INSTANCE.convert(refund, app));
    }

    @GetMapping("/page")
    @Operation(summary = "获得退款订单分页")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<PayRefundPageItemRespVO>> getRefundPage(@Valid PayRefundPageReqVO pageVO) {
        PageResult<PayRefundDO> pageResult = refundService.getRefundPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 处理应用ID数据
        Map<Long, PayAppDO> appMap = appService.getAppMap(convertList(pageResult.getList(), PayRefundDO::getAppId));
        return success(PayRefundConvert.INSTANCE.convertPage(pageResult, appMap));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出退款订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:refund:export')")
    @OperateLog(type = EXPORT)
    public void exportRefundExcel(@Valid PayRefundExportReqVO exportReqVO,
            HttpServletResponse response) throws IOException {
        List<PayRefundDO> list = refundService.getRefundList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "退款订单.xls", "数据",
                    PayRefundExcelVO.class, new ArrayList<>());
            return;
        }

        // 拼接返回
        Map<Long, PayAppDO> appMap = appService.getAppMap(convertList(list, PayRefundDO::getAppId));
        List<PayRefundExcelVO> excelList = PayRefundConvert.INSTANCE.convertList(list, appMap);
        // 导出 Excel
        ExcelUtils.write(response, "退款订单.xls", "数据", PayRefundExcelVO.class, excelList);
    }

}
