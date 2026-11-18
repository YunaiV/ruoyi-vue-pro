package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatStreamReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardImportReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaChatTypeEnum;
import cn.iocoder.yudao.module.ai.service.drama.bo.AiDramaStoryboardPlanRespBO;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AI 短剧剧本 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaScriptService {

    /**
     * 获得项目的当前剧本：最新的版本
     *
     * @param projectId 项目编号
     * @return 剧本，可能为空
     */
    AiDramaScriptDO getScriptByProjectId(Long projectId);

    /**
     * 获得我的项目的全部剧本版本：新的在前
     *
     * @param userId 用户编号
     * @param projectId 项目编号
     * @return 剧本版本列表
     */
    List<AiDramaScriptDO> getScriptListMy(Long userId, Long projectId);

    /**
     * 获得多个项目的当前剧本编号
     *
     * @param projectIds 项目编号
     * @return key 为项目编号，value 为当前剧本编号；没有剧本的项目不在其中
     */
    Map<Long, Long> getLatestScriptIdMap(Collection<Long> projectIds);

    /**
     * 保存我的剧本：指定编号时更新该剧本版本，不新增版本；未指定时新增一个版本
     *
     * @param userId 用户编号
     * @param updateReqVO 剧本
     * @return 剧本编号
     */
    Long updateScriptMy(Long userId, AiDramaScriptUpdateReqVO updateReqVO);

    /**
     * 获得剧本
     *
     * @param id 剧本编号
     * @return 剧本
     */
    AiDramaScriptDO getScript(Long id);

    /**
     * 根据故事想法生成剧本：新增一个剧本版本
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 剧本
     */
    AiDramaScriptDO generateScriptMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 按修改意见打磨剧本：新增一个剧本版本，并记录修改意见
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
     * 由当前剧本拆分分镜：替换当前剧本的分镜，其它版本的保留；其中有镜头视频生成中时不允许
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void generateStoryboardListMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 为当前剧本的分镜生成摄影规则、表演指导或关键帧规划：按分镜顺序逐个更新；其中有镜头视频生成中时不允许
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @param type 类型：摄影规则、表演指导、关键帧规划
     */
    void generateStoryboardDetailMy(Long userId, AiDramaChatReqVO reqVO, AiDramaChatTypeEnum type);

    /**
     * 由当前剧本规划分镜：只返回，不保存，供审阅后导入
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 规划的分镜（未保存，没有编号），及其规划提示
     */
    List<AiDramaStoryboardPlanRespBO> planStoryboardListMy(Long userId, AiDramaChatReqVO reqVO);

    /**
     * 导入审阅后的分镜：替换当前剧本的分镜，项目置为分镜就绪；其中有镜头视频生成中时不允许
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void importStoryboardListMy(Long userId, AiDramaStoryboardImportReqVO reqVO);

    /**
     * 流式生成：生成剧本、打磨剧本、分析资产、拆分分镜；结束后解析入库
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 增量文本；失败时为错误
     */
    Flux<CommonResult<String>> streamChatMy(Long userId, AiDramaChatStreamReqVO reqVO);

    /**
     * 接续我的项目进行中的流式生成：先重放已生成的内容；没有进行中的生成时直接结束
     *
     * @param userId 用户编号
     * @param projectId 项目编号
     * @return 增量文本
     */
    Flux<CommonResult<String>> resumeChatStreamMy(Long userId, Long projectId);

    /**
     * 从故事想法创建项目：流式生成初版剧本，生成成功、解析成功后，在同一事务内创建项目、保存剧本
     *
     * 前端断开后仍在后台完成；失败时不创建项目；同一用户同时只允许一个
     *
     * @param userId 用户编号
     * @param reqVO 项目设置与故事想法
     * @return 增量文本；最后一条为项目编号
     */
    Flux<CommonResult<AiDramaScriptCreateRespVO>> createProjectStreamMy(Long userId, AiDramaScriptCreateReqVO reqVO);

    /**
     * 获得项目进行中的流式生成的类型
     *
     * @param projectId 项目编号
     * @return 类型，参见 AiDramaChatTypeEnum；没有时为空
     */
    Integer getRunningChatType(Long projectId);

    /**
     * 删除项目的剧本：全部版本
     *
     * @param projectId 项目编号
     */
    void deleteScriptByProjectId(Long projectId);

}
