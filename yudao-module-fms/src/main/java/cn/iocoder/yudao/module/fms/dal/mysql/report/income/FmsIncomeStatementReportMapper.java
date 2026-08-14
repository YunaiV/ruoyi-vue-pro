package cn.iocoder.yudao.module.fms.dal.mysql.report.income;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.income.FmsIncomeStatementReportDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 利润表数据 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsIncomeStatementReportMapper extends BaseMapperX<FmsIncomeStatementReportDO> {

    default List<FmsIncomeStatementReportDO> selectListByPeriod(Long accountSetId,
                                                                Integer fromPeriod, Integer toPeriod, Integer type) {
        return selectList(new LambdaQueryWrapperX<FmsIncomeStatementReportDO>()
                .eq(FmsIncomeStatementReportDO::getAccountSetId, accountSetId)
                .eq(FmsIncomeStatementReportDO::getFromPeriod, fromPeriod)
                .eq(FmsIncomeStatementReportDO::getToPeriod, toPeriod)
                .eq(FmsIncomeStatementReportDO::getType, type)
                .orderByAsc(FmsIncomeStatementReportDO::getSort)
                .orderByAsc(FmsIncomeStatementReportDO::getId));
    }

}
