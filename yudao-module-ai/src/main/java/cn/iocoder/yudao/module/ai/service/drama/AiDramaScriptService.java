package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;

// DONE @AI：详细注释，只在实现类上写“每个项目一份剧本；生成、打磨、分析、拆分都同步调用对话模型”；其它也检查下；（已移到实现类；分镜、生成任务 Service 接口上的实现细节一并移到实现类，资产 Service 的出图说明同样处理）
/**
 * AI 短剧剧本 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaScriptService {

    /**
     * 获得项目的剧本
     *
     * @param projectId 项目编号
     * @return 剧本，可能为空
     */
    AiDramaScriptDO getScriptByProjectId(Long projectId);

    /**
     * 保存我的剧本：不存在时创建
     *
     * @param userId 用户编号
     * @param updateReqVO 剧本
     */
    void updateScriptMy(Long userId, AiDramaScriptUpdateReqVO updateReqVO);

    /**
     * 根据故事想法生成剧本：覆盖已有的剧本内容
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 剧本
     */
    AiDramaScriptDO generateScriptMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 按修改意见打磨剧本，并记录修改意见
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 剧本
     */
    AiDramaScriptDO polishScriptMy(Long userId, AiDramaScriptPolishReqVO reqVO);

    /**
     * 从剧本分析角色、场景、道具：只新增名称不重复的，已有的保持不变
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void analyzeAssetListMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 由剧本拆分分镜：替换项目已有的全部分镜；有镜头视频生成中时不允许
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void generateStoryboardListMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 删除项目的剧本
     *
     * @param projectId 项目编号
     */
    void deleteScriptByProjectId(Long projectId);

}
