package cn.iocoder.yudao.module.statistics.dal.mysql.pay;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSummaryRespVO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 支付钱包的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface PayWalletStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    WalletSummaryRespBO selectRechargeSummaryByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("payStatus") Boolean payStatus);

    WalletSummaryRespBO selectRechargeSummaryByRefundTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                 @Param("endTime") LocalDateTime endTime,
                                                                 @Param("refundStatus") Integer refundStatus);

    Integer selectPriceSummaryByBizTypeAndCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("bizType") Integer bizType);

    // TODO @疯狂：是不是搞个单独的 BO 呀；
    MemberSummaryRespVO selectRechargeSummaryGroupByWalletId(@Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime,
                                                             @Param("payStatus") Boolean payStatus);

}
