package cn.iocoder.yudao.module.trade.convert.brokerage.record;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.TradeBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金记录 Convert
 *
 * @author owen
 */
@Mapper
public interface TradeBrokerageRecordConvert {

    TradeBrokerageRecordConvert INSTANCE = Mappers.getMapper(TradeBrokerageRecordConvert.class);

    TradeBrokerageRecordRespVO convert(TradeBrokerageRecordDO bean);

    List<TradeBrokerageRecordRespVO> convertList(List<TradeBrokerageRecordDO> list);

    PageResult<TradeBrokerageRecordRespVO> convertPage(PageResult<TradeBrokerageRecordDO> page);

    default TradeBrokerageRecordDO convert(TradeBrokerageUserDO user, BrokerageRecordBizTypeEnum bizType, String bizId,
                                           Integer brokerageFrozenDays, int brokerage, LocalDateTime unfreezeTime,
                                           String title) {
        brokerageFrozenDays = ObjectUtil.defaultIfNull(brokerageFrozenDays, 0);
        // 不冻结时，佣金直接就是结算状态
        Integer status = brokerageFrozenDays > 0
                ? BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
                : BrokerageRecordStatusEnum.SETTLEMENT.getStatus();
        return new TradeBrokerageRecordDO()
                .setUserId(user.getId())
                .setBizType(bizType.getType())
                .setBizId(bizId)
                .setPrice(brokerage)
                .setTotalPrice(user.getBrokeragePrice())
                .setTitle(title)
                .setDescription(StrUtil.format(bizType.getDescription(), String.valueOf(brokerage / 100.0)))
                .setStatus(status)
                .setFrozenDays(brokerageFrozenDays)
                .setUnfreezeTime(unfreezeTime);
    }

}
