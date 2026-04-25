package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;

import java.util.List;
import java.util.Map;

/**
 * AI 服装设计模型协调器接口
 *
 * <p>对应 Python 参考代码中的 {@code ModelOrchestrator} + {@code FashionDesignOrchestrator}。</p>
 *
 * <p>职责：
 * <ul>
 *   <li>根据 workflowMode + 任务参数构建有序阶段列表</li>
 *   <li>并发预加载参考图片，减少总流水线耗时</li>
 *   <li>顺序执行各阶段，每步回写数据库</li>
 *   <li>提供 Prompt 增强、质量评估能力</li>
 * </ul>
 * </p>
 *
 * @author deepay
 */
public interface AiFashionModelOrchestratorService {

    /**
     * 根据工作流模式与任务参数构建有序步骤列表（DO 实例，尚未入库）
     *
     * @param task 主任务
     * @return 有序步骤列表
     */
    List<AiFashionTaskStepDO> buildStageList(AiFashionTaskDO task);

    /**
     * 执行完整流水线
     *
     * <p>从数据库加载已有步骤，按顺序调用 SD WebUI 各端点，
     * 每步产物存入 FileApi 并回写步骤状态。</p>
     *
     * @param task  主任务
     * @param steps 待执行的步骤列表（按 stepOrder 升序）
     * @return 最终产物图片 URL
     */
    String executePipeline(AiFashionTaskDO task, List<AiFashionTaskStepDO> steps);

    /**
     * 评估图片质量，返回各维度指标
     *
     * @param imageBytes 图片字节（PNG / JPEG）
     * @return 质量指标 Map：sharpness、contrast、colorfulness、resolution
     */
    Map<String, Object> evaluateQuality(byte[] imageBytes);

}
