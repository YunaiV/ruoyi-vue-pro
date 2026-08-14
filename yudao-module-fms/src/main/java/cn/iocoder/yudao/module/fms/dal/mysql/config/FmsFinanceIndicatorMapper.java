package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 首页财务指标 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsFinanceIndicatorMapper extends BaseMapperX<FmsFinanceIndicatorDO> {

    default FmsFinanceIndicatorDO selectByIdAndAccountSetId(Long id, Long accountSetId) {
        return selectOne(FmsFinanceIndicatorDO::getId, id,
                FmsFinanceIndicatorDO::getAccountSetId, accountSetId);
    }

    default List<FmsFinanceIndicatorDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsFinanceIndicatorDO>()
                .eq(FmsFinanceIndicatorDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsFinanceIndicatorDO::getSort)
                .orderByAsc(FmsFinanceIndicatorDO::getId));
    }

    // TODO DONE @AI：启用指标查询改为按 status 查询，方法名完整表达过滤条件。
    default List<FmsFinanceIndicatorDO> selectListByAccountSetIdAndStatus(Long accountSetId, Integer status) {
        return selectList(new LambdaQueryWrapperX<FmsFinanceIndicatorDO>()
                .eq(FmsFinanceIndicatorDO::getAccountSetId, accountSetId)
                .eq(FmsFinanceIndicatorDO::getStatus, status)
                .orderByAsc(FmsFinanceIndicatorDO::getSort)
                .orderByAsc(FmsFinanceIndicatorDO::getId));
    }

    default FmsFinanceIndicatorDO selectByAccountSetIdAndCode(Long accountSetId, String code) {
        return selectOne(new LambdaQueryWrapperX<FmsFinanceIndicatorDO>()
                .eq(FmsFinanceIndicatorDO::getAccountSetId, accountSetId)
                .eq(FmsFinanceIndicatorDO::getCode, code));
    }

}
