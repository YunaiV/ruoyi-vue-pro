package cn.iocoder.yudao.module.market.service.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.ActivityCreateReqVO;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.ActivityPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.ActivityUpdateReqVO;
import cn.iocoder.yudao.module.market.convert.activity.ActivityConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.activity.ActivityDO;
import cn.iocoder.yudao.module.market.dal.mysql.activity.ActivityMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.ACTIVITY_NOT_EXISTS;

/**
 * 首页banner 实现类
 *
 * @author xia
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
