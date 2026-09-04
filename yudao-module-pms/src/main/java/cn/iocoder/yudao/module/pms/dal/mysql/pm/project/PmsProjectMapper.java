package cn.iocoder.yudao.module.pms.dal.mysql.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSortTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsProjectMapper extends BaseMapperX<PmsProjectDO> {

    default List<PmsProjectDO> selectListByIdsAndStatus(Collection<Long> ids, Integer status) {
        return selectList(new LambdaQueryWrapperX<PmsProjectDO>()
                .in(PmsProjectDO::getId, ids)
                .eq(PmsProjectDO::getStatus, status)
                .orderByDesc(PmsProjectDO::getAccessTime)
                .orderByDesc(PmsProjectDO::getId));
    }

    /**
     * 分页查询项目，并按访问范围过滤项目
     *
     * @param pageReqVO 分页查询条件
     * @param projectIds 当前用户可访问的项目编号集合
     * @param includeOpenProject 是否包含公开项目
     * @param includeAllProject 是否包含全部项目
     * @return 项目分页结果
     */
    default PageResult<PmsProjectDO> selectPage(PmsProjectPageReqVO pageReqVO, Collection<Long> projectIds,
                                                boolean includeOpenProject, boolean includeAllProject) {
        LambdaQueryWrapperX<PmsProjectDO> query = new LambdaQueryWrapperX<PmsProjectDO>()
                .likeIfPresent(PmsProjectDO::getName, pageReqVO.getName())
                .eq(PmsProjectDO::getStatus, pageReqVO.getStatus());
        if (!includeAllProject) {
            if (includeOpenProject) {
                query.and(wrapper -> {
                    wrapper.eq(PmsProjectDO::getOpenStatus, true);
                    if (CollUtil.isNotEmpty(projectIds)) {
                        wrapper.or().in(PmsProjectDO::getId, projectIds);
                    }
                });
            } else {
                query.in(PmsProjectDO::getId, projectIds);
            }
        }
        if (PmsProjectSortTypeEnum.ACCESS_TIME.getType().equals(pageReqVO.getSortType())) {
            query.orderByDesc(PmsProjectDO::getAccessTime);
        } else {
            query.orderByAsc(PmsProjectDO::getCreateTime);
        }
        query.orderByDesc(PmsProjectDO::getId);
        return selectPage(pageReqVO, query);
    }

    default int updateStatusAndArchiveTimeById(Long id, Integer sourceStatus, Integer status,
                                               LocalDateTime archiveTime) {
        return update(new LambdaUpdateWrapper<PmsProjectDO>().eq(PmsProjectDO::getId, id)
                .eq(PmsProjectDO::getStatus, sourceStatus)
                .set(PmsProjectDO::getStatus, status)
                .set(PmsProjectDO::getArchiveTime, archiveTime));
    }

    default int updateStatusAndRecycleTimeById(Long id, Integer sourceStatus, Integer status,
                                               LocalDateTime recycleTime) {
        return update(new LambdaUpdateWrapper<PmsProjectDO>().eq(PmsProjectDO::getId, id)
                .eq(PmsProjectDO::getStatus, sourceStatus)
                .set(PmsProjectDO::getStatus, status)
                .set(PmsProjectDO::getRecycleTime, recycleTime));
    }

    default int updateToRestoreById(Long id, Integer status) {
        return update(new LambdaUpdateWrapper<PmsProjectDO>().eq(PmsProjectDO::getId, id)
                .in(PmsProjectDO::getStatus, Arrays.asList(PmsProjectStatusEnum.ARCHIVED.getStatus(),
                        PmsProjectStatusEnum.RECYCLED.getStatus()))
                .set(PmsProjectDO::getStatus, status)
                .set(PmsProjectDO::getArchiveTime, null)
                .set(PmsProjectDO::getRecycleTime, null));
    }

}
