package cn.iocoder.yudao.module.trade.controller.app.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSalePageItemRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleOperateTypeEnum;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.module.trade.service.aftersale.TradeAfterSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 交易售后")
@RestController
@RequestMapping("/trade/after-sale")
@Validated
@Slf4j
public class AppTradeAfterSaleController {

    @Resource
    private TradeAfterSaleService afterSaleService;

    // TODO 芋艿：待实现
    @GetMapping(value = "/page")
    @Operation(summary = "获得售后分页")
    public CommonResult<PageResult<AppTradeAfterSalePageItemRespVO>> getAfterSalePage() {
        AppTradeAfterSalePageItemRespVO vo = new AppTradeAfterSalePageItemRespVO();
        vo.setId(1L);
        vo.setNo("1146347329394184195");
        vo.setStatus(61);
        vo.setWay(10);
        vo.setType(10);
        vo.setApplyReason("不想要了");
        vo.setApplyDescription("这个商品我不喜欢，想退款");
        vo.setApplyPicUrls(Arrays.asList("pic_url_1", "pic_url_2", "pic_url_3"));

        // 设置订单相关信息
        vo.setOrderId(2001L);
        vo.setOrderNo("23456789009876");
        vo.setOrderItemId(3001L);
        vo.setSpuId(4001L);
        vo.setSpuName("商品名");
        vo.setSkuId(5001L);
        vo.setProperties(Arrays.asList(
                new AppProductPropertyValueDetailRespVO().setPropertyId(6001L).setPropertyName("颜色").setValueId(7001L).setValueName("红色"),
                new AppProductPropertyValueDetailRespVO().setPropertyId(6002L).setPropertyName("尺寸").setValueId(7002L).setValueName("XL")));
        vo.setPicUrl("https://cdn.pixabay.com/photo/2022/12/06/06/21/lavender-7638368_1280.jpg");
        vo.setCount(2);

        // 设置审批相关信息
        vo.setAuditReason("审核通过");

        // 设置退款相关信息
        vo.setRefundPrice(1000);
        vo.setRefundTime(LocalDateTime.now());

        // 设置退货相关信息
        vo.setLogisticsId(7001L);
        vo.setLogisticsNo("LAGN101010101001");
        vo.setDeliveryTime(LocalDateTime.now());
        vo.setReceiveTime(LocalDateTime.now());
        vo.setReceiveReason("收货正常");

        return success(new PageResult<>(Arrays.asList(vo), 1L));
//        return success(afterSaleService.getAfterSalePage(getLoginUserId()));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "申请售后")
    @AfterSaleLog(id = "#info.data", content = "'申请售后:售后编号['+#info.data+'],订单编号['+#createReqVO.orderItemId+'], '", operateType = AfterSaleOperateTypeEnum.APPLY)
    public CommonResult<Long> createAfterSale(@RequestBody AppTradeAfterSaleCreateReqVO createReqVO) {
        return success(afterSaleService.createAfterSale(getLoginUserId(), createReqVO));
    }

    @PostMapping(value = "/delivery")
    @Operation(summary = "退回货物")
    public CommonResult<Boolean> deliveryAfterSale(@RequestBody AppTradeAfterSaleDeliveryReqVO deliveryReqVO) {
        afterSaleService.deliveryAfterSale(getLoginUserId(), deliveryReqVO);
        return success(true);
    }

    @DeleteMapping(value = "/cancel")
    @Operation(summary = "取消售后")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<Boolean> cancelAfterSale(@RequestParam("id") Long id) {
        afterSaleService.cancelAfterSale(getLoginUserId(), id);
        return success(true);
    }

}
