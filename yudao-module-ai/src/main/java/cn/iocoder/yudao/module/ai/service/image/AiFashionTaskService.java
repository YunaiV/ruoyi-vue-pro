package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;

import java.util.List;

/**
 * AI 服装设计流水线任务 Service 接口
 *
 * <p>提供多模型流水线（SDXL → Pose ControlNet → Fabric img2img → Upscale）
 * 的完整后端闭环：任务创建、异步执行、查询、重试。</p>
 *
 * @author deepay
 */
public interface AiFashionTaskService {

    /**
     * 创建服装设计任务
     *
     * <p>根据请求参数创建主任务和步骤记录，立即返回 taskId，
     * 异步执行留给 {@link #executeTaskAsync(Long)}。</p>
     *
     * @param userId   当前登录用户编号
     * @param createVO 创建请求参数
     * @return 任务编号（taskId）
     */
    Long createTask(Long userId, AiFashionTaskCreateReqVO createVO);

    /**
     * 异步执行流水线任务
     *
     * <p>按步骤顺序调用 SD WebUI 模型：
     * SDXL → Pose（可选）→ Fabric（可选）→ Upscale（可选）。
     * 每步产物保存到 FileApi，步骤/任务状态实时回写数据库。</p>
     *
     * @param taskId 任务编号
     */
    void executeTaskAsync(Long taskId);

    /**
     * 查询任务详情（含步骤列表）
     *
     * @param id 任务编号
     * @return 任务 DO，若不存在返回 null
     */
    AiFashionTaskDO getTask(Long id);

    /**
     * 查询任务的所有步骤（按 stepOrder 升序）
     *
     * @param taskId 任务编号
     * @return 步骤列表
     */
    List<AiFashionTaskStepDO> getTaskSteps(Long taskId);

    /**
     * 分页查询任务列表
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    PageResult<AiFashionTaskDO> getTaskPage(AiFashionTaskPageReqVO pageReqVO);

    /**
     * 重试失败任务（从头重新执行全部步骤）
     *
     * <p>仅允许对状态为"已失败"的任务执行重试。
     * 重置任务及步骤状态后，异步重新执行流水线。</p>
     *
     * @param id 任务编号
     */
    void retryTask(Long id);

}
