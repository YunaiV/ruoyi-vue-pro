package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionAgentChatRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatReqVO;

import java.util.List;

/**
 * AI 服装设计智能体 Service
 *
 * <p>核心理念：<strong>纯自然语言驱动，零按钮操作，全自动链路执行</strong></p>
 *
 * <p>一句话即可触发完整多智能体协作链路，例如：</p>
 * <pre>
 * 用户：帮我设计5款甜酷风红色连衣裙然后转3D旋转90度看看
 *
 * 系统自动执行：
 *   ① BATCH_GENERATE  → 并行生成5款设计（SDXL→POSE→FABRIC→UPSAMPLE）
 *   ② GENERATE_3D     → 将第一款转为3D模型（深度估计→网格生成→纹理映射）
 *   ③ ROTATE          → 生成0/45/90/135...度旋转预览+GIF动图
 *
 * SSE实时推送每步进度，全程无需用户手动操作任何按钮。
 * </pre>
 *
 * @author deepay
 */
public interface AiFashionAgentService {

    /**
     * 处理用户消息：解析意图链 → 自动执行所有步骤 → SSE推送进度。
     *
     * <p>立即返回链路信息（chainId、步骤列表、SSE订阅URL），
     * 实际任务在后台异步执行，通过SSE推送进度。</p>
     *
     * @param userId 用户编号（来自登录上下文）
     * @param reqVO  请求（包含 message、sessionToken）
     * @return 立即返回的响应（含 chainId 和 SSE 订阅路径）
     */
    AiFashionAgentChatRespVO chat(Long userId, AiFashionSmartChatReqVO reqVO);

    /**
     * 查询某条链路的当前状态（用于无 SSE 降级场景的轮询）。
     *
     * @param chainId 链路编号
     * @return 链路所有步骤的最新状态列表
     */
    AiFashionAgentChatRespVO queryChain(String chainId);

    /**
     * 重试链路中某个失败的步骤。
     *
     * <p>失败步骤可通过 chainId + stepOrder 精确定位，
     * 从该步骤开始重新执行（后续步骤也会重新执行）。</p>
     *
     * @param chainId   链路编号
     * @param stepOrder 从哪一步重试（0-based）
     */
    void retryFromStep(String chainId, int stepOrder);

    /**
     * 获取用户最近 N 条链路的摘要（用于会话历史展示）。
     *
     * @param userId 用户编号
     * @param limit  最多返回数量
     * @return 链路摘要列表
     */
    List<AiFashionAgentChatRespVO> recentChains(Long userId, int limit);

}
