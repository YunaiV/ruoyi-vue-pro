package cn.iocoder.yudao.module.trade.dal.mysql.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapperX<BrokerageRecordDO> {

    default PageResult<BrokerageRecordDO> selectPage(BrokerageRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageRecordDO>()
                .eqIfPresent(BrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BrokerageRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BrokerageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BrokerageRecordDO::getId));
    }

    default List<BrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getStatus, status)
                .lt(BrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Integer id, Integer status, BrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getStatus, status));
    }

    default BrokerageRecordDO selectByBizTypeAndBizId(Integer bizType, String bizId) {
        return selectOne(BrokerageRecordDO::getBizType, bizType,
                BrokerageRecordDO::getBizId, bizId);
    }

    // TODO @疯狂：mysql 关键字，大写哈；这样看起来清晰点；例如说 SELECT COUNT(1)
    @Select("select count(1), sum(price) from trade_brokerage_record where user_id = #{userId} and biz_type = #{bizType} and status = #{status}")
    UserBrokerageSummaryBO selectCountAndSumPriceByUserIdAndBizTypeAndStatus(@Param("userId") Long userId,
                                                                             @Param("bizType") Integer bizType,
                                                                             @Param("status") Integer status);
}
