package cn.iocoder.yudao.module.pms.dal.mysql.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Mapper
public interface PmsKnowledgeFolderMapper extends BaseMapperX<PmsKnowledgeFolderDO> {

    default Set<Long> selectExistingPermissionIdSet(Collection<Long> permissionIds) {
        return convertSet(selectList(new LambdaQueryWrapperX<PmsKnowledgeFolderDO>()
                .in(PmsKnowledgeFolderDO::getPermissionId, permissionIds)
                .select(PmsKnowledgeFolderDO::getPermissionId)), PmsKnowledgeFolderDO::getPermissionId);
    }

    default List<PmsKnowledgeFolderDO> selectListByLibraryIdAndStatus(Long libraryId, Integer status) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFolderDO>()
                .eq(PmsKnowledgeFolderDO::getLibraryId, libraryId)
                .eq(PmsKnowledgeFolderDO::getStatus, status)
                .orderByAsc(PmsKnowledgeFolderDO::getCreateTime)
                .orderByAsc(PmsKnowledgeFolderDO::getId));
    }

    default List<PmsKnowledgeFolderDO> selectListByLibraryId(Long libraryId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFolderDO>()
                .eq(PmsKnowledgeFolderDO::getLibraryId, libraryId)
                .orderByAsc(PmsKnowledgeFolderDO::getId));
    }

    default List<PmsKnowledgeFolderDO> selectListByParentIds(Collection<Long> parentIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFolderDO>()
                .in(PmsKnowledgeFolderDO::getParentId, parentIds));
    }

    default int updateToRestoreByIds(Collection<Long> ids, Integer status) {
        return update(new LambdaUpdateWrapper<PmsKnowledgeFolderDO>()
                .set(PmsKnowledgeFolderDO::getStatus, status)
                .set(PmsKnowledgeFolderDO::getDeleteUserId, null)
                .set(PmsKnowledgeFolderDO::getDeleteTime, null)
                .in(PmsKnowledgeFolderDO::getId, ids));
    }

}
