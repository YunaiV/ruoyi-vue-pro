package cn.iocoder.yudao.module.trade.convert.brokerage.record;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.BrokerageRecordRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 佣金记录 Convert
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordConvert {

    BrokerageRecordConvert INSTANCE = Mappers.getMapper(BrokerageRecordConvert.class);

    BrokerageRecordRespVO convert(BrokerageRecordDO bean);

    List<BrokerageRecordRespVO> convertList(List<BrokerageRecordDO> list);

    PageResult<BrokerageRecordRespVO> convertPage(PageResult<BrokerageRecordDO> page);

    default BrokerageRecordDO convert(BrokerageUserDO user, BrokerageRecordBizTypeEnum bizType, String bizId,
                                      Integer brokerageFrozenDays, int brokerage, LocalDateTime unfreezeTime,
                                      String title, Long sourceUserId, Integer sourceUserType) {
        brokerageFrozenDays = ObjectUtil.defaultIfNull(brokerageFrozenDays, 0);
        // 不冻结时，佣金直接就是结算状态
        Integer status = brokerageFrozenDays > 0
                ? BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
                : BrokerageRecordStatusEnum.SETTLEMENT.getStatus();
        return new BrokerageRecordDO()
                .setUserId(user.getId())
                .setBizType(bizType.getType())
                .setBizId(bizId)
                .setPrice(brokerage)
                .setTotalPrice(user.getPrice())
                .setTitle(title)
                .setDescription(StrUtil.format(bizType.getDescription(), String.format("￥%.2f", brokerage / 100d)))
                .setStatus(status)
                .setFrozenDays(brokerageFrozenDays)
                .setUnfreezeTime(unfreezeTime)
                .setSourceUserType(sourceUserType)
                .setSourceUserId(sourceUserId);
    }

    default PageResult<BrokerageRecordRespVO> convertPage(PageResult<BrokerageRecordDO> pageResult, Map<Long, MemberUserRespDTO> userMap) {
        PageResult<BrokerageRecordRespVO> result = convertPage(pageResult);

        for (BrokerageRecordRespVO respVO : result.getList()) {
            Optional.ofNullable(userMap.get(respVO.getUserId())).ifPresent(user ->
                    respVO.setUserNickname(user.getNickname()).setUserAvatar(user.getAvatar()));

            Optional.ofNullable(userMap.get(respVO.getSourceUserId())).ifPresent(user ->
                    respVO.setSourceUserNickname(user.getNickname()).setSourceUserAvatar(user.getAvatar()));
        }

        return result;
    }
}
