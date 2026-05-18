package cn.iocoder.yudao.module.activity.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityCreateReqVO;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityDO;

public interface ActivityService {

    /** 创建活动，召集人自动报名为第一个 member */
    Long createActivity(AppActivityCreateReqVO reqVO);

    /** 活动详情（含 members 列表） */
    AppActivityRespVO getActivityDetail(Long id);

    /** 报名（接龙），返回当前 current_count */
    int signup(Long activityId, Long userId);

    /** 取消报名 */
    void cancelSignup(Long activityId, Long userId);

    /** 我创建的活动 */
    PageResult<ActivityDO> pageMyCreated(Long userId, PageParam pageParam);

    /** 我参与的活动 */
    PageResult<AppActivityRespVO> pageMyJoined(Long userId, PageParam pageParam);

}
