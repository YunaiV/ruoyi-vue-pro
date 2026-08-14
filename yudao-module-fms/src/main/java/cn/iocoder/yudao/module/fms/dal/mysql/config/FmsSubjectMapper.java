package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * FMS 会计科目 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsSubjectMapper extends BaseMapperX<FmsSubjectDO> {

    default List<FmsSubjectDO> selectListByAccountSetIdAndType(Long accountSetId, Integer type) {
        return selectList(new LambdaQueryWrapperX<FmsSubjectDO>()
                .eq(FmsSubjectDO::getAccountSetId, accountSetId)
                .eqIfPresent(FmsSubjectDO::getType, type)
                .orderByAsc(FmsSubjectDO::getCode)
                .orderByAsc(FmsSubjectDO::getId));
    }

    default FmsSubjectDO selectByAccountSetIdAndCode(Long accountSetId, String code) {
        return selectOne(new LambdaQueryWrapperX<FmsSubjectDO>()
                .eq(FmsSubjectDO::getAccountSetId, accountSetId)
                .eq(FmsSubjectDO::getCode, code));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(FmsSubjectDO::getParentId, parentId);
    }

    default Long selectCountByParentIds(Collection<Long> parentIds) {
        return selectCount(new LambdaQueryWrapperX<FmsSubjectDO>()
                .in(FmsSubjectDO::getParentId, parentIds));
    }

    default List<FmsSubjectDO> selectListByParentIds(Long accountSetId, Collection<Long> parentIds) {
        return selectList(new LambdaQueryWrapperX<FmsSubjectDO>()
                .eq(FmsSubjectDO::getAccountSetId, accountSetId)
                .in(FmsSubjectDO::getParentId, parentIds)
                .orderByAsc(FmsSubjectDO::getCode)
                .orderByAsc(FmsSubjectDO::getId));
    }

    default FmsSubjectDO selectByIdAndAccountSetId(Long id, Long accountSetId) {
        return selectOne(FmsSubjectDO::getId, id,
                FmsSubjectDO::getAccountSetId, accountSetId);
    }

    default List<FmsSubjectDO> selectListByIdsAndAccountSetId(Collection<Long> ids, Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsSubjectDO>()
                .in(FmsSubjectDO::getId, ids)
                .eq(FmsSubjectDO::getAccountSetId, accountSetId));
    }

    default void updateStatusByIdsAndAccountSetId(Collection<Long> ids, Long accountSetId, FmsSubjectDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<FmsSubjectDO>()
                .in(FmsSubjectDO::getId, ids)
                .eq(FmsSubjectDO::getAccountSetId, accountSetId));
    }

}
