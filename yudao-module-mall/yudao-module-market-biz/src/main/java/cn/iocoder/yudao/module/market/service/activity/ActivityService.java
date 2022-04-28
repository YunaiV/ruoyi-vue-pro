package cn.iocoder.yudao.module.market.service.activity;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.*;
import cn.iocoder.yudao.module.market.dal.dataobject.activity.ActivityDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 促销活动 Service 接口
 *
 * @author 芋道源码
 */
public interface ActivityService {

    /**
     * 创建促销活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createActivity(@Valid ActivityCreateReqVO createReqVO);

    /**
     * 更新促销活动
     *
     * @param updateReqVO 更新信息
     */
    void updateActivity(@Valid ActivityUpdateReqVO updateReqVO);

    /**
     * 删除促销活动
     *
     * @param id 编号
     */
    void deleteActivity(Long id);

    /**
     * 获得促销活动
     *
     * @param id 编号
     * @return 促销活动
     */
    ActivityDO getActivity(Long id);

    /**
     * 获得促销活动列表
     *
     * @param ids 编号
     * @return 促销活动列表
     */
    List<ActivityDO> getActivityList(Collection<Long> ids);

    /**
     * 获得促销活动分页
     *
     * @param pageReqVO 分页查询
     * @return 促销活动分页
     */
    PageResult<ActivityDO> getActivityPage(ActivityPageReqVO pageReqVO);

}
