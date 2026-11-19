package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskRespVO;

/**
 * AI 短剧生成任务 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaTaskService {

    /**
     * 获得生成任务分页
     *
     * 指定类型时直接分页；全部类型时，每类取前 pageNo * pageSize 条，合并后按更新时间倒序再分页
     * 模型筛选对角色形象、场景、视觉资产、镜头视频生效（都记录了上次出图、生成的模型），成片没有模型
     *
     * @param pageReqVO 分页查询
     * @return 分页；不含用户昵称
     */
    PageResult<AiDramaTaskRespVO> getTaskPage(AiDramaTaskPageReqVO pageReqVO);

    /**
     * 重新提交已失败的任务：使用上次的模型；成片使用当前剧本版本全部已完成的镜头重新合成
     *
     * @param reqVO 任务
     */
    void resubmitTask(AiDramaTaskActionReqVO reqVO);

    /**
     * 同步任务状态：只支持镜头视频
     *
     * @param reqVO 任务
     */
    void syncTask(AiDramaTaskActionReqVO reqVO);

    /**
     * 把进行中的任务标记为已失败
     *
     * @param reqVO 任务
     */
    void updateTaskFail(AiDramaTaskActionReqVO reqVO);

    /**
     * 超时处理：进行中且超过 60 分钟未更新的任务，置为已失败
     *
     * @return 处理的数量
     */
    int timeoutTasks();

}
