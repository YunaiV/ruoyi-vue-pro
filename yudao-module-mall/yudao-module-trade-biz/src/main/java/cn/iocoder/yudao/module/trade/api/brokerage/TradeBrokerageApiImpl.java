package cn.iocoder.yudao.module.trade.api.brokerage;

import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageRecordService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 订单 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class TradeBrokerageApiImpl implements TradeBrokerageApi {

    @Resource
    private BrokerageRecordService brokerageRecordService;

}
