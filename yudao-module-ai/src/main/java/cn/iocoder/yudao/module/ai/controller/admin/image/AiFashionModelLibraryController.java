package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionCollectionSourceDO;
import cn.iocoder.yudao.module.ai.service.image.AiFashionModelLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI 服装素材库 & 悬浮对话框
 *
 * <p>对外暴露：</p>
 * <ul>
 *   <li>GET  /ai/fashion/library/page          — 分页查询素材（时装秀/品牌/模特库）</li>
 *   <li>GET  /ai/fashion/library/detail        — 素材详情（含模特特征）</li>
 *   <li>GET  /ai/fashion/library/stats         — 素材库统计</li>
 *   <li>GET  /ai/fashion/library/sources       — 采集源列表</li>
 *   <li>POST /ai/fashion/library/collect       — 触发图片采集</li>
 *   <li>POST /ai/fashion/library/page-chat     — AI 悬浮对话框（页面感知）</li>
 *   <li>PUT  /ai/fashion/library/assistant-state — 更新悬浮窗位置/最小化</li>
 * </ul>
 *
 * @author deepay
 */
@Tag(name = "管理后台 - AI 服装素材库 & 悬浮对话框")
@RestController
@RequestMapping("/ai/fashion/library")
@Validated
@Slf4j
public class AiFashionModelLibraryController {

    @Resource
    private AiFashionModelLibraryService libraryService;

    // ========== 素材库 ==========

    @GetMapping("/page")
    @Operation(summary = "分页查询素材库",
            description = "支持关键词/来源类型/品类/品牌/模特体型等过滤，适用于图库页面和模特库页面")
    public CommonResult<PageResult<AiFashionModelLibraryRespVO>> getLibraryPage(
            @Validated AiFashionModelLibraryPageReqVO pageReqVO) {
        return success(libraryService.getLibraryPage(pageReqVO));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询素材详情（含模特特征）")
    @Parameter(name = "id", description = "素材编号", required = true, example = "1")
    public CommonResult<AiFashionModelLibraryRespVO> getLibraryDetail(@RequestParam("id") Long id) {
        return success(libraryService.getLibraryDetail(id));
    }

    @GetMapping("/stats")
    @Operation(summary = "素材库统计",
            description = "返回总数、品类分布、体型分布、肤色分布、姿势分布、品牌分布等统计数据")
    public CommonResult<AiFashionModelStatsRespVO> getStats() {
        return success(libraryService.getStats());
    }

    // ========== 采集源 ==========

    @GetMapping("/sources")
    @Operation(summary = "查询采集源列表",
            description = "返回预置的 10 大时装秀媒体 + 10 大品牌 + 5 大模特机构（共 25 个来源）")
    @Parameter(name = "sourceType", description = "来源类型过滤（fashion_show/brand/model_agency/street_style），不填返回全部", required = false)
    @PreAuthorize("@ss.hasPermission('ai:fashion-library:query')")
    public CommonResult<List<AiFashionCollectionSourceDO>> listSources(
            @RequestParam(value = "sourceType", required = false) String sourceType) {
        return success(libraryService.listSources(sourceType));
    }

    @PostMapping("/collect")
    @Operation(summary = "触发素材采集",
            description = "可指定单个来源 ID 或按类型批量采集；async=true 时立即返回 collectJobId，前端轮询进度")
    @PreAuthorize("@ss.hasPermission('ai:fashion-library:collect')")
    public CommonResult<AiFashionCollectRespVO> triggerCollect(
            @Valid @RequestBody AiFashionCollectReqVO reqVO) {
        return success(libraryService.triggerCollect(reqVO));
    }

    // ========== AI 悬浮对话框 ==========

    @PostMapping("/page-chat")
    @Operation(summary = "AI 悬浮对话框（页面感知）",
            description = "根据当前页面（home/model_library/design_studio/image_library/3d_viewer）" +
                    "生成专属回复，同时持久化悬浮窗的位置与最小化状态。" +
                    "前端在任意页面挂载此组件，无需手动操作，一句话即可触发对应页面的智能行动。")
    public CommonResult<AiFashionPageChatRespVO> pageChat(
            @Valid @RequestBody AiFashionPageChatReqVO reqVO) {
        return success(libraryService.pageChat(getLoginUserId(), reqVO));
    }

    @PutMapping("/assistant-state")
    @Operation(summary = "更新 AI 悬浮窗状态",
            description = "前端拖动悬浮窗或点击最小化时调用，持久化位置与折叠状态")
    @Parameter(name = "pageName",  description = "页面名称", required = true,  example = "model_library")
    @Parameter(name = "minimized", description = "是否最小化",required = false, example = "true")
    @Parameter(name = "x",         description = "X 坐标",   required = false, example = "20")
    @Parameter(name = "y",         description = "Y 坐标",   required = false, example = "20")
    public CommonResult<Boolean> updateAssistantState(
            @RequestParam("pageName")            String  pageName,
            @RequestParam(value = "minimized", required = false) Boolean minimized,
            @RequestParam(value = "x",         required = false) Integer x,
            @RequestParam(value = "y",         required = false) Integer y) {
        libraryService.updateAssistantState(getLoginUserId(), pageName, minimized, x, y);
        return success(true);
    }

}
