package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 凭证字 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsVoucherWordMapper extends BaseMapperX<FmsVoucherWordDO> {

    default List<FmsVoucherWordDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherWordDO>()
                .eq(FmsVoucherWordDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsVoucherWordDO::getSort)
                .orderByAsc(FmsVoucherWordDO::getId));
    }

    default FmsVoucherWordDO selectByAccountSetIdAndName(Long accountSetId, String name) {
        return selectOne(FmsVoucherWordDO::getAccountSetId, accountSetId,
                FmsVoucherWordDO::getName, name);
    }

    default FmsVoucherWordDO selectDefaultByAccountSetId(Long accountSetId) {
        return selectFirstOne(FmsVoucherWordDO::getAccountSetId, accountSetId,
                FmsVoucherWordDO::getDefaultStatus, true);
    }

    default FmsVoucherWordDO selectLastByAccountSetId(Long accountSetId) {
        return selectLastOne(new LambdaQueryWrapperX<FmsVoucherWordDO>()
                .eq(FmsVoucherWordDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsVoucherWordDO::getSort)
                .orderByAsc(FmsVoucherWordDO::getId));
    }

}
