package cn.iocoder.yudao.module.pms.dal.mysql.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeContentPermissionMapper extends BaseMapperX<PmsKnowledgeContentPermissionDO> {

    default List<PmsKnowledgeContentPermissionDO> selectListByLibraryIds(Collection<Long> libraryIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionDO>()
                .in(PmsKnowledgeContentPermissionDO::getLibraryId, libraryIds));
    }

    default List<PmsKnowledgeContentPermissionDO> selectListByLibraryId(Long libraryId) {
        return selectList(PmsKnowledgeContentPermissionDO::getLibraryId, libraryId);
    }

}
