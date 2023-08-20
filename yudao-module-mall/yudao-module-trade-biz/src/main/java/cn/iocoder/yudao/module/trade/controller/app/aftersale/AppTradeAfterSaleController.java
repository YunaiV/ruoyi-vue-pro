package cn.iocoder.yudao.module.trade.controller.app.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleRespVO;
import cn.iocoder.yudao.module.trade.convert.aftersale.TradeAfterSaleConvert;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleWayEnum;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.module.trade.service.aftersale.TradeAfterSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @GetMapping(value = "/page")
    @Operation(summary = "获得售后分页")
    public CommonResult<PageResult<AppTradeAfterSaleRespVO>> getAfterSalePage(PageParam pageParam) {
        return success(TradeAfterSaleConvert.INSTANCE.convertPage02(
                afterSaleService.getAfterSalePage(getLoginUserId(), pageParam)));
    }

    @GetMapping(value = "/get")
    @Operation(summary = "获得售后订单")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    public CommonResult<AppTradeAfterSaleRespVO> getAfterSale(@RequestParam("id") Long id) {
        return success(TradeAfterSaleConvert.INSTANCE.convert(afterSaleService.getAfterSale(getLoginUserId(), id)));
    }

    @GetMapping(value = "/get-applying-count")
    @Operation(summary = "获得进行中的售后订单数量")
    public CommonResult<Long> getApplyingAfterSaleCount() {
        return success(afterSaleService.getApplyingAfterSaleCount(getLoginUserId()));
    }

    // TODO 芋艿：待实现
    @GetMapping(value = "/get-reason-list")
    @Operation(summary = "获得售后原因")
    @Parameter(name = "way", description = "售后类型", required = true, example = "10")
    public CommonResult<List<String>> getAfterSaleReasonList(@RequestParam("way") Integer way) {
        if (Objects.equals(TradeAfterSaleWayEnum.REFUND.getWay(), way)) {
            return success(Arrays.asList("不想要了", "商品质量问题", "商品描述不符"));
        }
        return success(Arrays.asList("不想要了", "商品质量问题", "商品描述不符", "商品错发漏发", "商品包装破损"));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "申请售后")
    @AfterSaleLog(id = "#info.data", content = "'申请售后:售后编号['+#info.data+'],订单编号['+#createReqVO.orderItemId+'], '", operateType = AfterSaleOperateTypeEnum.APPLY)
    public CommonResult<Long> createAfterSale(@RequestBody AppTradeAfterSaleCreateReqVO createReqVO) {
        return success(afterSaleService.createAfterSale(getLoginUserId(), createReqVO));
    }

    @PutMapping(value = "/delivery")
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
