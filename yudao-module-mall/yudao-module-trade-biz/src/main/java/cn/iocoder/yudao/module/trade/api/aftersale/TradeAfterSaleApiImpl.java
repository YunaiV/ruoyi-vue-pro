package cn.iocoder.yudao.module.trade.api.aftersale;

import cn.iocoder.yudao.module.trade.api.aftersale.dto.AfterSaleSummaryRespDTO;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 售后 API 接口实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeAfterSaleApiImpl implements TradeAfterSaleApi {

    @Resource
    private AfterSaleService afterSaleService;

    @Override
    public AfterSaleSummaryRespDTO getAfterSaleSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return afterSaleService.getAfterSaleSummary(beginTime, endTime);
    }

}
