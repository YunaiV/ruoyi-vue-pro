package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatStreamReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardImportReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardRespVO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaChatTypeEnum;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 短剧剧本")
@RestController
@RequestMapping("/ai/drama/script")
public class AiDramaScriptController {

    @Resource
    private AiDramaScriptService scriptService;

    @PutMapping("/update")
    @Operation(summary = "保存【我的】剧本", description = "指定编号时更新该剧本版本，不新增版本；未指定时新增版本，返回剧本编号")
    public CommonResult<Long> updateScript(@Valid @RequestBody AiDramaScriptUpdateReqVO updateReqVO) {
        return success(scriptService.updateScriptMy(getLoginUserId(), updateReqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得【我的】项目的剧本版本列表", description = "新的在前，第一条为当前剧本；只读查看，不支持回滚")
    @Parameter(name = "projectId", required = true, description = "项目编号", example = "1024")
    public CommonResult<List<AiDramaScriptRespVO>> getScriptList(@RequestParam("projectId") Long projectId) {
        return success(BeanUtils.toBean(scriptService.getScriptListMy(getLoginUserId(), projectId), AiDramaScriptRespVO.class));
    }

    @PostMapping("/generate")
    @Operation(summary = "根据故事想法生成剧本", description = "同步调用对话模型，新增一个剧本版本")
    public CommonResult<AiDramaScriptRespVO> generateScript(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        return success(BeanUtils.toBean(scriptService.generateScriptMy(getLoginUserId(), reqVO), AiDramaScriptRespVO.class));
    }

    @PostMapping("/polish")
    @Operation(summary = "按修改意见打磨剧本", description = "同步调用对话模型，新增一个剧本版本，记录修改意见")
    public CommonResult<AiDramaScriptRespVO> polishScript(@Valid @RequestBody AiDramaScriptPolishReqVO reqVO) {
        return success(BeanUtils.toBean(scriptService.polishScriptMy(getLoginUserId(), reqVO), AiDramaScriptRespVO.class));
    }

    @PostMapping("/analyze-asset-list")
    @Operation(summary = "从剧本分析角色、场景、道具", description = "只新增名称不重复的")
    public CommonResult<Boolean> analyzeAssetList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.analyzeAssetListMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-storyboard-list")
    @Operation(summary = "由剧本拆分分镜", description = "替换当前剧本的分镜，其它剧本版本的分镜保留")
    public CommonResult<Boolean> generateStoryboardList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.generateStoryboardListMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/plan-storyboard-list")
    @Operation(summary = "由剧本规划分镜", description = "只返回不保存，审阅修改后通过导入接口保存")
    public CommonResult<List<AiDramaStoryboardRespVO>> planStoryboardList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        return success(convertList(scriptService.planStoryboardListMy(getLoginUserId(), reqVO), plan ->
                BeanUtils.toBean(plan.getStoryboard(), AiDramaStoryboardRespVO.class).setPlanWarnings(plan.getWarnings())));
    }

    @PostMapping("/import-storyboard-list")
    @Operation(summary = "导入审阅后的分镜", description = "替换当前剧本的分镜")
    public CommonResult<Boolean> importStoryboardList(@Valid @RequestBody AiDramaStoryboardImportReqVO reqVO) {
        scriptService.importStoryboardListMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-photography-rules")
    @Operation(summary = "为分镜生成摄影规则", description = "当前剧本的全部分镜")
    public CommonResult<Boolean> generatePhotographyRules(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.generateStoryboardDetailMy(getLoginUserId(), reqVO, AiDramaChatTypeEnum.PHOTOGRAPHY_RULES);
        return success(true);
    }

    @PostMapping("/generate-acting-notes")
    @Operation(summary = "为分镜生成表演指导", description = "当前剧本的全部分镜")
    public CommonResult<Boolean> generateActingNotes(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.generateStoryboardDetailMy(getLoginUserId(), reqVO, AiDramaChatTypeEnum.ACTING_NOTES);
        return success(true);
    }

    @PostMapping("/plan-frame-list")
    @Operation(summary = "为分镜规划关键帧", description = "生成关键帧画面描述与画面焦点；当前剧本的全部分镜")
    public CommonResult<Boolean> planFrameList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.generateStoryboardDetailMy(getLoginUserId(), reqVO, AiDramaChatTypeEnum.FRAME_PLAN);
        return success(true);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式生成", description = "生成剧本、打磨剧本、分析资产、拆分分镜；前端断开后仍在后台完成并入库，可通过 stream-resume 接续")
    public Flux<CommonResult<String>> streamChat(@Valid @RequestBody AiDramaChatStreamReqVO reqVO) {
        return scriptService.streamChatMy(getLoginUserId(), reqVO);
    }

    @PostMapping(value = "/create-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "从故事想法创建项目", description = "流式生成初版剧本；生成、解析成功后创建项目并保存剧本，最后一条返回项目编号；" +
            "失败时不创建项目；前端断开后仍在后台完成")
    public Flux<CommonResult<AiDramaScriptCreateRespVO>> createProjectStream(@Valid @RequestBody AiDramaScriptCreateReqVO reqVO) {
        return scriptService.createProjectStreamMy(getLoginUserId(), reqVO);
    }

    @GetMapping(value = "/stream-resume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "接续流式生成", description = "先重放已生成的内容；没有进行中的生成时直接结束")
    @Parameter(name = "projectId", required = true, description = "项目编号", example = "1024")
    public Flux<CommonResult<String>> resumeChatStream(@RequestParam("projectId") Long projectId) {
        return scriptService.resumeChatStreamMy(getLoginUserId(), projectId);
    }

}
