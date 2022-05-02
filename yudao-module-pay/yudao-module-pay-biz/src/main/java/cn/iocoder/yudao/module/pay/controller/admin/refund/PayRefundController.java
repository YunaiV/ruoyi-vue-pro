package cn.iocoder.yudao.module.pay.controller.admin.refund;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.*;
import cn.iocoder.yudao.module.pay.convert.refund.PayRefundConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayMerchantService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 退款订单")
@RestController
@RequestMapping("/pay/refund")
@Validated
public class PayRefundController {

    @Resource
    private PayRefundService refundService;
    @Resource
    private PayMerchantService merchantService;
    @Resource
    private PayAppService appService;
    @Resource
    private PayOrderService orderService;

    @GetMapping("/get")
    @ApiOperation("获得退款订单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PayRefundDetailsRespVO> getRefund(@RequestParam("id") Long id) {
        PayRefundDO refund = refundService.getRefund(id);
        if (ObjectUtil.isNull(refund)) {
            return success(new PayRefundDetailsRespVO());
        }

        PayMerchantDO merchantDO = merchantService.getMerchant(refund.getMerchantId());
        PayAppDO appDO = appService.getApp(refund.getAppId());
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(refund.getChannelCode());
        PayOrderDO orderDO = orderService.getOrder(refund.getOrderId());

        PayRefundDetailsRespVO refundDetail = PayRefundConvert.INSTANCE.refundDetailConvert(refund);
        refundDetail.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
        refundDetail.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
        refundDetail.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
        refundDetail.setSubject(orderDO.getSubject());

        return success(refundDetail);
    }

    @GetMapping("/page")
    @ApiOperation("获得退款订单分页")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<PayRefundPageItemRespVO>> getRefundPage(@Valid PayRefundPageReqVO pageVO) {
        PageResult<PayRefundDO> pageResult = refundService.getRefundPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 处理商户ID数据
        Map<Long, PayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(pageResult.getList(), PayRefundDO::getMerchantId));
        // 处理应用ID数据
        Map<Long, PayAppDO> appMap = appService.getAppMap(
                CollectionUtils.convertList(pageResult.getList(), PayRefundDO::getAppId));
        List<PayRefundPageItemRespVO> list = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(c -> {
            PayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            PayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());

            PayRefundPageItemRespVO item = PayRefundConvert.INSTANCE.pageItemConvert(c);

            item.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
            item.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
            item.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
            list.add(item);
        });

        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出退款订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:refund:export')")
    @OperateLog(type = EXPORT)
    public void exportRefundExcel(@Valid PayRefundExportReqVO exportReqVO,
            HttpServletResponse response) throws IOException {

        List<PayRefundDO> list = refundService.getRefundList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "退款订单.xls", "数据",
                    PayRefundExcelVO.class, new ArrayList<>());
        }

        // 处理商户ID数据
        Map<Long, PayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(list, PayRefundDO::getMerchantId));
        // 处理应用ID数据
        Map<Long, PayAppDO> appMap = appService.getAppMap(
                CollectionUtils.convertList(list, PayRefundDO::getAppId));

        List<PayRefundExcelVO> excelDatum = new ArrayList<>(list.size());
        // 处理商品名称数据
        Map<Long, PayOrderDO> orderMap = orderService.getOrderSubjectMap(
                CollectionUtils.convertList(list, PayRefundDO::getOrderId));

        list.forEach(c -> {
            PayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            PayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());

            PayRefundExcelVO excelItem = PayRefundConvert.INSTANCE.excelConvert(c);
            excelItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
            excelItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
            excelItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
            excelItem.setSubject(orderMap.get(c.getOrderId()).getSubject());

            excelDatum.add(excelItem);
        });

        // 导出 Excel
        ExcelUtils.write(response, "退款订单.xls", "数据", PayRefundExcelVO.class, excelDatum);
    }

}
