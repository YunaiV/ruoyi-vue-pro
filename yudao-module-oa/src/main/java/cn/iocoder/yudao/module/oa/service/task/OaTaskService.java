package cn.iocoder.yudao.module.oa.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskFeedbackReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskLogDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskReceiverDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * OA 任务 Service 接口
 *
 * @author 芋道源码
 */
public interface OaTaskService {

    /**
     * 创建任务
     *
     * @param createReqVO 新增信息
     * @param userId 发布人用户编号
     * @return 任务编号
     */
    Long createTask(OaTaskSaveReqVO createReqVO, Long userId);

    /**
     * 更新任务
     *
     * @param updateReqVO 修改信息
     * @param userId 发布人用户编号
     */
    void updateTask(OaTaskSaveReqVO updateReqVO, Long userId);

    /**
     * 删除发布的任务
     *
     * @param id 任务编号
     * @param userId 发布人用户编号
     */
    void deleteTask(Long id, Long userId);

    /**
     * 删除接收的任务
     *
     * @param id 任务编号
     * @param userId 接收人用户编号
     */
    void deleteReceivedTask(Long id, Long userId);

    /**
     * 获得任务详情
     *
     * @param id 任务编号
     * @param userId 用户编号
     * @return 任务
     */
    OaTaskDO getTask(Long id, Long userId);

    /**
     * 获得我发布的任务分页
     *
     * @param pageReqVO 分页查询
     * @param userId 发布人用户编号
     * @return 任务分页
     */
    PageResult<OaTaskDO> getPublishedTaskPage(OaTaskPageReqVO pageReqVO, Long userId);

    /**
     * 获得我的任务分页
     *
     * @param pageReqVO 分页查询
     * @param userId 接收人用户编号
     * @return 任务分页
     */
    PageResult<OaTaskDO> getReceivedTaskPage(OaTaskPageReqVO pageReqVO, Long userId);

    /**
     * 创建任务反馈
     *
     * @param createReqVO 反馈信息
     * @param userId 反馈人用户编号
     */
    void feedbackTask(OaTaskFeedbackReqVO createReqVO, Long userId);

    /**
     * 获得任务接收人列表
     *
     * @param taskIds 任务编号集合
     * @return 任务接收人列表
     */
    List<OaTaskReceiverDO> getTaskReceiverList(Collection<Long> taskIds);

    /**
     * 获得按任务编号分组的接收人列表 Map
     *
     * @param taskIds 任务编号集合
     * @return 任务编号与接收人列表的 Map
     */
    default Map<Long, List<OaTaskReceiverDO>> getTaskReceiverListMap(Collection<Long> taskIds) {
        return convertMultiMap(getTaskReceiverList(taskIds), OaTaskReceiverDO::getTaskId);
    }

    /**
     * 获得任务反馈日志列表
     *
     * @param taskId 任务编号
     * @return 任务反馈日志列表
     */
    List<OaTaskLogDO> getTaskLogList(Long taskId);

    /**
     * 获得当前用户各任务状态的数量 Map
     *
     * @param userId 用户编号
     * @return 任务状态与数量的 Map
     */
    Map<Integer, Long> getTaskStatusCountMap(Long userId);

    /**
     * 获得各发布人已完成任务数量排行 Map
     *
     * @return 发布人用户编号与总体已完成任务数量的 Map
     */
    Map<Long, Long> getCompletedTaskCountMapByUserId();

}
