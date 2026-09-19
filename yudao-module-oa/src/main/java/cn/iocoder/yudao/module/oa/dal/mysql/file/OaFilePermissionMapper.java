package cn.iocoder.yudao.module.oa.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.enums.file.OaFileSubjectTypeEnum;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 云盘共享权限 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaFilePermissionMapper extends BaseMapperX<OaFilePermissionDO> {

    default List<OaFilePermissionDO> selectListByUserIdAndDeptIdAndExpireTimeAfter(Long userId, Long deptId, LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<OaFilePermissionDO>()
                // 筛选主体
                .and(query -> {
                    query.eq(OaFilePermissionDO::getSubjectType, OaFileSubjectTypeEnum.USER.getType()).eq(OaFilePermissionDO::getSubjectId, userId);
                    if (deptId != null) {
                        query.or().eq(OaFilePermissionDO::getSubjectType, OaFileSubjectTypeEnum.DEPT.getType()).eq(OaFilePermissionDO::getSubjectId, deptId);
                    }
                })
                // 筛选权限未过期
                .and(query -> query.isNull(OaFilePermissionDO::getExpireTime)
                        .or().gt(OaFilePermissionDO::getExpireTime, now)));
    }

    default void updatePermission(OaFilePermissionDO permission) {
        // 到期时间允许清空，明确更新 null，避免旧到期时间残留
        update(null, new LambdaUpdateWrapper<OaFilePermissionDO>()
                .eq(OaFilePermissionDO::getId, permission.getId())
                .set(OaFilePermissionDO::getLevel, permission.getLevel())
                .set(OaFilePermissionDO::getInherit, permission.getInherit())
                .set(OaFilePermissionDO::getExpireTime, permission.getExpireTime()));
    }

    default List<OaFilePermissionDO> selectListByNodeId(Long nodeId) {
        return selectList(OaFilePermissionDO::getNodeId, nodeId);
    }

    default List<OaFilePermissionDO> selectListByNodeIds(Collection<Long> nodeIds) {
        return selectList(OaFilePermissionDO::getNodeId, nodeIds);
    }

    default OaFilePermissionDO selectByNodeIdAndSubjectTypeAndSubjectId(Long nodeId, Integer subjectType, Long subjectId) {
        return selectOne(new LambdaQueryWrapperX<OaFilePermissionDO>()
                .eq(OaFilePermissionDO::getNodeId, nodeId)
                .eq(OaFilePermissionDO::getSubjectType, subjectType)
                .eq(OaFilePermissionDO::getSubjectId, subjectId));
    }

    default void deleteByNodeIds(Collection<Long> nodeIds) {
        delete(new LambdaQueryWrapperX<OaFilePermissionDO>().in(OaFilePermissionDO::getNodeId, nodeIds));
    }
}
