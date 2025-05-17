package cn.iocoder.yudao.module.pay.controller.admin.transfer;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 转账单")
@RestController
@RequestMapping("/pay/transfer")
@Validated
public class PayTransferController {

    @Resource
    private PayTransferService payTransferService;
    @Resource
    private PayAppService payAppService;

    @GetMapping("/get")
    @Operation(summary = "获得转账订单")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PayTransferRespVO> getTransfer(@RequestParam("id") Long id) {
        PayTransferDO transfer = payTransferService.getTransfer(id);
        if (transfer == null) {
            return success(new PayTransferRespVO());
        }

        // 拼接数据
        PayAppDO app = payAppService.getApp(transfer.getAppId());
        return success(BeanUtils.toBean(transfer, PayTransferRespVO.class, transferVO -> {
            if (app != null) {
                transferVO.setAppName(app.getName());
            }
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得转账订单分页")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PageResult<PayTransferRespVO>> getTransferPage(@Valid PayTransferPageReqVO pageVO) {
        PageResult<PayTransferDO> pageResult = payTransferService.getTransferPage(pageVO);

        // 拼接数据
        Map<Long, PayAppDO> apps = payAppService.getAppMap(convertList(pageResult.getList(), PayTransferDO::getAppId));
        return success(BeanUtils.toBean(pageResult, PayTransferRespVO.class, transferVO -> {
            if (apps.containsKey(transferVO.getAppId())) {
                transferVO.setAppName(apps.get(transferVO.getAppId()).getName());
            }
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出转账订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:transfer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransfer(PayTransferPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        PageResult<PayTransferRespVO> pageResult = getTransferPage(pageReqVO).getData();

        // 导出 Excel
        ExcelUtils.write(response, "转账订单.xls", "数据", PayTransferRespVO.class, pageResult.getList());
    }

}
