package cn.iocoder.yudao.module.fms.dal.mysql.closing;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 结账方案 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsClosingSchemeMapper extends BaseMapperX<FmsClosingSchemeDO> {

    default FmsClosingSchemeDO selectByAccountSetIdAndType(Long accountSetId, Integer type) {
        return selectOne(new LambdaQueryWrapperX<FmsClosingSchemeDO>()
                .eq(FmsClosingSchemeDO::getAccountSetId, accountSetId)
                .eq(FmsClosingSchemeDO::getType, type));
    }

    default FmsClosingSchemeDO selectByIdAndAccountSetId(Long id, Long accountSetId) {
        return selectOne(new LambdaQueryWrapperX<FmsClosingSchemeDO>()
                .eq(FmsClosingSchemeDO::getId, id)
                .eq(FmsClosingSchemeDO::getAccountSetId, accountSetId));
    }

    default List<FmsClosingSchemeDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsClosingSchemeDO>()
                .eq(FmsClosingSchemeDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsClosingSchemeDO::getType)
                .orderByAsc(FmsClosingSchemeDO::getId));
    }

    default List<FmsClosingSchemeDO> selectPeriodEndListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsClosingSchemeDO>()
                .eq(FmsClosingSchemeDO::getAccountSetId, accountSetId)
                .and(wrapper -> wrapper.ne(FmsClosingSchemeDO::getType,
                                FmsClosingTypeEnum.REGULAR.getType())
                        .or().eq(FmsClosingSchemeDO::getPeriodEnd, true))
                .orderByAsc(FmsClosingSchemeDO::getType)
                .orderByAsc(FmsClosingSchemeDO::getId));
    }

    default Long selectCountByAccountSetIdAndVoucherWordId(
            Long accountSetId, Long voucherWordId) {
        return selectCount(new LambdaQueryWrapperX<FmsClosingSchemeDO>()
                .eq(FmsClosingSchemeDO::getAccountSetId, accountSetId)
                .eq(FmsClosingSchemeDO::getVoucherWordId, voucherWordId));
    }

}
