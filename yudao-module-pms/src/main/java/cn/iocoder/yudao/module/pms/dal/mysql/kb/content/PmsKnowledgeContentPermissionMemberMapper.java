package cn.iocoder.yudao.module.pms.dal.mysql.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeContentPermissionMemberMapper extends BaseMapperX<PmsKnowledgeContentPermissionMemberDO> {

    default List<PmsKnowledgeContentPermissionMemberDO> selectListByPermissionId(Long permissionId) {
        return selectList(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionId);
    }

    default List<PmsKnowledgeContentPermissionMemberDO> selectListByPermissionIds(Collection<Long> permissionIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionMemberDO>()
                .in(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionIds));
    }

    default PmsKnowledgeContentPermissionMemberDO selectByPermissionIdAndUserId(Long permissionId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionMemberDO>()
                .eq(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionId)
                .eq(PmsKnowledgeContentPermissionMemberDO::getUserId, userId));
    }

    default void deleteByPermissionId(Long permissionId) {
        delete(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionId);
    }

    default void deleteByPermissionIds(Collection<Long> permissionIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionMemberDO>()
                .in(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionIds));
    }

    default void deleteByPermissionIdsAndUserIds(Collection<Long> permissionIds, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionMemberDO>()
                .in(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionIds)
                .in(PmsKnowledgeContentPermissionMemberDO::getUserId, userIds));
    }

    default void deleteByPermissionIdsAndDeptIds(Collection<Long> permissionIds, Collection<Long> deptIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeContentPermissionMemberDO>()
                .in(PmsKnowledgeContentPermissionMemberDO::getPermissionId, permissionIds)
                .in(PmsKnowledgeContentPermissionMemberDO::getDeptId, deptIds));
    }

}
