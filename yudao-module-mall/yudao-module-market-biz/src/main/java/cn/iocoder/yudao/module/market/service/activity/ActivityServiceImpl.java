package cn.iocoder.yudao.module.market.service.activity;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.*;
import cn.iocoder.yudao.module.market.dal.dataobject.activity.ActivityDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.market.convert.activity.ActivityConvert;
import cn.iocoder.yudao.module.market.dal.mysql.activity.ActivityMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 促销活动 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Override
    public Long createActivity(ActivityCreateReqVO createReqVO) {
        // 插入
        ActivityDO activity = ActivityConvert.INSTANCE.convert(createReqVO);
        activityMapper.insert(activity);
        // 返回
        return activity.getId();
    }

    @Override
    public void updateActivity(ActivityUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateActivityExists(updateReqVO.getId());
        // 更新
        ActivityDO updateObj = ActivityConvert.INSTANCE.convert(updateReqVO);
        activityMapper.updateById(updateObj);
    }

    @Override
    public void deleteActivity(Long id) {
        // 校验存在
        this.validateActivityExists(id);
        // 删除
        activityMapper.deleteById(id);
    }

    private void validateActivityExists(Long id) {
        if (activityMapper.selectById(id) == null) {
            throw exception(ACTIVITY_NOT_EXISTS);
        }
    }

    @Override
    public ActivityDO getActivity(Long id) {
        return activityMapper.selectById(id);
    }

    @Override
    public List<ActivityDO> getActivityList(Collection<Long> ids) {
        return activityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ActivityDO> getActivityPage(ActivityPageReqVO pageReqVO) {
        return activityMapper.selectPage(pageReqVO);
    }

}
