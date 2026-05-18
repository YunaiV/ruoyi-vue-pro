package cn.iocoder.yudao.module.activity.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivityMemberMapper extends BaseMapperX<ActivityMemberDO> {

    default List<ActivityMemberDO> selectListByActivity(Long activityId) {
        return selectList(new LambdaQueryWrapperX<ActivityMemberDO>()
                .eq(ActivityMemberDO::getActivityId, activityId)
                .orderByAsc(ActivityMemberDO::getJoinedAt));
    }

    default ActivityMemberDO selectByActivityAndUser(Long activityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ActivityMemberDO>()
                .eq(ActivityMemberDO::getActivityId, activityId)
                .eq(ActivityMemberDO::getUserId, userId));
    }

    /** 我参与的活动 ID 列表（按报名时间倒序） */
    default PageResult<ActivityMemberDO> selectPageByUser(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ActivityMemberDO>()
                .eq(ActivityMemberDO::getUserId, userId)
                .orderByDesc(ActivityMemberDO::getJoinedAt));
    }

}
