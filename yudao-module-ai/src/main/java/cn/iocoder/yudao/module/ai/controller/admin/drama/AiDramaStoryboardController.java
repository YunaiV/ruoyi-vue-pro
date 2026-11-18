package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.*;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaStoryboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 短剧分镜")
@RestController
@RequestMapping("/ai/drama/storyboard")
public class AiDramaStoryboardController {

    @Resource
    private AiDramaStoryboardService storyboardService;

    @PostMapping("/create")
    @Operation(summary = "创建分镜")
    public CommonResult<Long> createStoryboard(@Valid @RequestBody AiDramaStoryboardSaveReqVO createReqVO) {
        return success(storyboardService.createStoryboardMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改分镜", description = "镜头视频生成中时不允许")
    public CommonResult<Boolean> updateStoryboard(@Valid @RequestBody AiDramaStoryboardSaveReqVO updateReqVO) {
        storyboardService.updateStoryboardMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除分镜")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<Boolean> deleteStoryboard(@RequestParam("id") Long id) {
        storyboardService.deleteStoryboardMy(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得分镜", description = "包括历史剧本版本的分镜；不存在时为空")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<AiDramaStoryboardRespVO> getStoryboard(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(storyboardService.getStoryboardMy(getLoginUserId(), id),
                AiDramaStoryboardRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得剧本版本的分镜列表", description = "用于只读查看历史剧本版本的镜头")
    @Parameter(name = "scriptId", required = true, description = "剧本编号", example = "1024")
    public CommonResult<List<AiDramaStoryboardRespVO>> getStoryboardList(@RequestParam("scriptId") Long scriptId) {
        return success(BeanUtils.toBean(storyboardService.getStoryboardListByScriptIdMy(getLoginUserId(), scriptId),
                AiDramaStoryboardRespVO.class));
    }

    @PostMapping("/generate-video")
    @Operation(summary = "生成镜头视频", description = "id 为分镜编号；提交服务商后由 Job 查询结果")
    public CommonResult<Boolean> generateVideo(@Valid @RequestBody AiDramaStoryboardVideoReqVO reqVO) {
        storyboardService.generateStoryboardVideoMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-video-batch")
    @Operation(summary = "批量生成镜头视频", description = "id 为项目编号；未生成、已失败的镜头逐个提交")
    public CommonResult<Integer> generateVideoBatch(@Valid @RequestBody AiDramaStoryboardVideoReqVO reqVO) {
        return success(storyboardService.generateStoryboardVideoBatchMy(getLoginUserId(), reqVO));
    }

    @PostMapping("/generate-frame")
    @Operation(summary = "生成镜头关键帧", description = "异步出图，结果写入关键帧；有修正说明时以当前关键帧为参考")
    public CommonResult<Boolean> generateFrame(@Valid @RequestBody AiDramaStoryboardFrameReqVO reqVO) {
        storyboardService.generateStoryboardFrameMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-frame-batch")
    @Operation(summary = "批量生成镜头关键帧", description = "当前剧本中镜头序号在范围内、还没有关键帧的镜头，一次最多 40 个")
    public CommonResult<Integer> generateFrameBatch(@Valid @RequestBody AiDramaStoryboardFrameBatchReqVO reqVO) {
        return success(storyboardService.generateStoryboardFrameBatchMy(getLoginUserId(), reqVO));
    }

    @PutMapping("/use-previous-last-frame")
    @Operation(summary = "承接上一镜头", description = "上一镜头视频的尾帧，作为本镜头的起始帧")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<Boolean> usePreviousLastFrame(@RequestParam("id") Long id) {
        storyboardService.usePreviousLastFrameMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/render-material")
    @Operation(summary = "素材图片渲染为镜头视频")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<Boolean> renderMaterial(@RequestParam("id") Long id) {
        storyboardService.renderStoryboardMaterialMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/import-video")
    @Operation(summary = "导入外部视频为镜头视频", description = "文件先通过文件上传接口上传；直接作为已完成的镜头视频")
    public CommonResult<Boolean> importVideo(@Valid @RequestBody AiDramaStoryboardImportVideoReqVO reqVO) {
        storyboardService.importStoryboardVideoMy(getLoginUserId(), reqVO);
        return success(true);
    }

    // ================ 声音 ================

    @GetMapping("/get-voice")
    @Operation(summary = "获得镜头的声音预览", description = "生成镜头视频时使用的发言角色、参考音频")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<AiDramaStoryboardVoiceRespVO> getVoice(@RequestParam("id") Long id) {
        return success(storyboardService.getStoryboardVoiceMy(getLoginUserId(), id));
    }

    @PutMapping("/update-speakers")
    @Operation(summary = "设置镜头的发言角色", description = "为空时按台词自动识别")
    public CommonResult<Boolean> updateSpeakers(@Valid @RequestBody AiDramaStoryboardSpeakerReqVO reqVO) {
        storyboardService.updateStoryboardSpeakersMy(getLoginUserId(), reqVO);
        return success(true);
    }

    // ================ 视频提交记录 ================

    @GetMapping("/video-submission/list")
    @Operation(summary = "获得镜头视频的提交记录", description = "新的在前")
    @Parameter(name = "storyboardId", required = true, description = "分镜编号", example = "1024")
    public CommonResult<List<AiDramaVideoSubmissionRespVO>> getVideoSubmissionList(@RequestParam("storyboardId") Long storyboardId) {
        return success(BeanUtils.toBean(storyboardService.getVideoSubmissionListMy(getLoginUserId(), storyboardId),
                AiDramaVideoSubmissionRespVO.class));
    }

    @PutMapping("/video-submission/resolve")
    @Operation(summary = "确认提交结果未知的任务编号", description = "填写在服务商控制台核对到的任务编号，镜头改为生成中")
    public CommonResult<Boolean> resolveVideoSubmission(@Valid @RequestBody AiDramaVideoSubmissionResolveReqVO reqVO) {
        storyboardService.resolveVideoSubmissionMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/video-submission/confirm-fail")
    @Operation(summary = "确认提交结果未知的提交为已失败", description = "已核对服务商未创建任务")
    @Parameter(name = "id", required = true, description = "提交记录编号", example = "1024")
    public CommonResult<Boolean> confirmVideoSubmissionFail(@RequestParam("id") Long id) {
        storyboardService.confirmVideoSubmissionFailMy(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/video-submission/use-video")
    @Operation(summary = "使用提交记录的视频", description = "previous 为 true 时恢复提交前的视频，否则使用本次提交成功的视频")
    @Parameter(name = "id", required = true, description = "提交记录编号", example = "1024")
    @Parameter(name = "previous", required = true, description = "是否恢复提交前的视频", example = "true")
    public CommonResult<Boolean> useVideoSubmission(@RequestParam("id") Long id, @RequestParam("previous") Boolean previous) {
        storyboardService.useVideoSubmissionMy(getLoginUserId(), id, previous);
        return success(true);
    }

}
