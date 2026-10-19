package cn.iocoder.yudao.module.oa.dal.mysql.task;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * OA 任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaTaskMapper extends BaseMapperX<OaTaskDO> {

    default Map<Long, Long> selectCountMapByStatusGroupByCreator(Integer status) {
        List<Map<String, Object>> counts = selectMaps(new MPJLambdaWrapperX<OaTaskDO>()
                .selectAs(OaTaskDO::getCreator, "userId")
                .selectCount(OaTaskDO::getId, "count")
                .eq(OaTaskDO::getStatus, status)
                .groupBy(OaTaskDO::getCreator));
        return convertMap(counts, row -> MapUtil.getLong(row, "userId"), row -> MapUtil.getLong(row, "count"));
    }

    default PageResult<OaTaskDO> selectPublishedPage(OaTaskPageReqVO pageReqVO, Long publisherUserId) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OaTaskDO>()
                .eq(OaTaskDO::getCreator, publisherUserId.toString())
                .likeIfPresent(OaTaskDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaTaskDO::getType, pageReqVO.getType())
                .eqIfPresent(OaTaskDO::getStatus, pageReqVO.getStatus())
                .eqIfPresent(OaTaskDO::getCanceled, pageReqVO.getCanceled())
                .betweenIfPresent(OaTaskDO::getPublishTime, pageReqVO.getPublishTime())
                .orderByDesc(OaTaskDO::getTop)
                .orderByDesc(OaTaskDO::getUpdateTime)
                .orderByDesc(OaTaskDO::getId));
    }

    default PageResult<OaTaskDO> selectReceivedPage(OaTaskPageReqVO pageReqVO, Collection<Long> taskIds) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OaTaskDO>()
                .in(OaTaskDO::getId, taskIds)
                .likeIfPresent(OaTaskDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaTaskDO::getType, pageReqVO.getType())
                .eqIfPresent(OaTaskDO::getCreator, StrUtil.toStringOrNull(pageReqVO.getPublisherUserId()))
                .eqIfPresent(OaTaskDO::getCanceled, pageReqVO.getCanceled())
                .betweenIfPresent(OaTaskDO::getPublishTime, pageReqVO.getPublishTime())
                .orderByAsc(OaTaskDO::getCanceled)
                .orderByAsc(OaTaskDO::getStatus)
                .orderByDesc(OaTaskDO::getUpdateTime)
                .orderByDesc(OaTaskDO::getId));
    }

}
