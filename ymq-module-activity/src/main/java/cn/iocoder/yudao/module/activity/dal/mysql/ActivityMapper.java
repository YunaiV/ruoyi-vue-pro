package cn.iocoder.yudao.module.activity.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper extends BaseMapperX<ActivityDO> {

    default ActivityDO selectByShortCode(String shortCode) {
        return selectOne(ActivityDO::getShortCode, shortCode);
    }

    /** 我创建的活动（按开始时间倒序） */
    default PageResult<ActivityDO> selectPageByCreator(Long creatorId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ActivityDO>()
                .eq(ActivityDO::getCreatorId, creatorId)
                .orderByDesc(ActivityDO::getStartTime));
    }

}
