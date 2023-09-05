package cn.iocoder.yudao.module.member.convert.brokerage.record;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.brokerage.record.vo.MemberBrokerageRecordRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.member.enums.brokerage.BrokerageRecordStatusEnum;
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
public interface MemberBrokerageRecordConvert {

    MemberBrokerageRecordConvert INSTANCE = Mappers.getMapper(MemberBrokerageRecordConvert.class);

    MemberBrokerageRecordRespVO convert(MemberBrokerageRecordDO bean);

    List<MemberBrokerageRecordRespVO> convertList(List<MemberBrokerageRecordDO> list);

    PageResult<MemberBrokerageRecordRespVO> convertPage(PageResult<MemberBrokerageRecordDO> page);

    default MemberBrokerageRecordDO convert(MemberUserDO user, String bizId, int brokerageFrozenDays, int brokerage, LocalDateTime unfreezeTime) {
        // 不冻结时，佣金直接就是结算状态
        Integer status = brokerageFrozenDays > 0
                ? BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
                : BrokerageRecordStatusEnum.SETTLEMENT.getStatus();
        return new MemberBrokerageRecordDO()
                .setUserId(user.getId())
                .setBizType(BrokerageRecordBizTypeEnum.ORDER.getType())
                .setBizId(bizId)
                .setPrice(brokerage)
                .setTotalPrice(user.getBrokeragePrice())
                .setTitle(BrokerageRecordBizTypeEnum.ORDER.getTitle())
                .setDescription(StrUtil.format(BrokerageRecordBizTypeEnum.ORDER.getDescription(), String.valueOf(brokerage / 100.0)))
                .setStatus(status)
                .setFrozenDays(brokerageFrozenDays)
                .setUnfreezeTime(unfreezeTime);
    }

}
