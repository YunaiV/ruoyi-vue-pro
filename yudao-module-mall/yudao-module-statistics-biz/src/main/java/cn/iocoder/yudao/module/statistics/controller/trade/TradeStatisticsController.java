package cn.iocoder.yudao.module.statistics.controller.trade;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeStatisticsDO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理后台 - 交易统计")
@RestController
@RequestMapping("/statistics/product")
@Validated
@Slf4j
public class TradeStatisticsController {

    // TODO @疯狂：有个 summary 接口，返回昨日、本月、支付金额、本月订单金额等数据；具体看 ui 哈；

    // TODO @疯狂：返回 ProductStatisticsComparisonResp， 里面有两个字段，一个是选择的时间范围的合计结果，一个是对比的时间范围的合计结果；
    // 例如说，选择时间范围是 2023-10-01 ~ 2023-10-02，那么对比就是 2023-09-30，再倒推 2 天；
    public CommonResult<Object> getTradeStatisticsComparison() {
        return null;
    }

    // TODO @疯狂：查询指定时间范围内的交易统计数据；DO 到时需要改成 VO 哈
    // 总收入（营业额）= 订单、充值的支付 - 订单、充值的退款
    public CommonResult<List<TradeStatisticsDO>> getTradeStatisticsList(
            LocalDateTime[] times) {
        return null;
    }

}
