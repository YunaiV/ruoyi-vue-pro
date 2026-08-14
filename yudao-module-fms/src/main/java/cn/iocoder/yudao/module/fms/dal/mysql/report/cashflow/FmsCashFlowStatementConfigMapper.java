package cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowStatementConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 现金流量表配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsCashFlowStatementConfigMapper extends BaseMapperX<FmsCashFlowStatementConfigDO> {

    default List<FmsCashFlowStatementConfigDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsCashFlowStatementConfigDO>()
                .eq(FmsCashFlowStatementConfigDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsCashFlowStatementConfigDO::getSort)
                .orderByAsc(FmsCashFlowStatementConfigDO::getId));
    }

}
