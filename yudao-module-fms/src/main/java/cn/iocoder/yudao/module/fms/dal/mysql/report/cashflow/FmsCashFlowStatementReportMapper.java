package cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowStatementReportDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * FMS 现金流量表数据 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsCashFlowStatementReportMapper extends BaseMapperX<FmsCashFlowStatementReportDO> {

    default List<FmsCashFlowStatementReportDO> selectListByPeriod(Long accountSetId,
                                                                  Integer fromPeriod, Integer toPeriod, Integer type) {
        return selectList(new LambdaQueryWrapperX<FmsCashFlowStatementReportDO>()
                .eq(FmsCashFlowStatementReportDO::getAccountSetId, accountSetId)
                .eq(FmsCashFlowStatementReportDO::getFromPeriod, fromPeriod)
                .eq(FmsCashFlowStatementReportDO::getToPeriod, toPeriod)
                .eq(FmsCashFlowStatementReportDO::getType, type)
                .orderByAsc(FmsCashFlowStatementReportDO::getSort)
                .orderByAsc(FmsCashFlowStatementReportDO::getId));
    }

    default List<FmsCashFlowStatementReportDO> selectListByIdsAndPeriod(List<Long> ids,
            Long accountSetId, Integer fromPeriod, Integer toPeriod, Integer type) {
        return selectList(new LambdaQueryWrapperX<FmsCashFlowStatementReportDO>()
                .in(FmsCashFlowStatementReportDO::getId, ids)
                .eq(FmsCashFlowStatementReportDO::getAccountSetId, accountSetId)
                .eq(FmsCashFlowStatementReportDO::getFromPeriod, fromPeriod)
                .eq(FmsCashFlowStatementReportDO::getToPeriod, toPeriod)
                .eq(FmsCashFlowStatementReportDO::getType, type));
    }

    default void clearAdjustedAmounts(Long accountSetId, Integer fromPeriod, Integer toPeriod, Integer type) {
        update(new FmsCashFlowStatementReportDO(),
                new LambdaUpdateWrapper<FmsCashFlowStatementReportDO>()
                        .eq(FmsCashFlowStatementReportDO::getAccountSetId, accountSetId)
                        .eq(FmsCashFlowStatementReportDO::getFromPeriod, fromPeriod)
                        .eq(FmsCashFlowStatementReportDO::getToPeriod, toPeriod)
                        .eq(FmsCashFlowStatementReportDO::getType, type)
                        .set(FmsCashFlowStatementReportDO::getCurrentAmount, BigDecimal.ZERO)
                        .set(FmsCashFlowStatementReportDO::getYearAmount, BigDecimal.ZERO));
    }

}
