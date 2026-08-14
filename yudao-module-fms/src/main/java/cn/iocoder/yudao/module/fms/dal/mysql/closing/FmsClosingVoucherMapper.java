package cn.iocoder.yudao.module.fms.dal.mysql.closing;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * FMS 结账凭证 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsClosingVoucherMapper extends BaseMapperX<FmsClosingVoucherDO> {

    default List<FmsClosingVoucherDO> selectListByPeriod(Long accountSetId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<FmsClosingVoucherDO>()
                .eq(FmsClosingVoucherDO::getAccountSetId, accountSetId)
                .between(FmsClosingVoucherDO::getVoucherTime, beginTime, endTime)
                .orderByDesc(FmsClosingVoucherDO::getId));
    }

    default List<FmsClosingVoucherDO> selectListByClosingId(Long closingId) {
        return selectList(FmsClosingVoucherDO::getClosingId, closingId);
    }

    default Long selectCountByClosingId(Long closingId) {
        return selectCount(FmsClosingVoucherDO::getClosingId, closingId);
    }

    default List<FmsClosingVoucherDO> selectListByClosingIdAndPeriod(Long closingId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<FmsClosingVoucherDO>()
                .eq(FmsClosingVoucherDO::getClosingId, closingId)
                .between(FmsClosingVoucherDO::getVoucherTime, beginTime, endTime)
                .orderByDesc(FmsClosingVoucherDO::getId));
    }

    default List<FmsClosingVoucherDO> selectListByClosingIdAndVoucherTimeBetween(Long closingId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<FmsClosingVoucherDO>()
                .eq(FmsClosingVoucherDO::getClosingId, closingId)
                .geIfPresent(FmsClosingVoucherDO::getVoucherTime, beginTime)
                .ltIfPresent(FmsClosingVoucherDO::getVoucherTime, endTime)
                .orderByAsc(FmsClosingVoucherDO::getVoucherTime)
                .orderByAsc(FmsClosingVoucherDO::getId));
    }

    default List<FmsClosingVoucherDO> selectListByAccountSetIdAndVoucherIds(
            Long accountSetId, Collection<Long> voucherIds) {
        return selectList(new LambdaQueryWrapperX<FmsClosingVoucherDO>()
                .eq(FmsClosingVoucherDO::getAccountSetId, accountSetId)
                .in(FmsClosingVoucherDO::getVoucherId, voucherIds));
    }

}
