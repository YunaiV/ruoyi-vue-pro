package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * FMS 辅助核算组合 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsAuxiliaryCombinationMapper extends BaseMapperX<FmsAuxiliaryCombinationDO> {

    default List<FmsAuxiliaryCombinationDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsAuxiliaryCombinationDO>()
                .eq(FmsAuxiliaryCombinationDO::getAccountSetId, accountSetId));
    }

    default Long selectCountByAccountSetIdAndSubjectIds(
            Long accountSetId, Collection<Long> subjectIds) {
        return selectCount(new LambdaQueryWrapperX<FmsAuxiliaryCombinationDO>()
                .eq(FmsAuxiliaryCombinationDO::getAccountSetId, accountSetId)
                .in(FmsAuxiliaryCombinationDO::getSubjectId, subjectIds));
    }

    default void updateSubject(Long accountSetId, Long subjectId, FmsAuxiliaryCombinationDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<FmsAuxiliaryCombinationDO>()
                .eq(FmsAuxiliaryCombinationDO::getAccountSetId, accountSetId)
                .eq(FmsAuxiliaryCombinationDO::getSubjectId, subjectId));
    }

}
