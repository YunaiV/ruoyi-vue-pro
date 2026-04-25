package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionCollectionSourceDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionModelLibraryDO;

import java.util.List;

/**
 * AI 服装素材库 Service 接口
 *
 * <p>提供：</p>
 * <ul>
 *   <li>分页/搜索素材库（时装秀、品牌、模特机构图片）</li>
 *   <li>模特详情查询（含身体特征）</li>
 *   <li>统计信息（品类、体型、肤色、姿势分布）</li>
 *   <li>采集源列表与触发采集（对应 Python EnhancedImageCollector）</li>
 *   <li>AI 悬浮对话框状态维护（对应 Python AIConversationManager.page_states）</li>
 * </ul>
 *
 * @author deepay
 */
public interface AiFashionModelLibraryService {

    // ========== 素材库 ==========

    /**
     * 分页查询素材库（支持关键词/来源类型/品类/品牌/是否含模特等过滤）
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    PageResult<AiFashionModelLibraryRespVO> getLibraryPage(AiFashionModelLibraryPageReqVO pageReqVO);

    /**
     * 查询单条素材详情（含模特特征，若有）
     *
     * @param id 素材编号
     * @return 素材详情 VO，不存在返回 null
     */
    AiFashionModelLibraryRespVO getLibraryDetail(Long id);

    /**
     * 素材库整体统计（品类分布、体型分布、采集源数等）
     *
     * @return 统计 VO
     */
    AiFashionModelStatsRespVO getStats();

    // ========== 采集源 ==========

    /**
     * 查询所有采集源列表
     *
     * @param sourceType 来源类型过滤（null 则返回全部）
     * @return 采集源列表
     */
    List<AiFashionCollectionSourceDO> listSources(String sourceType);

    /**
     * 触发素材采集（同步或异步，对应 Python EnhancedImageCollector.collect_*）
     *
     * <p>根据请求参数决定采集 单个来源 / 指定类型 / 全部 active 来源。
     * async=true 时立即返回 collectJobId，前端轮询进度。</p>
     *
     * @param reqVO 采集请求参数
     * @return 采集结果
     */
    AiFashionCollectRespVO triggerCollect(AiFashionCollectReqVO reqVO);

    // ========== AI 悬浮对话框 ==========

    /**
     * 处理页面 AI 悬浮对话框消息
     *
     * <p>根据当前 pageName 路由到对应的回复模板，
     * 同时持久化悬浮窗位置与最小化状态（对应 Python AIConversationManager）。</p>
     *
     * @param userId 当前用户 ID
     * @param reqVO  请求参数
     * @return 回复 VO（含 AI 文本 + 行动指令 + 悬浮窗状态）
     */
    AiFashionPageChatRespVO pageChat(Long userId, AiFashionPageChatReqVO reqVO);

    /**
     * 更新悬浮窗位置与最小化状态（前端拖动/点击最小化时调用）
     *
     * @param userId    当前用户 ID
     * @param pageName  页面名称
     * @param minimized 是否最小化
     * @param x         X 坐标
     * @param y         Y 坐标
     */
    void updateAssistantState(Long userId, String pageName, Boolean minimized, Integer x, Integer y);

}
