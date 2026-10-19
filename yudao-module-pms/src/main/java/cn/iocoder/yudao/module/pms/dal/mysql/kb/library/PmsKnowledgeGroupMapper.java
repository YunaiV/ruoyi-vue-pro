package cn.iocoder.yudao.module.pms.dal.mysql.kb.library;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeGroupMapper extends BaseMapperX<PmsKnowledgeGroupDO> {

    default PmsKnowledgeGroupDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeGroupDO>()
                .eq(PmsKnowledgeGroupDO::getId, id)
                .eq(PmsKnowledgeGroupDO::getUserId, userId));
    }

    default PmsKnowledgeGroupDO selectByUserIdAndName(Long userId, String name, Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeGroupDO>()
                .eq(PmsKnowledgeGroupDO::getUserId, userId)
                .eq(PmsKnowledgeGroupDO::getName, name)
                .neIfPresent(PmsKnowledgeGroupDO::getId, excludeId));
    }

    default List<PmsKnowledgeGroupDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeGroupDO>()
                .eq(PmsKnowledgeGroupDO::getUserId, userId)
                .orderByAsc(PmsKnowledgeGroupDO::getSort)
                .orderByAsc(PmsKnowledgeGroupDO::getId));
    }

    default List<PmsKnowledgeGroupDO> selectListByIdsAndUserId(Collection<Long> ids, Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeGroupDO>()
                .in(PmsKnowledgeGroupDO::getId, ids)
                .eq(PmsKnowledgeGroupDO::getUserId, userId));
    }

    default PmsKnowledgeGroupDO selectLastByUserId(Long userId) {
        return selectLastOne(new LambdaQueryWrapperX<PmsKnowledgeGroupDO>()
                .eq(PmsKnowledgeGroupDO::getUserId, userId)
                .orderByAsc(PmsKnowledgeGroupDO::getSort)
                .orderByAsc(PmsKnowledgeGroupDO::getId));
    }

}
